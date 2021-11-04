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
import uk.gov.hmrc.http.HttpReads.Implicits.readFromJson
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.plasticpackagingtax.returns.config.AppConfig
import uk.gov.hmrc.plasticpackagingtax.returns.models.domain.TaxReturn

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TaxReturnsConnector @Inject() (
  httpClient: HttpClient,
  appConfig: AppConfig,
  metrics: Metrics
)(implicit ec: ExecutionContext) {

  private val logger = Logger(this.getClass)

  def find(
    id: String
  )(implicit hc: HeaderCarrier): Future[Either[ServiceError, Option[TaxReturn]]] = {
    import uk.gov.hmrc.http.HttpReads.Implicits.readOptionOfNotFound
    val timer = metrics.defaultRegistry.timer("ppt.returns.find.timer").time()
    httpClient.GET[Option[TaxReturn]](appConfig.pptReturnUrl(id))
      .andThen { case _ => timer.stop() }
      .map {
        resp =>
          logger.info(s"Found ppt tax returns for id [$id] ")
          Right(resp.map(_.toTaxReturn))
      }
      .recover {
        case ex: Exception =>
          Left(
            DownstreamServiceError(
              s"Failed to retrieve return for id [$id], error: ${ex.getMessage}",
              ex
            )
          )
      }
  }

  def create(
    payload: TaxReturn
  )(implicit hc: HeaderCarrier): Future[Either[ServiceError, TaxReturn]] = {
    val timer = metrics.defaultRegistry.timer("ppt.returns.create.timer").time()
    httpClient.POST[TaxReturn, TaxReturn](appConfig.pptReturnsUrl, payload)
      .andThen { case _ => timer.stop() }
      .map {
        response =>
          logger.info(s"Create ppt tax returns with id [${response.id}]")
          Right(response.toTaxReturn)
      }
      .recover {
        case ex: Exception =>
          Left(DownstreamServiceError(s"Failed to create return, error: ${ex.getMessage}", ex))
      }
  }

  def update(
    payload: TaxReturn
  )(implicit hc: HeaderCarrier): Future[Either[ServiceError, TaxReturn]] = {
    val timer = metrics.defaultRegistry.timer("ppt.returns.update.timer").time()
    httpClient.PUT[TaxReturn, TaxReturn](appConfig.pptReturnUrl(payload.id), payload)
      .andThen { case _ => timer.stop() }
      .map { response =>
        logger.info(s"Updated ppt tax returns for id [${payload.id}]")
        Right(response.toTaxReturn)
      }
      .recover {
        case ex: Exception =>
          Left(DownstreamServiceError(s"Failed to update return, error: ${ex.getMessage}", ex))
      }
  }

}
