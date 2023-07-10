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
import models.CreditBalance
import play.api.Logging
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CalculateCreditsConnector @Inject()(httpClient: HttpClient, appConfig: FrontendAppConfig, metrics: Metrics)(implicit ec: ExecutionContext) extends Logging {

  def getEventually(pptReferenceNumber: String)(implicit hc: HeaderCarrier): Future[CreditBalance] = {

    val timer = metrics.defaultRegistry.timer("ppt.exportcredits.open.get.timer").time()
    httpClient.GET[CreditBalance](appConfig.pptCalculateCreditsUrl(pptReferenceNumber))
      .andThen { case _ => timer.stop() }
      .recover {
        case ex: Exception => throw DownstreamServiceError(s"Failed to calculate credits for ppt reference number" +
          s" [$pptReferenceNumber]", ex)
      }
  }

}
