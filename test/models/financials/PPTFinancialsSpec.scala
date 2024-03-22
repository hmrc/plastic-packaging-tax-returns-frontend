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

package models.financials

import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar.{mock, reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import play.api.i18n.Messages

import java.time.LocalDate

class PPTFinancialsSpec extends PlaySpec with BeforeAndAfterEach {

  val messages: Messages = mock[Messages]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(messages)
    when(messages.apply(any[String])).thenReturn("expected message")
    when(messages.apply(any[String], any)).thenReturn("expected message")
    when(messages.apply(any[String], any, any)).thenReturn("expected message")
  }

  "paymentStatement" must {
    "return nothingOutstanding text" when {
      "All values are none" in {
        val text = PPTFinancials(None, None, None).paymentStatement()(messages)

        text mustBe "expected message"
        verify(messages).apply("account.homePage.card.payments.nothingOutstanding")
      }
    }

    "return inCredit text" when {
      "Only credit is populated" in {
        val text = PPTFinancials(creditAmount = Some(BigDecimal(123)), None, None).paymentStatement()(messages)

        text mustBe "expected message"
        verify(messages).apply("account.homePage.card.payments.inCredit", "£123.00")
      }
    }

    "return debitDue text" when {
      "Only debit is populated" in {
        val text = PPTFinancials(
          None,
          debitAmount = Some(Charge(BigDecimal(345.21), LocalDate.of(2020, 3, 27))),
          None
        ).paymentStatement()(messages)

        text mustBe "expected message"
        verify(messages).apply("account.homePage.card.payments.debitDue", "£345.21", "27 expected message 2020")
        verify(messages).apply("month.3")
      }
    }

    "return overDue text" when {
      "Only overdue is populated" in {
        val text = PPTFinancials(None, None, overdueAmount = Some(0.01)).paymentStatement()(messages)

        text mustBe "expected message"
        verify(messages).apply("account.homePage.card.payments.overDue", "£0.01")
      }
    }

    "return debitAndOverDue text" when {
      "debit and overdue is populated" in {
        val text = PPTFinancials(
          None,
          debitAmount = Some(Charge(BigDecimal(345.21), LocalDate.of(2020, 3, 27))),
          overdueAmount = Some(0.01)
        ).paymentStatement()(messages)

        text mustBe "expected message"
        verify(messages).apply("account.homePage.card.payments.debitAndOverDue", "£345.21", "£0.01")
      }
    }

    "return error text" when {
      "a nonsense combination is populated" in {
        val text = PPTFinancials(
          creditAmount = Some(BigDecimal(123456)),
          debitAmount = Some(Charge(BigDecimal(345.21), LocalDate.of(2020, 3, 27))),
          overdueAmount = Some(0.01)
        ).paymentStatement()(messages)

        text mustBe "expected message"
        verify(messages).apply("account.homePage.card.payments.error")
      }
    }
  }

  "amountToPayInPence" must {
    def financials(debit: Option[BigDecimal] = None, overdue: Option[BigDecimal] = None) =
      PPTFinancials(None, debit.map(Charge(_, LocalDate.now())), overdue)
    "return 0" when {
      "there is no amounts" in {
        financials(debit = None).amountToPayInPence mustBe 0
      }
    }
    "return a value" when {
      "there is a debit" in {
        financials(debit = Some(10)).amountToPayInPence mustBe 1000
      }

      "there is an overdue" in {
        financials(overdue = Some(8)).amountToPayInPence mustBe 800
      }

      "there is both debit and overdue" in {
        financials(debit = Some(90), overdue = Some(20)).amountToPayInPence mustBe 9000
      }
    }
  }
}
