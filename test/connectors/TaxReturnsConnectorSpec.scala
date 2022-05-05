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
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import controllers.helpers.TaxReturnBuilder
import models.returns.{IdDetails, ReturnDisplayApi, ReturnDisplayDetails}
import org.scalatest.EitherValues
import org.scalatest.concurrent.ScalaFutures
import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.Helpers.await

import java.util.UUID

class TaxReturnsConnectorSpec
  extends ConnectorISpec with ScalaFutures with EitherValues with TaxReturnBuilder {

  lazy val connector: TaxReturnsConnector = app.injector.instanceOf[TaxReturnsConnector]

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    WireMock.configureFor(wireHost, wirePort)
    wireMockServer.start()
  }

  override protected def afterAll(): Unit = {
    wireMockServer.stop()
    super.afterAll()
  }

  val pptReference = UUID.randomUUID().toString

  "Tax Returns Connector" should {

    "get correctly" in {

      val x: BigDecimal = 0.1
      val expectedResult: ReturnDisplayApi =
        ReturnDisplayApi(
          "", IdDetails(pptReference, "subId"), None, ReturnDisplayDetails(x, x, x, x, x, x, x, x, x, x)
        )

      givenGetReturnsEndpointReturns(
        Status.OK, pptReference, "00xx", body = Json.toJson(expectedResult).toString
      )

      val res: Either[ServiceError, ReturnDisplayApi] = await(connector.get(pptReference, "00xx"))

      res.right.get mustBe expectedResult

    }

    "submit correctly" when {
      "there is a charge reference" in {

        givenReturnsSubmissionEndpointReturns(Status.OK,
          pptReference,
          body = """{"chargeDetails": {"chargeReference": "PANTESTPAN"}}"""
        )

        val res: Either[ServiceError, Option[String]] = await(connector.submit(aTaxReturn(withId(pptReference))))

        res.isRight mustBe true
        res.value mustBe Some("PANTESTPAN")

      }
      "there is no charge reference" in {

        givenReturnsSubmissionEndpointReturns(Status.OK,
          pptReference,
          body = """{"chargeDetails": null}"""
        )

        val res: Either[ServiceError, Option[String]] = await(connector.submit(aTaxReturn(withId(pptReference))))

        res.isRight mustBe true
        res.value mustBe None
      }
    }

    "Amend correctly" in {

      givenReturnsAmendmentEndpointReturns(Status.OK,
        pptReference,
        body = "{}"
      )

      val res: Either[ServiceError, Unit] = await(connector.amend(aTaxReturn(withId(pptReference))))

      res.isRight mustBe true

    }

    "return a left (error)" when {

      "get response is impassible" in {

        givenGetReturnsEndpointReturns(
          Status.OK, pptReference, "00xx", body = "{"
        )

        val res: Either[ServiceError, ReturnDisplayApi] = await(connector.get(pptReference, "00xx"))

        assert(res.left.get.isInstanceOf[DownstreamServiceError])

      }

      "submit response is impassible" in {

        givenReturnsSubmissionEndpointReturns(Status.OK,
          pptReference,
          body = "{"
        )

        val res: Either[ServiceError, Option[String]] = await(connector.submit(aTaxReturn(withId(pptReference))))

        assert(res.left.get.isInstanceOf[DownstreamServiceError])

      }

      "amend response is impassible" in {

        givenReturnsAmendmentEndpointReturns(Status.OK,
          pptReference,
          body = "{"
        )

        val res: Either[ServiceError, Unit] = await(connector.amend(aTaxReturn(withId(pptReference))))

        assert(res.left.get.isInstanceOf[DownstreamServiceError])

      }
    }
  }

  private def givenGetReturnsEndpointReturns(status: Int,
                                             pptReference: String,
                                             periodKey: String,
                                             body: String = ""
                                            ): StubMapping =
    stubFor(
      WireMock.get(s"/returns-submission/$pptReference/$periodKey")
        .willReturn(
          aResponse()
            .withStatus(status)
            .withBody(body)
        )
    )

  private def givenReturnsSubmissionEndpointReturns(
                                                     status: Int,
                                                     pptReference: String,
                                                     body: String = ""
                                                   ): StubMapping =
    stubFor(
      WireMock.post(s"/returns-submission/$pptReference")
        .willReturn(
          aResponse()
            .withStatus(status)
            .withBody(body)
        )
    )

  private def givenReturnsAmendmentEndpointReturns(
                                                    status: Int,
                                                    pptReference: String,
                                                    body: String = ""
                                                  ): StubMapping =
    stubFor(
      WireMock.put(s"/returns-amend/$pptReference")
        .willReturn(
          aResponse()
            .withStatus(status)
            .withBody(body)
        )
    )
}