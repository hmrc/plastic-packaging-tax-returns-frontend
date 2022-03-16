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
import play.api.i18n.Messages
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.plasticpackagingtax.returns.config.AppConfig
import uk.gov.hmrc.plasticpackagingtax.returns.models.financials.PPTFinancials

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FinancialsConnector @Inject() (
  httpClient: HttpClient,
  appConfig: AppConfig,
  metrics: Metrics
)(implicit ec: ExecutionContext)
    extends Logging {

  def getPaymentStatement(
    pptReferenceNumber: String
  )(implicit hc: HeaderCarrier, messages: Messages): Future[String] = {
    val timer = metrics.defaultRegistry.timer("ppt.financials.open.get.timer").time()
    httpClient.GET[PPTFinancials](appConfig.pptFinancialsUrl(pptReferenceNumber))
      .map {
        response =>
          logger.info(s"Retrieved open financials for ppt reference number [$pptReferenceNumber]")
          response.paymentStatement()
      }
      .andThen { case _ => timer.stop() }
      .recover {
        case _ =>
          messages("account.homePage.card.payments.error")
      }
  }

}
