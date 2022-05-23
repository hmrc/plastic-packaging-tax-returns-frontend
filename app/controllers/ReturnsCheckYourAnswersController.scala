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

import cacheables.ObligationCacheable
import com.google.inject.Inject
import connectors.TaxReturnsConnector
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.helpers.{TaxLiability, TaxLiabilityFactory, TaxReturnHelper}
import models.Mode
import models.returns.{ReturnType, TaxReturnObligation}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.{Entry, SessionRepository}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.ImportedPlasticPackagingSummary.CheckYourAnswerImportedPlasticPackagingSummary
import viewmodels.checkAnswers.ImportedPlasticPackagingWeightSummary.CheckYourAnswerImportedPlasticPackagingWeight
import viewmodels.checkAnswers.ManufacturedPlasticPackagingSummary.CheckYourAnswerManufacturedPlasticPackaging
import viewmodels.checkAnswers.ManufacturedPlasticPackagingWeightSummary.CheckYourAnswerForManufacturedPlasticWeight
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
                                                   sessionRepository: SessionRepository,
                                                   val controllerComponents: MessagesControllerComponents,
                                                   view: ReturnsCheckYourAnswersView
                                                 ) extends FrontendBaseController with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async {
      implicit request =>
        val list = SummaryListViewModel(rows =
          Seq(
            CheckYourAnswerManufacturedPlasticPackaging,
            CheckYourAnswerForManufacturedPlasticWeight,
            CheckYourAnswerImportedPlasticPackagingSummary,
            CheckYourAnswerImportedPlasticPackagingWeight,
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
          BigDecimal(answers.getOrElse("convertedPackagingCredit", 0).toString),
          answers.getOrElse("recycledPlasticPackagingWeight", 0).toString.toLong
        )
        request.userAnswers.get[TaxReturnObligation](ObligationCacheable) match {
          case Some(obligation) => Future.successful(Ok(view( mode, list, liability, obligation)))
          case None             => Future.successful(Redirect(routes.IndexController.onPageLoad))
        }

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

        returnsConnector.submit(taxReturn).flatMap {
          case Right(optChargeRef) =>
            sessionRepository.set(Entry(request.userId, optChargeRef)).map{
              _ => Redirect(routes.ReturnConfirmationController.onPageLoad())
            }
          case Left(error) => throw error
        }
    }

}
