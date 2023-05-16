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

import connectors.{CalculateCreditsConnector, ServiceError}
import controllers.actions._
import forms.returns.credits.CreditsClaimedListFormProvider
import models.requests.DataRequest
import models.{CreditBalance, Mode}
import models.requests.DataRequest.headerCarrier
import models.returns.credits.CreditSummaryRow
import navigation.ReturnsJourneyNavigator
import play.api.data.Form
import play.api.data.FormBinding.Implicits.formBinding
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Results.{BadRequest, Ok, Redirect}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import viewmodels.checkAnswers.returns.credits.{CreditTotalSummary, CreditsClaimedListSummary}
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
          calcCreditsConnector.get(request.pptReference).map { creditBalance =>
            creditBalance.fold(
              error => throw error,
              balance => BadRequest(view(formWithErrors, createCreditSummary(balance), mode)),
            )
          }
        },
        isAddingAnotherYear =>
          Future.successful(Redirect(navigator.creditClaimedList(mode, isAddingAnotherYear, request.userAnswers)))
      )
  }

  private def displayView(
    mode: Mode,
    creditBalance: CreditBalance
  )(implicit request: DataRequest[AnyContent]): Result = {
    Ok(view(formProvider(), createCreditSummary(creditBalance), mode))
  }

  private def createCreditSummary(
    creditBalance: CreditBalance
  )(implicit request: DataRequest[AnyContent]): Seq[CreditSummaryRow] = {
    CreditsClaimedListSummary.createRows(request.userAnswers, navigator) match {
      case Nil => Seq.empty
      case list => list :+ CreditTotalSummary.createRow(creditBalance.totalRequestedCreditInPounds)
    }
  }
}
