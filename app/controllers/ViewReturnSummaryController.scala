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

import connectors.{ServiceError, TaxReturnsConnector}
import controllers.actions._
import models.NormalMode
import models.returns.SubmittedReturn
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ViewReturnSummaryView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ViewReturnSummaryController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  val controllerComponents: MessagesControllerComponents,
  view: ViewReturnSummaryView,
  returnsConnector: TaxReturnsConnector
)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  // TODO stubs totally ignore this right now
  private val hardcoded_period_key = "yet-more-cheese-biscuits"

  // TODO Need to get this from auth
  private val hardcoded_ppt_ref = "XMPPT0000000001"

  def onPageLoad : Action[AnyContent] =
    identify.async {
      implicit request =>
        val taxReturn: Future[SubmittedReturn] = fetchTaxReturn(hardcoded_ppt_ref, hardcoded_period_key)
        taxReturn.map {
          _ => Ok(view())
        }
    }

  private def fetchTaxReturn(userId: String, periodKey: String)(implicit hc: HeaderCarrier): Future[SubmittedReturn] = {
    val future: Future[Either[ServiceError, SubmittedReturn]] = returnsConnector.get(userId, periodKey)
    future.map {
      case Right(taxReturn) => taxReturn
      case Left(error) => throw error
    }
  }

  def onSubmit(): Action[AnyContent] =
    identify {
      implicit request =>
        Redirect(routes.AmendAreYouSureController.onPageLoad(NormalMode))
    }

}
