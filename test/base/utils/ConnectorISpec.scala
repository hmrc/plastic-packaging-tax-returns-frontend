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

package base.utils

import com.codahale.metrics.SharedMetricRegistries
import uk.gov.hmrc.play.bootstrap.metrics.Metrics
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.DefaultAwaitTimeout
import play.api.{Application, inject}
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient

import scala.concurrent.ExecutionContext

class ConnectorISpec extends WiremockTestServer with GuiceOneAppPerSuite with DefaultAwaitTimeout {

  def overrideConfig: Map[String, Any] =
    Map(
      "microservice.services.plastic-packaging-tax-returns.host" -> wireHost,
      "microservice.services.plastic-packaging-tax-returns.port" -> wirePort,
      "microservice.services.pay-api.host"                       -> wireHost,
      "microservice.services.pay-api.port"                       -> wirePort,
      "microservice.services.direct-debit.host"                  -> wireHost,
      "microservice.services.direct-debit.port"                  -> wirePort
    )

  override def fakeApplication(): Application = {
    startWireMockServer()
    SharedMetricRegistries.clear()
    new GuiceApplicationBuilder()
      .overrides(inject.bind[SessionRepository].toInstance(mock[SessionRepository]))
      .configure(overrideConfig).build()
  }

  protected implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  protected implicit val hc: HeaderCarrier    = HeaderCarrier()
  protected val httpClient: DefaultHttpClient = app.injector.instanceOf[DefaultHttpClient]
  protected val metrics: Metrics              = app.injector.instanceOf[Metrics]
}
