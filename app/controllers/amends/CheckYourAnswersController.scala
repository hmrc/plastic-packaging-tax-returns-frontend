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

package controllers.amends

import cacheables.{ObligationCacheable, ReturnDisplayApiCacheable}
import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.TaxReturnsConnector
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.Mode
import models.returns.{ReturnDisplayApi, TaxReturnObligation}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{Json, OFormat}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.{Entry, SessionRepository}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.amends._
import viewmodels.govuk.summarylist._
import views.html.amends.CheckYourAnswersView

import scala.concurrent.ExecutionContext.Implicits.global

case class AmendSummaryRow(label: String, oldAnswer: String, newAnswer: String, changeUrl: String)

object AmendSummaryRow {
  implicit def jsonFormats: OFormat[AmendSummaryRow] =
    Json.using[Json.WithDefaultValues].format[AmendSummaryRow]
}

class CheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  returnsConnector: TaxReturnsConnector,
  appConfig: FrontendAppConfig,
  val controllerComponents: MessagesControllerComponents,
  sessionRepository: SessionRepository,
  view: CheckYourAnswersView
) extends FrontendBaseController with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData) {
      implicit request =>

        val rows: Seq[AmendSummaryRow] = Seq(
          AmendManufacturedPlasticPackagingSummary.buildRow(request.userAnswers),
          AmendImportedPlasticPackagingSummary.buildRow(request.userAnswers),
          AmendDirectExportPlasticPackagingSummary.buildRow(request.userAnswers),
          AmendHumanMedicinePlasticPackagingSummary.buildRow(request.userAnswers),
          AmendRecycledPlasticPackagingSummary.buildRow((request.userAnswers))
        ).flatten

        val list = SummaryListViewModel(rows =
          Seq(AmendManufacturedPlasticPackagingSummary,
              AmendImportedPlasticPackagingSummary,
              AmendHumanMedicinePlasticPackagingSummary,
              AmendDirectExportPlasticPackagingSummary,
              AmendRecycledPlasticPackagingSummary
          ).flatMap(_.row(request.userAnswers))
        )

        request.userAnswers.get[TaxReturnObligation](ObligationCacheable) match {
          case Some(obligation) =>
            if (appConfig.isAmendsFeatureEnabled) {Ok(view(mode, list, obligation, rows))}
          else
            {Redirect(controllers.routes.IndexController.onPageLoad)}
          case None => Redirect(routes.SubmittedReturnsController.onPageLoad())
        }
    }

  def onSubmit(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async {
      implicit request =>

        val pptId: String = request.request.pptReference

        val submissionId: String = request.userAnswers.get[ReturnDisplayApi](ReturnDisplayApiCacheable)
          .getOrElse(throw new IllegalStateException("must have a submission id to amend a return"))
          .idDetails
          .submissionId

        returnsConnector.amend(pptId, submissionId).flatMap {
          case Right(optChargeRef) =>
            sessionRepository.set(Entry(request.cacheKey, optChargeRef)).map{
              _ => Redirect(routes.AmendConfirmationController.onPageLoad())
            }
          case Left(error) =>
            throw error
        }
    }
}
