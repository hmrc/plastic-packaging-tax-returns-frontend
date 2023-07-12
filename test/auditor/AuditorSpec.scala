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

package auditor

import audit.Auditor
import audit.returns.{AmendStarted, ReturnStarted}
import base.utils.ConnectorISpec
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.MockitoSugar.{times, verify}
import org.mockito.{ArgumentCaptor, Mockito}
import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import java.time.ZonedDateTime

class AuditorSpec extends ConnectorISpec with ScalaFutures {

  val auditConnector: AuditConnector = mock[AuditConnector]
  val auditor: Auditor               = new Auditor(auditConnector)
  val processingDate                 = ZonedDateTime.now()
  val fromDate: ZonedDateTime        = ZonedDateTime.now()
  val toDate: ZonedDateTime          = ZonedDateTime.now.plusDays(1)
  val pptReference: String           = "XMPPT0000000123"

  "Auditor" when {

    "returns" should {

      "post return started" when {

        "returnStarted invoked" in {

          Mockito.reset(auditConnector)

          val captor = ArgumentCaptor.forClass(classOf[ReturnStarted])

          auditor.returnStarted("testId", pptReference)

          verify(auditConnector, times(1)).
            sendExplicitAudit(eqTo(ReturnStarted.eventType), captor.capture())(any(), any(), any())

          val capturedEvent = captor.getValue.asInstanceOf[ReturnStarted]
          capturedEvent.internalId mustBe "testId"
          capturedEvent.pptReference mustBe pptReference
          capturedEvent.msg mustBe ReturnStarted.message

        }
      }
    }

    "amends" should {

      "post amend started" when {

        "amendStarted invoked" in {

          Mockito.reset(auditConnector)

          val captor = ArgumentCaptor.forClass(classOf[AmendStarted])

          auditor.amendStarted("testId", pptReference)

          verify(auditConnector, times(1)).
            sendExplicitAudit(eqTo(AmendStarted.eventType), captor.capture())(any(), any(), any())

          val capturedEvent = captor.getValue.asInstanceOf[AmendStarted]
          capturedEvent.internalId mustBe "testId"
          capturedEvent.pptReference mustBe pptReference
          capturedEvent.msg mustBe AmendStarted.message

        }
      }
    }
  }
}
