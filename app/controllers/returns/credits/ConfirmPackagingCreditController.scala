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

import connectors.{CacheConnector, CalculateCreditsConnector}
import controllers.actions._
import factories.CreditSummaryListFactory
import models.requests.DataRequest
import models.requests.DataRequest.headerCarrier
import models.{CreditBalance, Mode}
import navigation.ReturnsJourneyNavigator
import pages.returns.credits.{ConvertedCreditsPage, ExportedCreditsPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Results.{Ok, Redirect}
import play.api.mvc._
import views.html.returns.credits.ConfirmPackagingCreditView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ConfirmPackagingCreditController @Inject()( //todo rename to something like OneYearCreditCheckYourAnswers vs AllYearsBlah
  override val messagesApi: MessagesApi,
  creditConnector: CalculateCreditsConnector,
  journeyAction: JourneyAction,
  val controllerComponents: MessagesControllerComponents,
  confirmCreditView: ConfirmPackagingCreditView,
  cacheConnector: CacheConnector,
  returnsJourneyNavigator: ReturnsJourneyNavigator,
  creditSummaryListFactory: CreditSummaryListFactory
)(implicit ec: ExecutionContext)  extends I18nSupport {

  def onPageLoad(key: String, mode: Mode): Action[AnyContent] =
    journeyAction.async {
      implicit request =>
        if (!isUserAnswerValid(key)) {
          Future.successful(Redirect(controllers.returns.credits.routes.WhatDoYouWantToDoController.onPageLoad(mode)))
        } else {
          creditConnector.get(request.pptReference).map { //todo does this make sense to call here?
            case Right(response) => displayView(response, key, mode)
            case Left(_) => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad)
          }
        }
    }

  private def isUserAnswerValid(key: String)(implicit request: DataRequest[_]) = {
    request.userAnswers.get(ExportedCreditsPage(key)).isDefined &&
      request.userAnswers.get(ConvertedCreditsPage(key)).isDefined
  }

  private def displayView(creditBalance: CreditBalance, key: String, mode: Mode)(implicit request: DataRequest[_]): Result = {

    //Todo: Pass to the view the fromDate and toDate the user is claiming
    // credit for.
    val singleYear = creditBalance.creditForYear(key)
    Ok(confirmCreditView(
        key,
        singleYear.moneyInPounds,
        canClaim = true, // TODO rigged to "can claim" to avoid a multi-year claim stopping here (this page is about a single year) 
        creditSummaryListFactory.createSummaryList(creditBalance, key: String, request.userAnswers),
        returnsJourneyNavigator.confirmCredit(mode),
        mode
      )
    )
  }
}
