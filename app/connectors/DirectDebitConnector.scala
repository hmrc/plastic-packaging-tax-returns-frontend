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
import play.api.Logging
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DirectDebitConnector @Inject()
(
  httpClient: HttpClient,
  appConfig: FrontendAppConfig,
  metrics: Metrics
)(implicit ec: ExecutionContext)
  extends Logging {
  def getDirectDebitMandate(
                             pptReferenceNumber: String)
                           (implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val timer = metrics.defaultRegistry.timer("ppt.financials.open.get.timer").time()

    httpClient.GET[HttpResponse](appConfig.pptDirectDebitUrl(pptReferenceNumber))
      .map {
        response =>
          logger.info(s"Retrieved direct debit mandate for ppt reference number [$pptReferenceNumber]")
          response
      }
      .andThen { case _ => timer.stop() }
      .recover {
        case exception: Exception =>
          throw DownstreamServiceError("SOME ERROR",
            exception)
      }
  }
}

