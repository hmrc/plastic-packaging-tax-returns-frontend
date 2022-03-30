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

import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import uk.gov.hmrc.plasticpackagingtax.returns.config.{AppConfig, Features}
import uk.gov.hmrc.plasticpackagingtax.returns.connectors.{
  DownstreamServiceError,
  FinancialsConnector,
  ObligationsConnector,
  SubscriptionConnector
}
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.actions.AuthAction
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.deregistration.{
  routes => deregistrationRoutes
}
import uk.gov.hmrc.plasticpackagingtax.returns.models.EisFailure
import uk.gov.hmrc.plasticpackagingtax.returns.models.financials.PPTFinancials
import uk.gov.hmrc.plasticpackagingtax.returns.models.obligations.PPTObligations
import uk.gov.hmrc.plasticpackagingtax.returns.models.subscription.subscriptionDisplay.SubscriptionDisplayResponse
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

      subscriptionConnector.get(pptReference).flatMap {
        case Right(subscription) =>
          for {
            paymentStatement <- getPaymentsStatement(pptReference)
            obligations      <- getObligationsDetail(pptReference)
          } yield Ok(
            homePage(appConfig,
                     subscription,
                     obligations,
                     paymentStatement,
                     appConfig.pptCompleteReturnGuidanceUrl,
                     pptReference
            )
          )
        case Left(eisFailure) =>
          if (isDeregistered(eisFailure))
            Future.successful(Redirect(deregistrationRoutes.DeregisteredController.displayPage()))
          else
            throw new RuntimeException(
              s"Failed to get subscription - ${eisFailure.failures.headOption.map(_.reason)
                .getOrElse("no underlying reason supplied")}"
            )
      }
    }

  private def getPaymentsStatement(
    pptReference: String
  )(implicit hc: HeaderCarrier, messages: Messages): Future[Option[String]] =
    if (appConfig.isFeatureEnabled(Features.paymentsEnabled))
      financialsConnector.getPaymentStatement(pptReference)(hc).map(
        response => Some(response.paymentStatement()(messages))
      ).recoverWith { case _: Exception => Future(None) }
    else Future.successful(Some(PPTFinancials(None, None, None).paymentStatement()(messages)))

  private def getObligationsDetail(
    pptReference: String
  )(implicit hc: HeaderCarrier): Future[Option[PPTObligations]] =
    if (appConfig.isFeatureEnabled(Features.returnsEnabled))
      obligationsConnector.get(pptReference).map(response => Some(response)).recoverWith {
        case _ => Future(None)
      }
    else Future.successful(Some(PPTObligations(None, None, 0, false, false)))

  private def isDeregistered(eisFailure: EisFailure) =
    eisFailure.failures.exists(_.code == "NO_DATA_FOUND")

}
