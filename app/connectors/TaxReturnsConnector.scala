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
import models.returns.TaxReturn
import play.api.Logger
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.HttpReads.Implicits.readFromJson
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TaxReturnsConnector @Inject() (
  httpClient: HttpClient,
  appConfig: FrontendAppConfig,
  metrics: Metrics
)(implicit ec: ExecutionContext) {

  private val logger = Logger(this.getClass)

  def submit(payload: TaxReturn)(implicit hc: HeaderCarrier): Future[Either[ServiceError, Unit]] = {
    val timer        = metrics.defaultRegistry.timer("ppt.returns.submit.timer").time()
    val pptReference = payload.id
    httpClient.POST[String, JsValue](appConfig.pptReturnSubmissionUrl(pptReference), payload.id)
      .andThen { case _ => timer.stop() }
      .map { _ =>
        logger.info(s"Submitted ppt tax returns for id [$pptReference]")
        Right()
      }
      .recover {
        case ex: Exception =>
          Left(DownstreamServiceError(s"Failed to submit return, error: ${ex.getMessage}", ex))
      }
  }

}
