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
import connectors.DirectDebitConnector._
import play.api.Logging
import play.api.libs.json.{Json, OWrites, Reads}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DirectDebitConnector @Inject()
(
  httpClient: HttpClient,
  appConfig: FrontendAppConfig,
  metrics: Metrics
)(implicit ec: ExecutionContext)
  extends Logging {

  def getDirectDebitLink(pptReference: String, homeUrl: String)(implicit hc: HeaderCarrier): Future[String] = {
    val timer = metrics.defaultRegistry.timer("ppt.financials.open.get.timer").time()
    val request = DDLinkRequest(pptReference, "zppt", homeUrl, homeUrl)
    httpClient.POST[DDLinkRequest, DDLinkResponse](appConfig.pptStartDirectDebit, request).map { res =>
      logger.info(s"DD redirect created for pptReferenceNumber: $pptReference with journeyId:${res.journeyId}")
      res.nextUrl
    }
      .andThen { case _ => timer.stop() }
      .recover {
        case exception: Exception =>
          throw DownstreamServiceError("Error trying to get Direct Debit link",
            exception)
      }
  }
}

object DirectDebitConnector {
  private final case class DDLinkRequest(taxIdValue: String, taxIdType: String, returnUrl: String, backUrl: String)

  private object DDLinkRequest {
    implicit val writes: OWrites[DDLinkRequest] = Json.writes[DDLinkRequest]
  }

  private final case class DDLinkResponse(journeyId: String, nextUrl: String)

  private object DDLinkResponse {
    implicit val reads: Reads[DDLinkResponse] = Json.reads[DDLinkResponse]
  }
}
