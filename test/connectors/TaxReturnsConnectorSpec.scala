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
import models.returns.{IdDetails, ReturnDisplayApi, ReturnDisplayDetails}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.verbs.MustVerb
import org.scalatest.{BeforeAndAfterEach, EitherValues}
import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.Helpers.await

class TaxReturnsConnectorSpec extends ConnectorISpec with ScalaFutures with EitherValues with BeforeAndAfterEach 
  with MustVerb {

  lazy val connector: TaxReturnsConnector = app.injector.instanceOf[TaxReturnsConnector]

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    startWireMockServer
  }

  override protected def afterAll(): Unit = {
    stopWireMockServer
    super.afterAll()
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    resetWireMockServer
  }

  private val pptReference = "XMPPT001"

  "Tax Returns Connector" should {

    "get correctly" in {

      val bd: BigDecimal = 0.1
      val l: Long = 1
      val expectedResult: ReturnDisplayApi =
        ReturnDisplayApi(
          "", IdDetails(pptReference, "subId"), None, ReturnDisplayDetails(l, l, l, l, l, l, bd, bd, l, bd)
        )

      givenGetReturnsEndpointReturns(
        Status.OK, pptReference, "00xx", body = Json.toJson(expectedResult).toString
      )

      await(connector.get(pptReference, "00xx")) mustBe expectedResult
    }

    "submit correctly" when {
      "there is a charge reference" in {
        givenReturnsSubmissionEndpointReturns(Status.OK,
          pptReference,
          body = """{"chargeDetails": {"chargeReference": "PANTESTPAN"}}"""
        )
        await(connector.submit(pptReference)) mustBe Right(Some("PANTESTPAN"))
      }

      "there is no charge reference" in {
        givenReturnsSubmissionEndpointReturns(Status.OK,
          pptReference,
          body = """{"chargeDetails": null}"""
        )
        await(connector.submit(pptReference)) mustBe Right(None)
      }
      
      "the obligation is no longer open" in {
        givenReturnsSubmissionEndpointReturns(Status.EXPECTATION_FAILED, pptReference)
        await(connector.submit(pptReference)) mustBe Left(AlreadySubmitted)
      }

      "etmp says the return has already been submitted" in {
        givenReturnsSubmissionEndpointReturns(Status.UNPROCESSABLE_ENTITY, pptReference)
        await(connector.submit(pptReference)) mustBe Left(AlreadySubmitted)
      }
    }

    "Amend correctly" when {

      "there is a charge reference" in {
        givenReturnsAmendmentEndpointReturns(Status.OK,
          pptReference,
          body = """{"chargeDetails": {"chargeReference": "SOMEREF"}}"""
        )
        await(connector.amend(pptReference)) mustBe Some("SOMEREF")
      }

      "there is no charge reference" in {
        givenReturnsAmendmentEndpointReturns(Status.OK,
          pptReference,
          body = """{"chargeDetails": null}""",
        )
        await(connector.amend(pptReference)) mustBe None
      }
    }

    "throw" when {
      "get response cannot be parsed" in {
        givenGetReturnsEndpointReturns(Status.OK, pptReference, "00xx", body = "{")
        a[DownstreamServiceError] mustBe thrownBy(await(connector.get(pptReference, "00xx")))
      }

      "submit response cannot be parsed" in {
        givenReturnsSubmissionEndpointReturns(Status.OK, pptReference, body = "{")
        a[DownstreamServiceError] mustBe thrownBy(await(connector.submit(pptReference)))
      }

      "amend response cannot be parsed" in {
        givenReturnsAmendmentEndpointReturns(Status.OK, pptReference, body = "{")
        a[DownstreamServiceError] mustBe thrownBy(await(connector.amend(pptReference)))
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
      WireMock.get(s"/returns-submission/$pptReference")
        .willReturn(
          aResponse()
            .withStatus(status)
            .withBody(body)
        )
    )

  private def givenReturnsAmendmentEndpointReturns(
                                                    status: Int,
                                                    pptReference: String,
                                                    body: String = "",
                                                  ): StubMapping =
    stubFor(
      WireMock.get(s"/returns-amend/$pptReference")
        .willReturn(
          aResponse()
            .withStatus(status)
            .withBody(body)
        )
    )
}