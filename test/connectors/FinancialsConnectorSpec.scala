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
import models.financials.PPTFinancials
import org.scalatest.EitherValues
import org.scalatest.concurrent.ScalaFutures
import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.Helpers.await

import java.util.UUID

class FinancialsConnectorSpec extends ConnectorISpec with ScalaFutures with EitherValues {

  lazy val connector: FinancialsConnector = app.injector.instanceOf[FinancialsConnector]

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    startWireMockServer()
  }

  override protected def afterAll(): Unit = {
    stopWireMockServer()
    super.afterAll()
  }

  "getPaymentLink" should {
    "return success response" when {
      "retrieving existing financial details" in {
        val pptReference       = UUID.randomUUID().toString

        givenPostPaymentsUrl(Status.CREATED, Json.obj("journeyId" -> "id", "nextUrl" -> "/expected").toString())

        val res = await(connector.getPaymentLink(pptReference, 0, "/"))

        res mustBe "/expected"
      }
    }

    "throws exception" when {

      "service returns non success status code" in {
        val pptReference = UUID.randomUUID().toString
        givenPostPaymentsUrl(Status.BAD_REQUEST, pptReference)

        intercept[DownstreamServiceError] {
          await(connector.getPaymentStatement(pptReference))
        }
      }

      "service returns invalid response" in {
        val pptReference = UUID.randomUUID().toString
        givenPostPaymentsUrl(Status.CREATED, "someRubbish")

        intercept[DownstreamServiceError] {
          await(connector.getPaymentStatement(pptReference))
        }
      }
    }
  }

  "get financials details" should {

    "return success response" when {

      "retrieving existing financial details" in {

        val pptReference       = UUID.randomUUID().toString
        val expectedFinancials = PPTFinancials(None, None, None)
        givenGetSubscriptionEndpointReturns(Status.OK,
                                            pptReference,
                                            Json.toJsObject(expectedFinancials).toString
        )

        val res = await(connector.getPaymentStatement(pptReference))

        res mustBe expectedFinancials

      }
    }

    "throws exception" when {

      "service returns non success status code" in {
        val pptReference = UUID.randomUUID().toString
        givenGetSubscriptionEndpointReturns(Status.BAD_REQUEST, pptReference)

        intercept[DownstreamServiceError] {
          await(connector.getPaymentStatement(pptReference))
        }
      }

      "service returns invalid response" in {
        val pptReference = UUID.randomUUID().toString
        givenGetSubscriptionEndpointReturns(Status.CREATED, pptReference, "someRubbish")

        intercept[DownstreamServiceError] {
          await(connector.getPaymentStatement(pptReference))
        }
      }
    }
  }

  private def givenPostPaymentsUrl(status: Int, body: String) =
    stubFor(
      WireMock.post("/pay-api/plastic-packaging-tax/journey/start")
        .willReturn(
          aResponse()
            .withStatus(status)
            .withBody(body)
        )
    )

  private def givenGetSubscriptionEndpointReturns(
    status: Int,
    pptReference: String,
    body: String = ""
  ) =
    stubFor(
      WireMock.get(s"/financials/open/$pptReference")
        .willReturn(
          aResponse()
            .withStatus(status)
            .withBody(body)
        )
    )

}
