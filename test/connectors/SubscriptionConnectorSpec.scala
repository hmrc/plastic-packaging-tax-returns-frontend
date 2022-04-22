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
import org.scalatest.EitherValues
import org.scalatest.concurrent.ScalaFutures
import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.Helpers.await
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import models.subscription.subscriptionDisplay.ChangeOfCircumstanceDetails
import models.subscription.subscriptionUpdate.SubscriptionUpdateResponse
import support.PptTestData._

import java.time.ZonedDateTime
import java.util.UUID

class SubscriptionConnectorSpec extends ConnectorISpec with ScalaFutures with EitherValues {

  lazy val connector: SubscriptionConnector = app.injector.instanceOf[SubscriptionConnector]

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    WireMock.configureFor(wireHost, wirePort)
    wireMockServer.start()
  }

  override protected def afterAll(): Unit = {
    wireMockServer.stop()
    super.afterAll()
  }

  "get subscription details" should {

    "return success response" when {

      "retrieving existing subscription details" in {

        val pptReference       = UUID.randomUUID().toString
        val expectedRegDetails = createSubscriptionDisplayResponse(ukLimitedCompanySubscription)
        givenGetSubscriptionEndpointReturns(Status.OK,
                                            pptReference,
                                            Json.toJsObject(expectedRegDetails).toString
        )

        val res = await(connector.get(pptReference))

        res mustBe Right(expectedRegDetails)

      }
    }

    "throw exception" when {

      "service returns non success status code" in {
        val pptReference = UUID.randomUUID().toString
        givenGetSubscriptionEndpointReturns(Status.BAD_REQUEST, pptReference)

        intercept[Exception] {
          await(connector.get(pptReference))
        }
      }

      "service returns invalid response" in {
        val pptReference = UUID.randomUUID().toString
        givenGetSubscriptionEndpointReturns(Status.CREATED, pptReference, "someRubbish")

        intercept[Exception] {
          await(connector.get(pptReference))
        }
      }
    }
  }

  "update subscription details" should {

    "return success response" when {

      "updating existing subscription details" in {

        val pptReference = UUID.randomUUID().toString
        val expectedUpdateResponse = SubscriptionUpdateResponse(pptReference = pptReference,
                                                                processingDate = ZonedDateTime.now,
                                                                formBundleNumber = "123456789"
        )
        givenUpdateSubscriptionEndpointReturns(Status.OK,
                                               pptReference,
                                               Json.toJsObject(expectedUpdateResponse).toString
        )

        val res = await(
          connector.update(
            pptReference,
            createSubscriptionUpdateRequest(ukLimitedCompanySubscription,
                                            ChangeOfCircumstanceDetails("Update to details")
            )
          )
        )

        res mustBe expectedUpdateResponse

      }
    }

    "throws exception" when {

      "service returns non success status code" in {
        val pptReference = UUID.randomUUID().toString
        givenUpdateSubscriptionEndpointReturns(Status.BAD_REQUEST, pptReference)

        intercept[DownstreamServiceError] {
          await(
            connector.update(
              pptReference,
              createSubscriptionUpdateRequest(ukLimitedCompanySubscription,
                                              ChangeOfCircumstanceDetails("Update to details")
              )
            )
          )
        }
      }

      "service returns invalid response" in {
        val pptReference = UUID.randomUUID().toString
        givenUpdateSubscriptionEndpointReturns(Status.CREATED, pptReference, "someRubbish")

        intercept[DownstreamServiceError] {
          await(
            connector.update(
              pptReference,
              createSubscriptionUpdateRequest(ukLimitedCompanySubscription,
                                              ChangeOfCircumstanceDetails("Update to details")
              )
            )
          )
        }
      }
    }
  }

  private def givenGetSubscriptionEndpointReturns(
    status: Int,
    pptReference: String,
    body: String = ""
  ) =
    stubFor(
      WireMock.get(s"/subscriptions/$pptReference")
        .willReturn(
          aResponse()
            .withStatus(status)
            .withBody(body)
        )
    )

  private def givenUpdateSubscriptionEndpointReturns(
    status: Int,
    pptReference: String,
    body: String = ""
  ) =
    stubFor(
      WireMock.put(s"/subscriptions/$pptReference")
        .willReturn(
          aResponse()
            .withStatus(status)
            .withBody(body)
        )
    )

}