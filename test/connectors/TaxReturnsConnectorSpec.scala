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
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, status}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import controllers.helpers.TaxReturnBuilder
import models.returns.{IdDetails, ReturnDisplayApi, ReturnDisplayDetails, TaxReturn}
import org.scalatest.EitherValues
import org.scalatest.concurrent.ScalaFutures
import play.api.http.Status
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

  // TODO - we need to look at adding specs here.
  //  - What do we expect back from this endpoint?
  //  - What do we want to do with it?
  //
  //  # Return Display API
  //  GET         /returns-submission/:pptReference/:periodKey      uk.gov.hmrc.plasticpackagingtaxreturns.controllers.ReturnsSubmissionController.get(pptReference: String, periodKey: String)
  //
  //  # EIS Return Amend
  //  PUT        /returns-amend/:pptReference                      uk.gov.hmrc.plasticpackagingtaxreturns.controllers.ReturnsSubmissionController.amend(pptReference: String)
  val pptReference = UUID.randomUUID().toString

  "create tax return" should {
    "return success response" when {
      "getting a return" in {
        val x: BigDecimal = 0.1
        val expectedResult: ReturnDisplayApi = ReturnDisplayApi("", IdDetails(pptReference, "subId"), None, (ReturnDisplayDetails(x, x, x, x, x, x, x, x, x, x)))

        givenGetReturnsEndpointReturns(Status.OK, pptReference, body = "{}")
        val res: Either[ServiceError, ReturnDisplayApi] = await(connector.get(pptReference, "00xx"))

        res.right.get mustBe expectedResult
      }
      "a return is submitted" in {
        givenReturnsSubmissionEndpointReturns(Status.OK,
          pptReference,
          body = "{}"
        )

        val res: Either[ServiceError, Unit] = await(connector.submit(aTaxReturn(withId(pptReference))))

        res.isRight mustBe true
      }
      "a return is amended" in {
        givenReturnsAmendmentEndpointReturns(Status.OK,
          pptReference,
          body = "{}"
        )

        val res: Either[ServiceError, Unit] = await(connector.amend(aTaxReturn(withId(pptReference))))

        res.isRight mustBe true
      }

      "return a left" when {
        "submit response is impassible" in {
          givenReturnsSubmissionEndpointReturns(Status.OK,
            pptReference,
            body = "{"
          )

          val res: Either[ServiceError, Unit] = await(connector.submit(aTaxReturn(withId(pptReference))))

          assert(res.left.get.isInstanceOf[DownstreamServiceError])
        }
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
                                             body: String = ""
                                            ): StubMapping =
    stubFor(
      WireMock.get(s"/returns/$pptReference")
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