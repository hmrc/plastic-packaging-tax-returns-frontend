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

package uk.gov.hmrc.plasticpackagingtax.returns.connectors

import com.kenshoo.play.metrics.Metrics
import play.api.Logging
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.plasticpackagingtax.returns.config.AppConfig
import uk.gov.hmrc.plasticpackagingtax.returns.models.exportcredits.ExportCreditBalance

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ExportCreditsConnector @Inject() (
  httpClient: HttpClient,
  appConfig: AppConfig,
  metrics: Metrics
)(implicit ec: ExecutionContext)
    extends Logging {

  def get(pptReferenceNumber: String, fromDate: LocalDate, toDate: LocalDate)(implicit
    hc: HeaderCarrier
  ): Future[Either[ServiceError, ExportCreditBalance]] = {
    val timer = metrics.defaultRegistry.timer("ppt.exportcredits.open.get.timer").time()
    httpClient.GET[ExportCreditBalance](
      appConfig.pptExportCreditsUrl(pptReferenceNumber, fromDate, toDate)
    )
      .map {
        response =>
          logger.info(
            s"Retrieved export credits for ppt reference number [$pptReferenceNumber] with fromDate $fromDate and toDate $toDate"
          )
          Right(response)
      }
      .andThen { case _ => timer.stop() }
      .recover {
        case ex: Exception =>
          Left(
            DownstreamServiceError(
              s"Failed to retrieve export credits for ppt reference number [$pptReferenceNumber] with fromDate $fromDate and toDate $toDate, error: [${ex.getMessage}]",
              ex
            )
          )
      }
  }

}
