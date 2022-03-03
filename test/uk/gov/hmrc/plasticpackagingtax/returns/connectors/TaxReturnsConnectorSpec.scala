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

package uk.gov.hmrc.plasticpackagingtax.returns.connectors

import com.codahale.metrics.{MetricFilter, SharedMetricRegistries, Timer}
import uk.gov.hmrc.plasticpackagingtax.returns.base.it.ConnectorISpec
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, post, put}
import org.scalatest.EitherValues
import org.scalatest.concurrent.ScalaFutures
import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.Helpers.await
import uk.gov.hmrc.plasticpackagingtax.returns.builders.TaxReturnBuilder
import uk.gov.hmrc.plasticpackagingtax.returns.models.domain.TaxReturn

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

  "create tax return" should {

    "return success response" when {

      "valid request send" in {

        givenPostToReturnsEndpointReturns(Status.CREATED,
                                          Json.toJsObject(aTaxReturn(withId("123"))).toString
        )

        val res = await(connector.create(aTaxReturn(withId("123"))))

        res.value.id mustBe "123"
        getTimer("ppt.returns.create.timer").getCount mustBe 1
      }

      "tax return already exists" in {

        givenPostToReturnsEndpointReturns(Status.OK,
                                          Json.toJsObject(aTaxReturn(withId("123"))).toString
        )

        val res = await(connector.create(aTaxReturn(withId("123"))))

        res.value.id mustBe "123"

      }
    }

    "return error" when {

      "service returns non success status code" in {

        givenPostToReturnsEndpointReturns(Status.BAD_REQUEST)

        val res = await(connector.create(aTaxReturn()))

        res.left.value.getMessage must include("Failed to create return")
      }

      "service returns invalid response" in {

        givenPostToReturnsEndpointReturns(Status.CREATED, "someRubbish")

        val res = await(connector.create(aTaxReturn()))

        res.left.value.getMessage must include("Failed to create return")
      }
    }
  }

  private def givenPostToReturnsEndpointReturns(status: Int, body: String = "") =
    stubFor(
      post("/returns")
        .willReturn(
          aResponse()
            .withStatus(status)
            .withBody(body)
        )
    )

  "find tax return" should {

    "return tax return" when {

      "exists" in {

        givenGetReturnsEndpointReturns(Status.OK,
                                       Json.toJsObject(aTaxReturn(withId("123"))).toString()
        )

        val res = await(connector.find("123"))

        res.value.get.id mustBe "123"
        getTimer("ppt.returns.find.timer").getCount mustBe 1
      }
    }

    "tax return empty result" when {

      "not exists" in {

        givenGetReturnsEndpointReturns(Status.NOT_FOUND)

        val res = await(connector.find("123"))

        res.isRight mustBe true
        res.value.isEmpty mustBe true
      }
    }

    "tax return error" when {

      "downstream returns error" in {

        givenGetReturnsEndpointReturns(Status.BAD_REQUEST)

        val res = await(connector.find("123"))

        res.left.value.getMessage must include("Failed to retrieve return")
      }
    }
  }

  private def givenGetReturnsEndpointReturns(status: Int, body: String = "") =
    stubFor(
      get("/returns/123")
        .willReturn(
          aResponse()
            .withStatus(status)
            .withBody(body)
        )
    )

  "update tax return" should {

    "return success response" when {

      "valid request send" in {

        givenPutToReReturnsEndpointReturns(Status.CREATED,
                                           "123",
                                           Json.toJsObject(aTaxReturn(withId("123"))).toString
        )

        val res = await(connector.update(aTaxReturn(withId("123"))))

        res.value.id mustBe "123"
        getTimer("ppt.returns.update.timer").getCount mustBe 1
      }

      "tax return already exists" in {

        givenPutToReReturnsEndpointReturns(Status.OK,
                                           "123",
                                           Json.toJsObject(aTaxReturn(withId("123"))).toString
        )

        val res = await(connector.update(aTaxReturn(withId("123"))))

        res.value.id mustBe "123"

      }
    }

    "tax return error" when {

      "service returns non success status code" in {

        givenPutToReReturnsEndpointReturns(Status.BAD_REQUEST, "123")

        val res = await(connector.update(aTaxReturn()))

        res.left.value.getMessage must include("Failed to update return")
      }

      "service returns invalid response" in {

        givenPutToReReturnsEndpointReturns(Status.CREATED, "123", "someRubbish")

        val res = await(connector.update(aTaxReturn()))

        res.left.value.getMessage must include("Failed to update return")
      }
    }
  }

  private def givenPutToReReturnsEndpointReturns(status: Int, id: String, body: String = "") =
    stubFor(
      put(s"/returns/$id")
        .willReturn(
          aResponse()
            .withStatus(status)
            .withBody(body)
        )
    )

  private def getTimer(name: String): Timer =
    SharedMetricRegistries
      .getOrCreate("plastic-packaging-tax-returns-frontend")
      .getTimers(MetricFilter.startsWith(name))
      .get(name)

}
