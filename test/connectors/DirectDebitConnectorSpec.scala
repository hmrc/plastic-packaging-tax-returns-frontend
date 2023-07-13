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
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.Json
import play.api.test.Helpers.await

class DirectDebitConnectorSpec extends ConnectorISpec {

  lazy val connector: DirectDebitConnector = app.injector.instanceOf[DirectDebitConnector]

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    startWireMockServer()
  }

  override protected def afterAll(): Unit = {
    stopWireMockServer()
    super.afterAll()
  }

  "getDirectDebitLink" must {
    "return the link" in {
      stubDDFor(OK,  Json.obj("journeyId" -> "id", "nextUrl" -> "/expected-url").toString())

      val result = await(connector.getDirectDebitLink("ppt-ref", "/home-url"))

      result mustBe "/expected-url"
    }

    "error when fails" in {
      stubDDFor(INTERNAL_SERVER_ERROR,  Json.obj("go" -> "boom").toString())

      val ex = intercept[DownstreamServiceError](await(connector.getDirectDebitLink("ppt-ref", "/home-url")))

      ex.getMessage mustBe "Error trying to get Direct Debit link"

    }
  }

  def stubDDFor(status: Int, body: String) = {
    stubFor(
      WireMock.post("/direct-debit-backend/ppt-homepage/ppt/journey/start")
        .willReturn(
          aResponse()
            .withStatus(status)
            .withBody(body)
        )
    )
  }

}
