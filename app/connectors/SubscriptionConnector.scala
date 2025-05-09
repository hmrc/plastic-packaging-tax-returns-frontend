/*
 * Copyright 2025 HM Revenue & Customs
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

import uk.gov.hmrc.play.bootstrap.metrics.Metrics
import config.FrontendAppConfig
import models.EisFailure
import models.subscription.subscriptionDisplay.SubscriptionDisplayResponse
import play.api.http.Status
import play.api.http.Status.OK
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubscriptionConnector @Inject() (
  httpClient: HttpClient,
  appConfig: FrontendAppConfig,
  metrics: Metrics
)(implicit ec: ExecutionContext) {

  def get(
    pptReferenceNumber: String
  )(implicit hc: HeaderCarrier): Future[Either[EisFailure, SubscriptionDisplayResponse]] = {

    val timer = metrics.defaultRegistry.timer("ppt.subscription.get.timer").time()
    httpClient.GET[HttpResponse](appConfig.pptSubscriptionUrl(pptReferenceNumber))
      .map { response =>
        if (Status.isSuccessful(response.status)) {
          Right(response.json.as[SubscriptionDisplayResponse])
        } else {
          Left(response.json.as[EisFailure])
        }
      }
      .andThen { case _ => timer.stop() }
  }

  def changeGroupLead(pptReferenceNumber: String)(implicit
    hc: HeaderCarrier
  ): Future[HttpResponse] = {
    httpClient.POSTEmpty[HttpResponse](
      appConfig.pptChangeGroupLeadUrl(pptReferenceNumber)
    ).map { response =>
      response.status match {
        case OK => response
        case _ =>
          throw DownstreamServiceError(
            s"Failed to update subscription details for PPTReference: [$pptReferenceNumber], error: [${response.body}]",
            new Exception()
          )
      }
    }
  }

}
