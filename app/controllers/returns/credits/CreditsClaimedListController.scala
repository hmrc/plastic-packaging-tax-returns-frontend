/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.returns.credits

import connectors.{AvailableCreditYearsConnector, CalculateCreditsConnector}
import controllers.actions._
import forms.returns.credits.CreditsClaimedListFormProvider
import models.requests.DataRequest
import models.requests.DataRequest.headerCarrier
import models.returns.CreditRangeOption
import models.{CreditBalance, Mode, ReturnsUserAnswers}
import navigation.ReturnsJourneyNavigator
import play.api.data.FormBinding.Implicits.formBinding
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsObject, JsPath}
import play.api.mvc.Results.{BadRequest, Ok, Redirect}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import viewmodels.checkAnswers.returns.credits.CreditsClaimedListSummary.createCreditSummary
import views.html.returns.credits.CreditsClaimedListView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CreditsClaimedListController @Inject() (
  override val messagesApi: MessagesApi,
  calcCreditsConnector: CalculateCreditsConnector,
  availableCreditYearsConnector: AvailableCreditYearsConnector,
  navigator: ReturnsJourneyNavigator,
  journeyAction: JourneyAction,
  formProvider: CreditsClaimedListFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: CreditsClaimedListView
)(implicit ec: ExecutionContext)
    extends I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = journeyAction.async { implicit request =>
    // Although we don't use the obligation here, the backend will fail without it
    ReturnsUserAnswers.checkObligation(request) { _ =>
      calcCreditsConnector.getEventually(request.pptReference).flatMap { balance =>
        displayView(mode, balance)
      }
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = journeyAction.async { implicit request =>
    availableCreditYearsConnector.get(request.pptReference).flatMap { options =>
      val remainingOpts = remainingOptions(options)
      formProvider(remainingOpts).bindFromRequest().fold(
        formWithErrors =>
          calcCreditsConnector.getEventually(request.pptReference).map { creditBalance =>
            BadRequest(
              view(
                formWithErrors,
                creditBalance,
                earliestCreditDate(options),
                remainingOpts,
                createCreditSummary(request.userAnswers, creditBalance, Some(navigator)),
                mode
              )
            )
          },
        isAddingAnotherYear =>
          Future.successful(Redirect(navigator.creditClaimedList(mode, isAddingAnotherYear, request.userAnswers)))
      )
    }
  }

  private def displayView(
    mode: Mode,
    creditBalance: CreditBalance
  )(implicit request: DataRequest[AnyContent]): Future[Result] = {
    availableCreditYearsConnector.get(request.pptReference).map(options =>
      Ok(
        view(
          formProvider(remainingOptions(options)),
          creditBalance,
          earliestCreditDate(options),
          remainingOptions(options),
          createCreditSummary(request.userAnswers, creditBalance, Some(navigator)),
          mode
        )
      )
    )
  }

  def earliestCreditDate(available: Seq[CreditRangeOption]) =
    available.minBy(_.from).from

  private def remainingOptions(
    available: Seq[CreditRangeOption]
  )(implicit request: DataRequest[AnyContent]): Seq[CreditRangeOption] = {
    val alreadyUsedYears = request.userAnswers.get[Map[String, JsObject]](JsPath \ "credit").getOrElse(Map.empty).keySet

    available.collect { case y if !alreadyUsedYears.contains(y.key) => y }
  }

}
