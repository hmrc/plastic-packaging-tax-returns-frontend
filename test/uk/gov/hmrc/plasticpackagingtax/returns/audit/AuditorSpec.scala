/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.plasticpackagingtax.returns.audit

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.client.{VerificationException, WireMock}
import org.scalatest.concurrent.Eventually.eventually
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import play.api.http.Status
import uk.gov.hmrc.plasticpackagingtax.returns.base.it.ConnectorISpec
import uk.gov.hmrc.plasticpackagingtax.returns.base.unit.Injector
import uk.gov.hmrc.plasticpackagingtax.returns.builders.TaxReturnBuilder
import uk.gov.hmrc.plasticpackagingtax.returns.models.domain.TaxReturn

class AuditorSpec extends ConnectorISpec with Injector with ScalaFutures with TaxReturnBuilder {

  val auditor: Auditor = app.injector.instanceOf[Auditor]
  val auditUrl         = "/write/audit"

  override def overrideConfig: Map[String, Any] =
    Map("auditing.enabled" -> true, "auditing.consumer.baseUri.port" -> wirePort)

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    WireMock.configureFor(wireHost, wirePort)
    wireMockServer.start()
  }

  override protected def afterAll(): Unit = {
    wireMockServer.stop()
    super.afterAll()
  }

  "Auditor" should {
    "post taxReturn event" when {
      "submit Tax Return invoked" in {
        givenAuditReturns(Status.NO_CONTENT)
        val taxReturn = aTaxReturn()

        auditor.auditTaxReturn(taxReturn)

        eventually(timeout(Span(5, Seconds))) {
          eventSendToAudit(auditUrl, taxReturn) mustBe true
        }
      }
    }

    "not throw exception" when {
      "submit taxReturn audit event fails" in {
        givenAuditReturns(Status.BAD_REQUEST)
        val taxReturn = aTaxReturn()

        auditor.auditTaxReturn(taxReturn)

        eventually(timeout(Span(5, Seconds))) {
          eventSendToAudit(auditUrl, taxReturn) mustBe true
        }
      }
    }
  }

  private def givenAuditReturns(statusCode: Int): Unit =
    stubFor(
      post(auditUrl)
        .willReturn(
          aResponse()
            .withStatus(statusCode)
        )
    )

  private def eventSendToAudit(url: String, taxReturn: TaxReturn): Boolean =
    try {
      verify(
        postRequestedFor(urlEqualTo(url))
          .withRequestBody(
            equalToJson(
              """{
                  "auditSource": "plastic-packaging-tax-returns-frontend",
                  "auditType": """" + CreateTaxReturnEvent.eventType + """",
                  "eventId": "${json-unit.any-string}",
                  "tags": {
                    "clientIP": "-",
                    "path": "-",
                    "X-Session-ID": "-",
                    "Akamai-Reputation": "-",
                    "X-Request-ID": "-",
                    "deviceID": "-",
                    "clientPort": "-"
                  },
                  "detail": """ + TaxReturn.format.writes(taxReturn).toString() + """,
                  "generatedAt": "${json-unit.any-string}",
                  "metadata": {
                    "sendAttemptAt": "${json-unit.any-string}",
                    "instanceID": "${json-unit.any-string}",
                    "sequence": "${json-unit.any-number}"
                  }
                }""".stripMargin,
              true,
              true
            )
          )
      )
      true
    } catch {
      case _: VerificationException => false
    }

}
