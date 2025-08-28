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

import org.apache.pekko.util.Timeout
import base.utils.ConnectorISpec
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import models.{CreditBalance, TaxablePlastic}
import org.scalatest.concurrent.ScalaFutures
import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.Helpers.await

import java.util.UUID
import scala.concurrent.Await

class CalculateCreditsConnectorSpec extends ConnectorISpec with ScalaFutures {

  lazy val connector: CalculateCreditsConnector = app.injector.instanceOf[CalculateCreditsConnector]
  private val pptReference                      = UUID.randomUUID().toString

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    startWireMockServer()
  }

  override protected def afterAll(): Unit = {
    stopWireMockServer()
    super.afterAll()
  }

  "it" should {

    "return a credit for a pptReference" in {
      val creditBalance = CreditBalance(10, 5, 200, true, Map("a-key" -> TaxablePlastic(1, 2, 3)))

      stubCalculateCreditApi(
        Status.OK,
        pptReference,
        Json.toJsObject(creditBalance).toString
      )

      await(connector.getEventually(pptReference)) mustBe creditBalance
    }

    "return an error" in {
      stubCalculateCreditApi(
        Status.INTERNAL_SERVER_ERROR,
        pptReference
      )

      val eventualResult   = connector.getEventually(pptReference)
      val timeout: Timeout = implicitly
      Await.ready(eventualResult, timeout.duration)

      eventualResult.failed.futureValue mustBe a[DownstreamServiceError]
    }
  }

  private def stubCalculateCreditApi(status: Int, pptReference: String, body: String = "") =
    stubFor(
      WireMock.get(s"/credits/calculate/$pptReference")
        .willReturn(
          aResponse()
            .withStatus(status)
            .withBody(body)
        )
    )
}
