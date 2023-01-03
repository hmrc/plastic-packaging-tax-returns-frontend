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
import models.obligations.PPTObligations
import models.returns.TaxReturnObligation
import org.scalatest.EitherValues
import org.scalatest.concurrent.ScalaFutures
import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.Helpers.await

import java.time.LocalDate
import java.util.UUID

class ObligationsConnectorSpec extends ConnectorISpec with ScalaFutures with EitherValues {

  lazy val connector: ObligationsConnector = app.injector.instanceOf[ObligationsConnector]

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    startWireMockServer
  }

  override protected def afterAll(): Unit = {
    stopWireMockServer
    super.afterAll()
  }

  "get open obligation details" should {

    "return success response" when {

      "retrieving existing obligation details" in {

        val pptReference        = UUID.randomUUID().toString
        val expectedObligations = PPTObligations(None, None, 0, true, false)

        givenGetSubscriptionEndpointReturns(
          Status.OK,
          pptReference,
          Json.toJsObject(expectedObligations).toString,
          "open"
        )

        val res = await(connector.getOpen(pptReference))

        res mustBe expectedObligations

      }
    }

    "throws exception" when {

      "service returns non success status code" in {
        val pptReference = UUID.randomUUID().toString
        givenGetSubscriptionEndpointReturns(Status.BAD_REQUEST, pptReference, "", "open")

        intercept[DownstreamServiceError] {
          await(connector.getOpen(pptReference))
        }
      }

      "service returns invalid response" in {
        val pptReference = UUID.randomUUID().toString
        givenGetSubscriptionEndpointReturns(Status.CREATED, pptReference, "someRubbish", "open")

        intercept[DownstreamServiceError] {
          await(connector.getOpen(pptReference))
        }
      }
    }
  }

  "get fulfilled obligation details" should {

    "return success response" when {

      "retrieving existing obligation details" in {

        val obligations =
          Seq(
            TaxReturnObligation(
              LocalDate.now(),
              LocalDate.now().plusMonths(3),
              LocalDate.now().plusMonths(3),
              "PK1"
            ),
            TaxReturnObligation(
              LocalDate.now().plusMonths(3),
              LocalDate.now().plusMonths(6),
              LocalDate.now().plusMonths(6),
              "PK2"
            )
          )

        val pptReference        = UUID.randomUUID().toString
        val expectedObligations = obligations
        givenGetSubscriptionEndpointReturns(
          Status.OK,
          pptReference,
          Json.toJson(expectedObligations).toString,
          "fulfilled"
        )

        val res = await(connector.getFulfilled(pptReference))

        res mustBe expectedObligations

      }
    }

    "throws exception" when {

      "service returns non success status code" in {
        val pptReference = UUID.randomUUID().toString
        givenGetSubscriptionEndpointReturns(Status.BAD_REQUEST, pptReference, "", "fulfilled")

        intercept[DownstreamServiceError] {
          await(connector.getFulfilled(pptReference))
        }
      }

      "service returns invalid response" in {
        val pptReference = UUID.randomUUID().toString
        givenGetSubscriptionEndpointReturns(
          Status.CREATED,
          pptReference,
          "someRubbish",
          "fulfilled"
        )

        intercept[DownstreamServiceError] {
          await(connector.getFulfilled(pptReference))
        }
      }
    }
  }

  private def givenGetSubscriptionEndpointReturns(
    status: Int,
    pptReference: String,
    body: String = "",
    obligationStatus: String
  ) =
    stubFor(
      WireMock.get(s"/obligations/$obligationStatus/$pptReference")
        .willReturn(
          aResponse()
            .withStatus(status)
            .withBody(body)
        )
    )

}
