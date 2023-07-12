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

package connectors

import com.kenshoo.play.metrics.Metrics
import config.FrontendAppConfig
import models.obligations.PPTObligations
import models.returns.TaxReturnObligation
import play.api.Logging
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReadsInstances}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ObligationsConnector @Inject() (
  httpClient: HttpClient,
  appConfig: FrontendAppConfig,
  metrics: Metrics
)(implicit ec: ExecutionContext)
    extends Logging with HttpReadsInstances {

  def getOpen(pptReferenceNumber: String)(implicit hc: HeaderCarrier): Future[PPTObligations] = {
    val timer = metrics.defaultRegistry.timer("ppt.obligations.open.get.timer").time()
    httpClient.GET[PPTObligations](appConfig.pptOpenObligationUrl(pptReferenceNumber))
      .map {
        response =>
          logger.info(s"Retrieved open obligations for ppt reference number [$pptReferenceNumber]")
          response
      }
      .andThen { case _ => timer.stop() }
      .recover {
        case exception: Exception =>
          throw DownstreamServiceError(
            s"Failed to retrieve open obligations for PPTReference: [$pptReferenceNumber], error: [${exception.getMessage}]",
            exception
          )
      }
  }

  def getFulfilled(pptReferenceNumber: String)(implicit hc: HeaderCarrier): Future[Seq[TaxReturnObligation]] = {
    val timer = metrics.defaultRegistry.timer("ppt.obligations.fulfilled.get.timer").time()
    httpClient.GET[Seq[TaxReturnObligation]](appConfig.pptFulfilledObligationUrl(pptReferenceNumber))
      .map {
        response =>
          logger.info(s"Retrieved fulfilled obligations for ppt reference number [$pptReferenceNumber]")
          response
      }
      .andThen { case _ => timer.stop() }
      .recover {
        case exception: Exception =>
          throw DownstreamServiceError(
            s"Failed to retrieve fulfilled obligations for PPTReference: [$pptReferenceNumber], error: [${exception.getMessage}]",
            exception
          )
      }
  }

}
