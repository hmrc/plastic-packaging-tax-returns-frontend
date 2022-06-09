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

package controllers.returns

import cacheables.ObligationCacheable
import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.TaxReturnsConnector
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.helpers.{TaxLiability, TaxLiabilityFactory, TaxReturnHelper, TaxReturnViewModel}
import models.requests.DataRequest
import models.returns.{ReturnType, TaxReturnObligation}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.{Entry, SessionRepository}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.returns.ImportedPlasticPackagingSummary.CheckYourAnswerImportedPlasticPackagingSummary
import viewmodels.checkAnswers.returns.ImportedPlasticPackagingWeightSummary.CheckYourAnswerImportedPlasticPackagingWeight
import viewmodels.checkAnswers.returns.ManufacturedPlasticPackagingSummary.CheckYourAnswerManufacturedPlasticPackaging
import viewmodels.checkAnswers.returns.ManufacturedPlasticPackagingWeightSummary.CheckYourAnswerForManufacturedPlasticWeight
import viewmodels.checkAnswers.returns._
import viewmodels.govuk.summarylist._
import views.html.returns.ReturnsCheckYourAnswersView

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
  view: ReturnsCheckYourAnswersView,
  appConfig: FrontendAppConfig
) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async {
      implicit request =>
        val list = SummaryListViewModel(rows =
          Seq(
            CheckYourAnswerManufacturedPlasticPackaging,
            CheckYourAnswerForManufacturedPlasticWeight,
            CheckYourAnswerImportedPlasticPackagingSummary,
            CheckYourAnswerImportedPlasticPackagingWeight,
            new HumanMedicinesPlasticPackagingSummary,
            new HumanMedicinesPlasticPackagingWeightSummary,
            DirectlyExportedComponentsSummary,
            new ExportedPlasticPackagingWeightSummary,
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
          case Some(obligation) => displayPage(request, list, liability, obligation)
          case None             => Future.successful(Redirect(controllers.routes.IndexController.onPageLoad))
        }

    }

  private def displayPage(request: DataRequest[AnyContent], list: SummaryList, liability: TaxLiability,
    obligation: TaxReturnObligation)(implicit messages: Messages) = {

    val creditsAdviceUrl = appConfig.creditsAdviceUrl
    val returnViewModel = TaxReturnViewModel(request.pptReference, obligation, request.userAnswers)
    Future.successful(Ok(view(list, liability, obligation, returnViewModel, request.pptReference, creditsAdviceUrl)(request, messages)))
  }

  def onSubmit(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async {
      implicit request =>

        val pptId: String = request.pptReference

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
