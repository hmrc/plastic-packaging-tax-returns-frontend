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
import models.requests.DataRequest
import models.requests.DataRequest.headerCarrier
import models.{CreditBalance, Mode}
import navigation.ReturnsJourneyNavigator
import pages.returns.credits.WhatDoYouWantToDoPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Results.{Ok, Redirect}
import play.api.mvc._
import util.EdgeOfSystem
import views.html.returns.credits.{ConfirmPackagingCreditView, TooMuchCreditClaimedView}

import java.time.LocalDateTime
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ConfirmPackagingCreditController @Inject()(
  override val messagesApi: MessagesApi,
  creditConnector: CalculateCreditsConnector,
  journeyAction: JourneyAction,
  val controllerComponents: MessagesControllerComponents,
  confirmCreditView: ConfirmPackagingCreditView,
  tooMuchCreditView: TooMuchCreditClaimedView,
  cacheConnector: CacheConnector,
  returnsJourneyNavigator: ReturnsJourneyNavigator,
  edgeOfSystem: EdgeOfSystem
)(implicit ec: ExecutionContext)  extends I18nSupport {

  private val midnight1stApril2023 = LocalDateTime.of(2023, 4, 1, 0, 0, 0)

  def onPageLoad(mode: Mode): Action[AnyContent] =
    journeyAction.async {
      implicit request =>
        creditConnector.get(request.pptReference).map {
          case Right(response) => displayView(response, mode)
          case Left(_) => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad)
        }
    }


  private def displayView(creditBalance: CreditBalance, mode: Mode)(implicit request: DataRequest[_]): Result = {
    val isBeforeApril2023 = midnight1stApril2023.isAfter(edgeOfSystem.localDateTimeNow) 
    if (creditBalance.canBeClaimed) {
      val continueCall = returnsJourneyNavigator.confirmCreditRoute(mode)
      Ok(confirmCreditView(
        creditBalance.totalRequestedCreditInPounds,
        creditBalance.totalRequestedCreditInKilograms,
        creditBalance.taxRate,
        continueCall,
        mode,
        isBeforeApril2023)
      )
    } else {
      val changeWeightCall: Call = controllers.returns.credits.routes.ExportedCreditsController.onPageLoad(mode)
      val cancelClaimCall: Call = controllers.returns.credits.routes.ConfirmPackagingCreditController.onCancelClaim(mode)
      Ok(tooMuchCreditView(changeWeightCall, cancelClaimCall))
    }  
  }

  def onCancelClaim(mode: Mode): Action[AnyContent] =
    journeyAction.async {
      implicit request =>
        request.userAnswers
          .setOrFail(WhatDoYouWantToDoPage, false)
          .save(cacheConnector.saveUserAnswerFunc(request.pptReference))
          .map(_ => Redirect(returnsJourneyNavigator.confirmCreditRoute(mode)))
    }
}
