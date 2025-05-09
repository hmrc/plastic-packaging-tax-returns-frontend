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

import connectors.{CacheConnector, CalculateCreditsConnector}
import controllers.actions._
import factories.CreditSummaryListFactory
import models.requests.DataRequest
import models.requests.DataRequest.headerCarrier
import models.returns.CreditRangeOption
import models.{CreditBalance, Mode, ReturnsUserAnswers}
import navigation.ReturnsJourneyNavigator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.JsPath
import play.api.mvc.Results.{Ok, Redirect}
import play.api.mvc._
import views.html.returns.credits.ConfirmPackagingCreditView

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ConfirmPackagingCreditController @Inject() (
  override val messagesApi: MessagesApi,
  creditConnector: CalculateCreditsConnector,
  journeyAction: JourneyAction,
  val controllerComponents: MessagesControllerComponents,
  confirmCreditView: ConfirmPackagingCreditView,
  cacheConnector: CacheConnector,
  returnsJourneyNavigator: ReturnsJourneyNavigator,
  creditSummaryListFactory: CreditSummaryListFactory
)(implicit ec: ExecutionContext)
    extends I18nSupport {

  def onPageLoad(key: String, mode: Mode): Action[AnyContent] =
    journeyAction.async { implicit request =>
      // Although we don't use the obligation here, the backend will fail without it
      ReturnsUserAnswers.checkObligation(request) { _ =>
        creditConnector.getEventually(request.pptReference)
          .map(response => displayView(response, key, mode))
          .recover(_ =>
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad)
          ) // TODO do we want to do this?
      }
    }

  private def displayView(creditBalance: CreditBalance, key: String, mode: Mode)(implicit
    request: DataRequest[_]
  ): Result = {
    val singleYear        = creditBalance.creditForYear(key)
    val summaryList       = creditSummaryListFactory.createSummaryList(singleYear, key: String, request.userAnswers)
    val fromDate          = request.userAnswers.getOrFail[String](JsPath \ "credit" \ key \ "fromDate")
    val toDate            = request.userAnswers.getOrFail[String](JsPath \ "credit" \ key \ "toDate")
    val creditRangeOption = CreditRangeOption(LocalDate.parse(fromDate), LocalDate.parse(toDate))
    Ok(
      confirmCreditView(
        key,
        singleYear.moneyInPounds,
        summaryList,
        returnsJourneyNavigator.confirmCredit(mode),
        mode,
        creditRangeOption
      )
    )
  }
}
