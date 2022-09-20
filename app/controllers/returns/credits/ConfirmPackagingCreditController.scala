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

import connectors.CalculateCreditsConnector
import controllers.actions._
import models.{CreditBalance, Mode}
import models.Mode.CheckMode
import models.requests.DataRequest
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
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
  tooMuchCreditView: TooMuchCreditClaimedView
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

  private def displayView(response: CreditBalance, mode: Mode)(implicit request: DataRequest[_]): Result = {
    if (response.canBeClaimed) {
      val continueCall = if (mode == CheckMode) controllers.returns.routes.ReturnsCheckYourAnswersController.onPageLoad()
        else controllers.returns.routes.NowStartYourReturnController.onPageLoad

      Ok(
        confirmCreditView(
          response.totalRequestedCreditInPounds,
          response.totalRequestedCreditInKilograms,
          continueCall
        )
      )
    }
    else
      Ok(tooMuchCreditView())
  }
}
