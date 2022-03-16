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

package uk.gov.hmrc.plasticpackagingtax.returns.models.financials

import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.i18n.Messages

import java.time.LocalDate

class PPTFinancialsSpec extends AnyWordSpecLike with Matchers with BeforeAndAfterEach {

  val mockMessages = mock[Messages]

  override protected def afterEach(): Unit = {
    super.beforeEach()
    reset(mockMessages)
  }

  "PPT Financials" should {
    "return payment statement" when {
      "nothing outstanding" in {
        val pptFinancials = PPTFinancials(None, None, None)
        pptFinancials.paymentStatement()(mockMessages)

        val captor = ArgumentCaptor.forClass(classOf[String])
        verify(mockMessages).apply(captor.capture(), any())
        captor.getValue.toString mustBe "account.homePage.card.payments.nothingOutstanding"
      }

      "in credit" in {
        val pptFinancials = PPTFinancials(Some(BigDecimal(100)), None, None)

        pptFinancials.paymentStatement()(mockMessages)

        val captor = ArgumentCaptor.forClass(classOf[String])
        verify(mockMessages).apply(captor.capture(), any())
        captor.getValue.toString mustBe "account.homePage.card.payments.inCredit"
      }

      "debitDue statement" in {
        val pptFinancials = PPTFinancials(None, Some(BigDecimal(100), LocalDate.now()), None)

        pptFinancials.paymentStatement()(mockMessages)

        val captor = ArgumentCaptor.forClass(classOf[String])
        verify(mockMessages, times(2)).apply(captor.capture(), any())
        captor.getValue.toString mustBe "account.homePage.card.payments.debitDue"
      }

      "overDue statement" in {
        val pptFinancials = PPTFinancials(None, None, Some(BigDecimal(50)))

        pptFinancials.paymentStatement()(mockMessages)

        val captor = ArgumentCaptor.forClass(classOf[String])
        verify(mockMessages).apply(captor.capture(), any())
        captor.getValue.toString mustBe "account.homePage.card.payments.overDue"
      }

      "debitDue and overDue statement" in {
        val pptFinancials =
          PPTFinancials(None, Some(BigDecimal(100), LocalDate.now()), Some(BigDecimal(50)))

        pptFinancials.paymentStatement()(mockMessages)

        val captor = ArgumentCaptor.forClass(classOf[String])
        verify(mockMessages).apply(captor.capture(), any())
        captor.getValue.toString mustBe "account.homePage.card.payments.debitAndOverDue"
      }
    }
  }
}
