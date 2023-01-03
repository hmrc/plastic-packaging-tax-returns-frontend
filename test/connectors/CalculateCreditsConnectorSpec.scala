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

package connectors

import base.utils.ConnectorISpec
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import models.CreditBalance
import org.scalatest.concurrent.ScalaFutures
import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.Helpers.await

import java.util.UUID

class CalculateCreditsConnectorSpec extends ConnectorISpec with ScalaFutures {

  lazy val connector: CalculateCreditsConnector = app.injector.instanceOf[CalculateCreditsConnector]
  private val pptReference = UUID.randomUUID().toString

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    startWireMockServer
  }

  override protected def afterAll(): Unit = {
    stopWireMockServer
    super.afterAll()
  }

  "Get" should {
    "return a credit for a pptReference" in {
      val expectedCredit = CreditBalance(10, 5, 200, true)
      stubCalculateCreditApi(
        Status.OK,
        pptReference,
        Json.toJsObject(expectedCredit).toString
      )

      val res = await(connector.get(pptReference))

      res mustBe Right(expectedCredit)
    }

    "return an error" in {
      stubCalculateCreditApi(
        Status.INTERNAL_SERVER_ERROR,
        pptReference
      )

      val res = await(connector.get(pptReference))

      assert(res.left.get.isInstanceOf[DownstreamServiceError])
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
