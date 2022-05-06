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

import base.utils.ConnectorISpec
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import play.api.http.Status.OK
import play.api.libs.json.Json
import play.api.test.Helpers.{await, status}

class DirectDebitConnectorSpec extends ConnectorISpec {

  lazy val connector: DirectDebitConnector = app.injector.instanceOf[DirectDebitConnector]

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    WireMock.configureFor(wireHost, wirePort)
    wireMockServer.start()
  }

  override protected def afterAll(): Unit = {
    wireMockServer.stop()
    super.afterAll()
  }
//TODO: Reimpliment unit tests for DD

  private def stubEndPointForDirectDebit
  (
    status: Int,
    pptReference: String,
    body: String = ""
  ) =
    stubFor(
      WireMock.get(s"/direct-debit/$pptReference")
        .willReturn(
          aResponse()
            .withStatus(status)
            .withBody(body)
        )
    )

}
