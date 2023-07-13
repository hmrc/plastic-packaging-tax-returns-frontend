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

package controllers

import config.FrontendAppConfig
import connectors.{FinancialsConnector, ObligationsConnector}
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import models.PPTSubscriptionDetails
import models.obligations.PPTObligations
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, Results}
import repositories.SessionRepository
import repositories.SessionRepository.Paths.SubscriptionIsActive
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendHeaderCarrierProvider
import views.html.IndexView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IndexController @Inject() (
  val messagesApi: MessagesApi,
  identify: IdentifierAction,
  view: IndexView,
  appConfig: FrontendAppConfig,
  sessionRepository: SessionRepository,
  financialsConnector: FinancialsConnector,
  obligationsConnector: ObligationsConnector,
  getData: DataRetrievalAction
)(implicit ec: ExecutionContext)
    extends Results with I18nSupport with FrontendHeaderCarrierProvider {

  def onPageLoad: Action[AnyContent] =
    (identify andThen getData).async { implicit request =>
      val pptReference = request.pptReference
        for {
          legalEntity      <- sessionRepository.get[PPTSubscriptionDetails](request.cacheKey, SubscriptionIsActive)
          paymentStatement <- getPaymentsStatement(pptReference)
          obligations      <- getObligationsDetail(pptReference)
          isFirstReturn    <- isFirstReturn(pptReference)
        } yield Ok(
          view(
            legalEntity.get.legalEntityDetails,
            obligations,
            isFirstReturn,
            paymentStatement,
            pptReference
          )
        )
    }

  private def getPaymentsStatement(
    pptReference: String
  )(implicit hc: HeaderCarrier, messages: Messages): Future[Option[String]] =
    financialsConnector.getPaymentStatement(pptReference).map(
      response => Some(response.paymentStatement()(messages))
    ).recover { case _ => None}

  private def isFirstReturn(pptReference: String)(implicit hc: HeaderCarrier): Future[Boolean] =
    obligationsConnector.getFulfilled(pptReference).map(_.isEmpty).recoverWith {
      case _ => Future.successful(false) //assume not first return
    }

  private def getObligationsDetail(
    pptReference: String
  )(implicit hc: HeaderCarrier): Future[Option[PPTObligations]] =
    obligationsConnector.getOpen(pptReference).map(response => Some(response)).recoverWith {
      case _ => Future(None)
    }

}
