/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import cacheables.{AmendSelectedPeriodKey, ObligationCacheable}
import com.google.inject.Inject
import connectors.{ServiceError, TaxReturnsConnector}
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.helpers.{TaxLiability, TaxLiabilityFactory, TaxReturnHelper}
import models.Mode
import models.returns.{ReturnType, TaxReturn, TaxReturnObligation}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers._
import viewmodels.govuk.summarylist._
import views.html.ReturnsCheckYourAnswersView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReturnsCheckYourAnswersController @Inject()(
                                                   override val messagesApi: MessagesApi,
                                                   identify: IdentifierAction,
                                                   getData: DataRetrievalAction,
                                                   requireData: DataRequiredAction,
                                                   returnsConnector: TaxReturnsConnector,
                                                   taxReturnHelper: TaxReturnHelper,
                                                   val controllerComponents: MessagesControllerComponents,
                                                   view: ReturnsCheckYourAnswersView
                                                 ) extends FrontendBaseController with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData) {
      implicit request =>
        val list = SummaryListViewModel(rows =
          Seq(
            ManufacturedPlasticPackagingSummary,
            ManufacturedPlasticPackagingWeightSummary,
            ImportedPlasticPackagingSummary,
            ImportedPlasticPackagingWeightSummary,
            HumanMedicinesPlasticPackagingWeightSummary,
            ExportedPlasticPackagingWeightSummary,
            RecycledPlasticPackagingWeightSummary,
            ConvertedPackagingCreditSummary
          ).flatMap(_.row(request.userAnswers))
        )
        val answers = request.userAnswers.data.value.toMap
        val liability: TaxLiability = TaxLiabilityFactory.create(
          answers.getOrElse("manufacturedPlasticPackagingWeight", 0).toString.toLong,
          answers.getOrElse("importedPlasticPackagingWeight", 0).toString.toLong,
          answers.getOrElse("humanMedicinesPlasticPackagingWeight", 0).toString.toLong,
          answers.getOrElse("exportedPlasticPackagingWeight", 0).toString.toLong,
          answers.getOrElse("convertedPackagingCredit", 0).toString.toLong,
          answers.getOrElse("recycledPlasticPackagingWeight", 0).toString.toLong
        )
        Ok(view(mode, list, liability))
    }

  def onSubmit(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async {
      implicit request =>

        val pptId: String = request.request.enrolmentId.getOrElse(
          throw new IllegalStateException("no enrolmentId, all users at this point should have one")
        )

        val obligation = request.userAnswers.get[TaxReturnObligation](ObligationCacheable).getOrElse(
          throw new IllegalStateException("Obligation not found!")
        )

        val taxReturn = taxReturnHelper.getTaxReturn(pptId, request.userAnswers, obligation.periodKey, ReturnType.NEW)

        submit(taxReturn).map {
          case Right(_) =>
            Redirect(routes.ReturnConfirmationController.onPageLoad())

          case Left(error) =>
            throw error

        }
    }

  private def submit(
                      taxReturn: TaxReturn
                    )(implicit hc: HeaderCarrier): Future[Either[ServiceError, Unit]] =
    returnsConnector.submit(taxReturn)

}
