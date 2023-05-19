/*
 * Copyright 2023 HM Revenue & Customs
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

import connectors.CalculateCreditsConnector
import controllers.actions._
import forms.returns.credits.CreditsClaimedListFormProvider
import models.requests.DataRequest
import models.requests.DataRequest.headerCarrier
import models.{CreditBalance, Mode}
import navigation.ReturnsJourneyNavigator
import pages.returns.credits.AvailableYears
import play.api.data.Form
import play.api.data.FormBinding.Implicits.formBinding
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsObject, JsPath}
import play.api.mvc.Results.{BadRequest, Ok, Redirect}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import viewmodels.checkAnswers.returns.credits.CreditsClaimedListSummary
import views.html.returns.credits.CreditsClaimedListView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CreditsClaimedListController @Inject()(
  override val messagesApi: MessagesApi,
  calcCreditsConnector: CalculateCreditsConnector,
  navigator: ReturnsJourneyNavigator,
  journeyAction: JourneyAction,
  formProvider: CreditsClaimedListFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: CreditsClaimedListView
)(implicit ec: ExecutionContext) extends I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = journeyAction.async {
    implicit request =>
      calcCreditsConnector.get(request.pptReference).map { creditBalance =>
        creditBalance.fold(
          error => throw error,
          balance => displayView(mode, balance)
        )
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = journeyAction.async {
    implicit request =>
      formProvider().bindFromRequest().fold(
        formWithErrors => {
          //todo: is there a better way to display the view without calling the API again?
          handleFormError(formWithErrors, mode)
        },
        isAddingAnotherYear =>
          Future.successful(Redirect(navigator.creditClaimedList(mode, isAddingAnotherYear, request.userAnswers)))
      )
  }

  private def handleFormError(
    formWithErrors: Form[Boolean],
    mode: Mode,
  )(implicit request: DataRequest[AnyContent]): Future[Result] = {
    calcCreditsConnector.get(request.pptReference).map { creditBalance =>
      creditBalance.fold(
        error => throw error,
        balance => BadRequest(view(formWithErrors, balance.canBeClaimed, moreYearsLeftToClaim, 
          createCreditSummary(balance, Some(navigator)), mode)),
          balance.canBeClaimed,
          CreditsClaimedListSummary.createCreditSummary(request.userAnswers, balance, Some(navigator)),
          mode)
        ),
      )
    }
  }

  private def displayView(
    mode: Mode,
    creditBalance: CreditBalance
  )(implicit request: DataRequest[AnyContent]): Result = {
    Ok(view(formProvider(), creditBalance.canBeClaimed, moreYearsLeftToClaim, 
      createCreditSummary(creditBalance, Some(navigator)), mode))
  }

  private def moreYearsLeftToClaim(implicit request: DataRequest[AnyContent]) ={
    val availableYears = request.userAnswers.getOrFail(AvailableYears)
    val alreadyUsedYears = request.userAnswers.get[Map[String, JsObject]](JsPath \ "credit").getOrElse(Map.empty).keySet

    alreadyUsedYears != availableYears.map(_.key).toSet
}
