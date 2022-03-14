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
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import org.scalatest.EitherValues
import org.scalatest.concurrent.ScalaFutures
import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.Helpers.await
import uk.gov.hmrc.plasticpackagingtax.returns.base.it.ConnectorISpec
import uk.gov.hmrc.plasticpackagingtax.returns.models.exportcredits.ExportCreditBalance

import java.time.LocalDate
import java.util.UUID

class ExportCreditsConnectorSpec extends ConnectorISpec with ScalaFutures with EitherValues {

  lazy val connector: ExportCreditsConnector = app.injector.instanceOf[ExportCreditsConnector]

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    WireMock.configureFor(wireHost, wirePort)
    wireMockServer.start()
  }

  override protected def afterAll(): Unit = {
    wireMockServer.stop()
    super.afterAll()
  }

  "get export credit details" should {

    "return success response" when {

      "retrieving existing export credit details" in {

        val pptReference = UUID.randomUUID().toString
        val fromDate     = LocalDate.now().minusYears(2)
        val toDate       = LocalDate.now()
        val expectedExportCreditBalance =
          ExportCreditBalance(totalPPTCharges = BigDecimal("1234.56"),
                              totalExportCreditClaimed = BigDecimal("12.34"),
                              totalExportCreditAvailable = BigDecimal("123.45")
          )

        givenGetExportCreditEndpointReturns(Status.OK,
                                            pptReference,
                                            fromDate,
                                            toDate,
                                            Json.toJsObject(expectedExportCreditBalance).toString
        )

        val res = await(connector.get(pptReference, fromDate, toDate))

        res.value.totalExportCreditAvailable mustBe expectedExportCreditBalance.totalExportCreditAvailable
        getTimer("ppt.exportcredits.open.get.timer").getCount mustBe 1
      }
    }

    "throws exception" when {

      "service returns non success status code" in {
        val pptReference = UUID.randomUUID().toString
        val fromDate     = LocalDate.now().minusYears(2)
        val toDate       = LocalDate.now()
        givenGetExportCreditEndpointReturns(Status.BAD_REQUEST, pptReference, fromDate, toDate)

        val result = await(connector.get(pptReference, fromDate, toDate))

        result.left.value.getMessage must include("Failed to retrieve export credits")
      }

      "service returns invalid response" in {
        val pptReference = UUID.randomUUID().toString
        val fromDate     = LocalDate.now().minusYears(2)
        val toDate       = LocalDate.now()
        givenGetExportCreditEndpointReturns(Status.CREATED,
                                            pptReference,
                                            fromDate,
                                            toDate,
                                            "someRubbish"
        )

        val result = await(connector.get(pptReference, fromDate, toDate))

        result.left.value.getMessage must include("Failed to retrieve export credits")
      }
    }
  }

  private def givenGetExportCreditEndpointReturns(
    status: Int,
    pptReference: String,
    fromDate: LocalDate,
    toDate: LocalDate,
    body: String = ""
  ) =
    stubFor(
      WireMock.get(s"/export-credits/$pptReference?fromDate=$fromDate&toDate=$toDate")
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
