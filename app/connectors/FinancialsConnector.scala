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

package connectors

import com.kenshoo.play.metrics.Metrics
import config.FrontendAppConfig
import models.financials.PPTFinancials
import play.api.Logging
import play.api.libs.json.{Json, OWrites, Reads}
import play.api.mvc.Call
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FinancialsConnector @Inject() (
  httpClient: HttpClient,
  appConfig: FrontendAppConfig,
  metrics: Metrics
)(implicit ec: ExecutionContext)
    extends Logging {

  //todo move these? companion?
  //{
  //  "reference": "XAPPT0012345678",
  //  "amountInPence": 200099,
  //  "returnUrl": "https://www.tax.service.gov.uk/plastic-packaging-tax/account",
  //  "backUrl": "https://www.tax.service.gov.uk/plastic-packaging-tax/account"
  //}

  final case class PaymentLinkRequest(reference: String, amountInPence: Int, returnUrl: String, backUrl: String)
  object PaymentLinkRequest {
    implicit val writes: OWrites[PaymentLinkRequest] = Json.writes[PaymentLinkRequest]
  }

  //{
  // "journeyId": "592d4a09cdc8e04b00021459",
  // "nextUrl" : "http://localhost:9056/pay/choose-a-way-to-pay?traceId=12345678"
  //}
  final case class PaymentLinkResponse(journeyId: String, nextUrl: String)
  object PaymentLinkResponse {
    implicit val writes: Reads[PaymentLinkResponse] = Json.reads[PaymentLinkResponse]
  }

  def getPaymentLink(
                      pptReferenceNumber: String,
                      amountInPence: Int,
                      homeUrl: String
                    )(implicit hc: HeaderCarrier): Future[String] = {
    val request = PaymentLinkRequest(pptReferenceNumber, amountInPence, homeUrl, homeUrl)
    httpClient.POST[PaymentLinkRequest, PaymentLinkResponse](appConfig.makePaymentUrl, request).map{ res =>
      logger.info(s"Payment redirect created for pptReferenceNumber: $pptReferenceNumber with journeyId:${res.journeyId}")
      res.nextUrl
    }
  }

  def getPaymentStatement(
    pptReferenceNumber: String
  )(implicit hc: HeaderCarrier): Future[PPTFinancials] = {
    val timer = metrics.defaultRegistry.timer("ppt.financials.open.get.timer").time()
    httpClient.GET[PPTFinancials](appConfig.pptFinancialsUrl(pptReferenceNumber))
      .map {
        response =>
          logger.info(s"Retrieved open financials for ppt reference number [$pptReferenceNumber]")
          response
      }
      .andThen { case _ => timer.stop() }
      .recover {
        case exception: Exception =>
          throw DownstreamServiceError(
            s"Failed to retrieve open financials for PPTReference: [$pptReferenceNumber], error: [${exception.getMessage}]",
            exception
          )
      }
  }

}
