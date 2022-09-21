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

package controllers.returns.credits

import connectors.{CacheConnector, CalculateCreditsConnector}
import controllers.actions._
import models.{CreditBalance, Mode}
import models.requests.DataRequest
import navigation.ReturnsJourneyNavigator
import pages.returns.credits.WhatDoYouWantToDoPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.returns.credits.{ConfirmPackagingCreditView, TooMuchCreditClaimedView}

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ConfirmPackagingCreditController @Inject()(
  override val messagesApi: MessagesApi,
  creditConnector: CalculateCreditsConnector,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  confirmCreditView: ConfirmPackagingCreditView,
  tooMuchCreditView: TooMuchCreditClaimedView,
  cacheConnector: CacheConnector,
  returnsJourneyNavigator: ReturnsJourneyNavigator
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async {
      implicit request: DataRequest[AnyContent] =>
        creditConnector.get(request.pptReference).map {
          case Right(response) => displayView(response, mode)
          case Left(_) => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad)
        }
    }


  private def displayView(creditBalance: CreditBalance, mode: Mode)(implicit request: DataRequest[_]): Result = {
    if (creditBalance.canBeClaimed) {
      val continueCall = returnsJourneyNavigator.confirmCreditRoute(mode)
      Ok(confirmCreditView(
        creditBalance.totalRequestedCreditInPounds,
        creditBalance.totalRequestedCreditInKilograms,
        continueCall,
        mode)
      )
    } else {
      val changeWeightCall: Call = controllers.returns.credits.routes.ExportedCreditsController.onPageLoad(mode)
      val cancelClaimCall: Call = controllers.returns.credits.routes.ConfirmPackagingCreditController.onCancelClaim(mode)
      Ok(tooMuchCreditView(changeWeightCall, cancelClaimCall))
    }  
  }

  def onCancelClaim(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async {
      implicit request =>
        request.userAnswers
          .setOrFail(WhatDoYouWantToDoPage, false)
          .save(cacheConnector.saveUserAnswerFunc(request.pptReference))
          .map(_ => Redirect(returnsJourneyNavigator.confirmCreditRoute(mode)))
    }

}
