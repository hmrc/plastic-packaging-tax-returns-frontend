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
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import controllers.helpers.TaxReturnBuilder
import models.returns.{IdDetails, ReturnDisplayApi, ReturnDisplayDetails}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterEach, EitherValues}
import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.Helpers.await

class TaxReturnsConnectorSpec
  extends ConnectorISpec with ScalaFutures with EitherValues with TaxReturnBuilder with BeforeAndAfterEach {

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

  override def beforeEach(): Unit = {
    super.beforeEach()
    wireMockServer.resetAll()
  }

  private val pptReference = "XMPPT001"

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

    "submit correctly" in {

      givenReturnsSubmissionEndpointReturns(Status.OK,
        pptReference,
        body = "{}"
      )

      val res: Either[ServiceError, Unit] = await(connector.submit(aTaxReturn(withId(pptReference))))

      res.isRight mustBe true

    }

    "Amend correctly" in {
      wireMockServer.stubFor(put(anyUrl()).willReturn(ok().withBody("{}")))
      val result: Either[ServiceError, Unit] = await(connector.amend(aTaxReturn(withId("id-ref-10")), "submission-214"))
      result mustBe Right(())
      wireMockServer.verify(putRequestedFor(urlEqualTo(s"/returns-amend/id-ref-10/submission-214")))
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

        val res: Either[ServiceError, Unit] = await(connector.submit(aTaxReturn(withId(pptReference))))

        assert(res.left.get.isInstanceOf[DownstreamServiceError])

      }

      "amend response is impassible" in {

        givenReturnsAmendmentEndpointReturns(Status.OK,
          pptReference,
          body = "{"
        )

        val res: Either[ServiceError, Unit] = await(connector.amend(aTaxReturn(withId(pptReference)), "submission-214"))

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