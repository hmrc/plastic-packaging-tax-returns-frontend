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
import models.returns.{AmendsCalculations, Calculations, DDInProgressApi, ReturnDisplayApi}
import play.api.Logger
import play.api.http.Status
import play.api.libs.json.{JsString, JsValue}
import uk.gov.hmrc.http.HttpReads.Implicits.readFromJson
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse, Upstream4xxResponse}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

case object AlreadySubmitted

@Singleton
class TaxReturnsConnector @Inject()(
  httpClient: HttpClient,
  frontendAppConfig: FrontendAppConfig,
  metrics: Metrics
)(implicit ec: ExecutionContext) {

  private val logger = Logger(this.getClass)

  def get(userId: String, periodKey: String)(implicit hc: HeaderCarrier): Future[ReturnDisplayApi] = {
    val url = frontendAppConfig.pptReturnSubmissionUrl(userId) + "/" + periodKey
    val timer = metrics.defaultRegistry.timer("ppt.returns.get.timer").time()
    httpClient.GET[ReturnDisplayApi](url)
      .andThen { case _ => timer.stop() }
      .recover {
        case ex: Exception =>
          throw DownstreamServiceError(s"Failed to get return, error: ${ex.getMessage}", ex)
      }
  }

  def ddInProgress(pptReference: String, periodKey: String)(implicit hc: HeaderCarrier): Future[DDInProgressApi] =
    httpClient.GET[DDInProgressApi](frontendAppConfig.pptDDInProgress(pptReference, periodKey))

  def getCalculationAmends(pptReference: String)(implicit hc: HeaderCarrier): Future[Either[ServiceError, AmendsCalculations]] =
    httpClient.GET[AmendsCalculations](frontendAppConfig.pptAmendsCalculationUrl(pptReference)).
      map(Right.apply)
      .recover {
        case ex: Exception =>
          Left(DownstreamServiceError(s"Failed to get calculations, error: ${ex.getMessage}", ex))
      }

  def getCalculationReturns(pptReference: String)(implicit hc: HeaderCarrier): Future[Either[ServiceError, Calculations]] =
    httpClient.GET[Calculations](frontendAppConfig.pptReturnsCalculationUrl(pptReference)).
      map(Right.apply)
      .recover {
        case ex: Exception =>
          Left(DownstreamServiceError(s"Failed to get calculations, error: ${ex.getMessage}", ex))
      }

  def submit(pptReference: String)(implicit hc: HeaderCarrier): Future[Either[AlreadySubmitted.type, Option[String]]] = {
    val timer = metrics.defaultRegistry.timer("ppt.returns.submit.timer").time()

    httpClient.GET[JsValue](frontendAppConfig.pptReturnSubmissionUrl(pptReference))
      .andThen { case _ => timer.stop() }
      .map { returnJson =>
        val chargeReference = (returnJson \ "chargeDetails" \ "chargeReference").asOpt[JsString].map(_.value)
        logger.info(s"Submitted ppt tax returns for id [$pptReference] with charge ref: $chargeReference")
        Right(chargeReference)
      }
      .recover {
        case exception: Upstream4xxResponse if exception.statusCode == Status.EXPECTATION_FAILED 
            || exception.statusCode == Status.UNPROCESSABLE_ENTITY => Left(AlreadySubmitted)
        case ex: Exception => 
          throw DownstreamServiceError(s"Failed to submit return, error: ${ex.getMessage}", ex)
      }
  }

  def amend(pptReference: String)(implicit hc: HeaderCarrier): Future[Option[String]] = {
    val timer = metrics.defaultRegistry.timer("ppt.returns.submit.timer").time()

    httpClient.POSTEmpty[HttpResponse](frontendAppConfig.pptReturnAmendUrl(pptReference))
      .andThen { case _ => timer.stop() }
      .map { response =>
        val chargeReference = (response.json \ "chargeDetails" \ "chargeReference").asOpt[JsString].map(_.value)
        logger.info(s"Submitted ppt amendment for id [$pptReference] with charge ref: $chargeReference")
        chargeReference
      }
      .recover {
        case ex: Exception =>
          throw DownstreamServiceError(s"Failed to submit return amendment, error: ${ex.getMessage}", ex)
      }
  }

}
