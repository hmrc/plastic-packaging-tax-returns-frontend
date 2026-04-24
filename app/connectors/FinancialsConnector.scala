/*
 * Copyright 2026 HM Revenue & Customs
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

package connectors

import config.FrontendAppConfig
import connectors.FinancialsConnector.{PaymentLinkRequest, PaymentLinkResponse}
import models.financials.PPTFinancials
import play.api.Logging
import play.api.libs.json.{Json, OWrites, Reads}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReadsInstances, StringContextOps}
import uk.gov.hmrc.play.bootstrap.metrics.Metrics

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FinancialsConnector @Inject() (httpClient: HttpClientV2, appConfig: FrontendAppConfig, metrics: Metrics)(implicit
  ec: ExecutionContext
) extends Logging
    with HttpReadsInstances {

  def getPaymentLink(pptReferenceNumber: String, amountInPence: Int, homeUrl: String, nextDueDate: Option[String])(
    implicit hc: HeaderCarrier
  ): Future[String] = {
    val request = PaymentLinkRequest(pptReferenceNumber, amountInPence, homeUrl, homeUrl, nextDueDate)
    httpClient
      .post(url"${appConfig.makePaymentUrl}")
      .withBody(Json.toJson(request))
      .execute[PaymentLinkResponse]
      .map { res =>
        logger.info(
          s"Payment redirect created for pptReferenceNumber: $pptReferenceNumber with journeyId:${res.journeyId}"
        )
        res.nextUrl
      }
  }

  def getPaymentStatement(pptReferenceNumber: String)(implicit hc: HeaderCarrier): Future[PPTFinancials] = {
    val timer = metrics.defaultRegistry.timer("ppt.financials.open.get.timer").time()
    httpClient
      .get(url"${appConfig.pptFinancialsUrl(pptReferenceNumber)}")
      .execute[PPTFinancials]
      .map { response =>
        logger.info(s"Retrieved open financials for ppt reference number [$pptReferenceNumber]")
        response
      }
      .andThen { case _ => timer.stop() }
      .recover { case exception: Exception =>
        throw DownstreamServiceError(
          s"Failed to retrieve open financials for PPTReference: [$pptReferenceNumber]," +
            s"error: [${exception.getMessage}]",
          exception
        )
      }
  }

}

object FinancialsConnector {
  private final case class PaymentLinkRequest(
    reference: String,
    amountInPence: Int,
    returnUrl: String,
    backUrl: String,
    dueDate: Option[String]
  )

  private object PaymentLinkRequest {
    implicit val writes: OWrites[PaymentLinkRequest] = Json.writes[PaymentLinkRequest]
  }

  private final case class PaymentLinkResponse(journeyId: String, nextUrl: String)

  private object PaymentLinkResponse {
    implicit val writes: Reads[PaymentLinkResponse] = Json.reads[PaymentLinkResponse]
  }

}
