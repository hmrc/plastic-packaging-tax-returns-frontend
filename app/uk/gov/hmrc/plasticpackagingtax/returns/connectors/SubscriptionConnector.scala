/*
 * Copyright 2021 HM Revenue & Customs
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
import play.api.Logger
import play.api.libs.json.Json.toJson
import uk.gov.hmrc.http.HttpReads.Implicits.readFromJson
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.plasticpackagingtax.returns.config.AppConfig
import uk.gov.hmrc.plasticpackagingtax.returns.models.subscription.subscriptionDisplay.SubscriptionDisplayResponse
import uk.gov.hmrc.plasticpackagingtax.returns.models.subscription.subscriptionUpdate.{
  SubscriptionUpdateRequest,
  SubscriptionUpdateResponse
}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubscriptionConnector @Inject() (
  httpClient: HttpClient,
  appConfig: AppConfig,
  metrics: Metrics
)(implicit ec: ExecutionContext) {

  private val logger = Logger(this.getClass)

  def get(
    pptReferenceNumber: String
  )(implicit hc: HeaderCarrier): Future[SubscriptionDisplayResponse] = {
    val timer = metrics.defaultRegistry.timer("ppt.subscription.get.timer").time()
    httpClient.GET[SubscriptionDisplayResponse](appConfig.pptSubscriptionUrl(pptReferenceNumber))
      .map {
        response =>
          logger.info(
            s"Retrieved subscription for ppt reference number [$pptReferenceNumber] had response [${toJson(response)}]"
          )
          response
      }
      .andThen { case _ => timer.stop() }
      .recover {
        case exception: Exception =>
          throw DownstreamServiceError(
            s"Failed to retrieve subscription details for PPTReference: [$pptReferenceNumber], error: [${exception.getMessage}]",
            exception
          )
      }
  }

  def update(pptReferenceNumber: String, updateRequest: SubscriptionUpdateRequest)(implicit
    hc: HeaderCarrier
  ): Future[SubscriptionUpdateResponse] = {
    val timer = metrics.defaultRegistry.timer("ppt.subscription.update.timer").time()
    httpClient.PUT[SubscriptionUpdateRequest, SubscriptionUpdateResponse](
      appConfig.pptSubscriptionUrl(pptReferenceNumber),
      updateRequest
    ).map {
      response =>
        logger.info(
          s"Update subscription for ppt reference number [$pptReferenceNumber] had response [${toJson(response)}]"
        )
        response
    }
      .andThen { case _ => timer.stop() }
      .recover {
        case exception: Exception =>
          throw DownstreamServiceError(
            s"Failed to update subscription details for PPTReference: [$pptReferenceNumber], error: [${exception.getMessage}]",
            exception
          )
      }
  }

}
