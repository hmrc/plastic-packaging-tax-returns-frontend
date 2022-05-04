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

import cacheables.{AmendSelectedPeriodKey, ReturnDisplayApiCacheable}
import com.google.inject.Inject
import connectors.{ServiceError, TaxReturnsConnector}
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.helpers.TaxReturnHelper
import models.Mode
import models.returns.{ReturnDisplayApi, ReturnType, TaxReturn}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers._
import viewmodels.govuk.summarylist._
import views.html.CheckYourAnswersView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  returnsConnector: TaxReturnsConnector,
  taxReturnHelper: TaxReturnHelper,
  val controllerComponents: MessagesControllerComponents,
  view: CheckYourAnswersView
) extends FrontendBaseController with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData) {
      implicit request =>
        val list = SummaryListViewModel(rows =
          Seq(AmendManufacturedPlasticPackagingSummary,
              AmendImportedPlasticPackagingSummary,
              AmendHumanMedicinePlasticPackagingSummary,
              AmendDirectExportPlasticPackagingSummary,
              AmendRecycledPlasticPackagingSummary
          ).flatMap(_.row(request.userAnswers))
        )

        request.userAnswers.get[ReturnDisplayApi](ReturnDisplayApiCacheable) match {
          case Some(displayApi) => Ok(view(mode, list, displayApi))
          case None             => Redirect(routes.SubmittedReturnsController.onPageLoad())
        }
    }

  def onSubmit(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async {
      implicit request =>

        val pptId: String = request.request.enrolmentId.getOrElse(
          throw new IllegalStateException("no enrolmentId, all users at this point should have one")
        )

        val obligation = request.userAnswers.get[String](AmendSelectedPeriodKey).getOrElse(
          throw new IllegalStateException("Obligation not found!")
        )

        val taxReturn = taxReturnHelper.getTaxReturn(pptId, request.userAnswers, obligation, ReturnType.AMEND)
        submit(taxReturn).map {
          case Right(_) =>
            Redirect(routes.AmendConfirmationController.onPageLoad())

          case Left(error) =>
            throw error

        }
    }

  private def submit(
    taxReturn: TaxReturn
  )(implicit hc: HeaderCarrier): Future[Either[ServiceError, Unit]] =
    returnsConnector.amend(taxReturn)

}
