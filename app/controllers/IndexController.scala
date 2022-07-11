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

import config.{Features, FrontendAppConfig}
import connectors.{FinancialsConnector, ObligationsConnector, SubscriptionConnector}
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import models.EisFailure
import models.financials.PPTFinancials
import models.obligations.PPTObligations
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.IndexView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IndexController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  identify: IdentifierAction,
  view: IndexView,
  appConfig: FrontendAppConfig,
  subscriptionConnector: SubscriptionConnector,
  financialsConnector: FinancialsConnector,
  obligationsConnector: ObligationsConnector,
  getData: DataRetrievalAction
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] =
    (identify andThen getData).async { implicit request =>
      val pptReference = request.pptReference

      subscriptionConnector.get(pptReference).flatMap {
        case Right(subscription) =>
          for {
            paymentStatement <- getPaymentsStatement(pptReference)
            obligations      <- getObligationsDetail(pptReference)
          } yield Ok(
            view(
              appConfig,
              subscription,
              obligations,
              paymentStatement,
              appConfig.pptCompleteReturnGuidanceUrl,
              pptReference
            )
          )
        case Left(eisFailure) =>
          if (eisFailure.isDeregistered) {
            Future.successful(Redirect(routes.DeregisteredController.onPageLoad()))
          } else {
            throw new RuntimeException(
              s"Failed to get subscription - ${eisFailure.failures.map(_.headOption.map(_.reason))
                .getOrElse("no underlying reason supplied")}"
            )
          }
      }
    }

  private def getPaymentsStatement(
    pptReference: String
  )(implicit hc: HeaderCarrier, messages: Messages): Future[Option[String]] =
    if (appConfig.isFeatureEnabled(Features.paymentsEnabled)) {
      financialsConnector.getPaymentStatement(pptReference).map(
        response => Some(response.paymentStatement()(messages))
      ).recover { case _ => None}
    } else {
      Future.successful(Some(PPTFinancials(None, None, None).paymentStatement()(messages)))
    }

  private def getObligationsDetail(
    pptReference: String
  )(implicit hc: HeaderCarrier): Future[Option[PPTObligations]] =
    if (appConfig.isFeatureEnabled(Features.returnsEnabled)) {
      obligationsConnector.getOpen(pptReference).map(response => Some(response)).recoverWith {
        case _ => Future(None)
      }
    } else {
      Future.successful(Some(PPTObligations(None, None, 0, false, false)))
    }

}
