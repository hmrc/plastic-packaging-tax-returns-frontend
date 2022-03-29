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

package uk.gov.hmrc.plasticpackagingtax.returns.controllers.home

import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.UpstreamErrorResponse
import uk.gov.hmrc.plasticpackagingtax.returns.config.AppConfig
import uk.gov.hmrc.plasticpackagingtax.returns.connectors.{
  FinancialsConnector,
  ObligationsConnector,
  SubscriptionConnector
}
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.actions.AuthAction
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.deregistration.{
  routes => deregistrationRoutes
}
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.home.home_page
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class HomeController @Inject() (
  authenticate: AuthAction,
  subscriptionConnector: SubscriptionConnector,
  financialsConnector: FinancialsConnector,
  obligationsConnector: ObligationsConnector,
  appConfig: AppConfig,
  mcc: MessagesControllerComponents,
  homePage: home_page
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayPage: Action[AnyContent] =
    authenticate.async { implicit request =>
      val pptReference =
        request.enrolmentId.getOrElse(throw new IllegalStateException("no enrolmentId"))

      subscriptionConnector.get(pptReference).flatMap(
        subscription =>
          for {
            paymentStatement <- financialsConnector.getPaymentStatement(pptReference).map(
              response => Some(response.paymentStatement())
            ).recoverWith { case _: Exception => Future(None) }
            obligations <- obligationsConnector.get(pptReference).map(
              response => Some(response)
            ).recoverWith { case _ => Future(None) }
          } yield Ok(
            homePage(subscription,
                     obligations,
                     paymentStatement,
                     appConfig.pptCompleteReturnGuidanceUrl,
                     pptReference
            )
          )
      ).recover {
        case httpEx: UpstreamErrorResponse if isDeregistered(httpEx) =>
          Redirect(deregistrationRoutes.DeregisteredController.displayPage())
        case ex: Throwable => throw ex
      }

    }

  private def isDeregistered(ex: UpstreamErrorResponse) = ex.statusCode == NOT_FOUND
}
