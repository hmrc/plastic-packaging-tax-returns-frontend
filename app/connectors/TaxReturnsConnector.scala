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
import models.returns.{ReturnDisplayApi, TaxReturn}
import play.api.Logger
import play.api.libs.json.{JsString, JsValue}
import uk.gov.hmrc.http.HttpReads.Implicits.readFromJson
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TaxReturnsConnector @Inject()(
                                     httpClient: HttpClient,
                                     appConfig: FrontendAppConfig,
                                     metrics: Metrics
                                   )(implicit ec: ExecutionContext) {

  private val logger = Logger(this.getClass)

  def get(userId: String, periodKey: String)(implicit
                                             hc: HeaderCarrier
  ): Future[Either[ServiceError, ReturnDisplayApi]] = {
    val url = appConfig.pptReturnSubmissionUrl(userId) + "/" + periodKey
    val timer = metrics.defaultRegistry.timer("ppt.returns.get.timer").time()
    httpClient.GET[ReturnDisplayApi](url)
      .andThen { case _ => timer.stop() }
      .map(taxReturn => Right(taxReturn))
      .recover {
        case ex: Exception =>
          Left(DownstreamServiceError(s"Failed to get return, error: ${ex.getMessage}", ex))
      }
  }

  def submit(payload: TaxReturn)(implicit hc: HeaderCarrier): Future[Either[ServiceError, Option[String]]] = {
    val timer = metrics.defaultRegistry.timer("ppt.returns.submit.timer").time()
    val pptReference = payload.id

    httpClient.POST[TaxReturn, JsValue](appConfig.pptReturnSubmissionUrl(pptReference), payload)
      .andThen { case _ => timer.stop() }
      .map { returnJson =>
        val chargeReference = (returnJson \ "chargeDetails" \ "chargeReference").asOpt[JsString].map(_.value)
        logger.info(s"Submitted ppt tax returns for id [$pptReference] with charge ref: $chargeReference")
        Right(chargeReference)
      }
      .recover {
        case ex: Exception =>
          Left(DownstreamServiceError(s"Failed to submit return, error: ${ex.getMessage}", ex))
      }
  }


  def amend(payload: TaxReturn, submissionId: String)(implicit hc: HeaderCarrier): Future[Either[ServiceError, Option[String]]] = {
    val timer = metrics.defaultRegistry.timer("ppt.returns.submit.timer").time()
    val pptReference = payload.id
    val url = appConfig.pptReturnAmendUrl(pptReference, submissionId)

    httpClient.PUT[TaxReturn, JsValue](url, payload)
      .andThen { case _ => timer.stop() }
      .map { returnJson =>
        val chargeReference = (returnJson \ "chargeDetails" \ "chargeReference").asOpt[JsString].map(_.value)
        logger.info(s"Submitted ppt amendment for id [$pptReference] with charge ref: $chargeReference")
        Right(chargeReference)
      }
      .recover {
        case ex: Exception =>
          Left(
            DownstreamServiceError(s"Failed to submit return amendment, error: ${ex.getMessage}", ex
            )
          )
      }
  }

}
