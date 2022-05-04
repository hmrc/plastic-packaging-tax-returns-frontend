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

import config.FrontendAppConfig
import connectors.FinancialsConnector
import controllers.actions.IdentifierAction
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class MakePaymentController  @Inject() (
                                         override val messagesApi: MessagesApi,
                                         identify: IdentifierAction,
                                         val controllerComponents: MessagesControllerComponents,
                                         financialsConnector: FinancialsConnector,
                                         appConfig: FrontendAppConfig
                                       )(implicit ec: ExecutionContext)
  extends FrontendBaseController with I18nSupport {

  def redirectLink(): Action[AnyContent] = identify.async {implicit request =>
    val pptRef = request.enrolmentId.getOrElse(throw new IllegalStateException("no enrolmentId, all users at this point should have one"))

    for {
      financials <- financialsConnector.getPaymentStatement(pptRef)
      amountInPence = financials.amountToPayInPence
      link <- financialsConnector.getPaymentLink(pptRef, amountInPence, homeUrl = appConfig.returnUrl(routes.IndexController.onPageLoad.url))
    } yield {
      Redirect(Call("GET", link))
    }
  }

}
