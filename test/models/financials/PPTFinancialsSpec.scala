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

package models.financials

import org.scalatestplus.play.PlaySpec

import java.time.LocalDate

class PPTFinancialsSpec extends PlaySpec {

  "amountToPayInPence" must {
    def financials(debit: Option[BigDecimal], overdue: Option[BigDecimal]) = PPTFinancials(None, debit.map(_ -> LocalDate.now()), overdue)
    "return 0" when {
      "there is no amounts" in {
        financials(debit = None, overdue = None).amountToPayInPence mustBe 0
      }
    }
    "return a value" when {
      "there is only debit" in {
        financials(debit = Some(10), overdue = None).amountToPayInPence mustBe 1000
      }
      "there is only overdue" in {
        financials(debit = None, overdue = Some(20)).amountToPayInPence mustBe 2000
      }
      "there is both debit and overdue" in {
        financials(debit = Some(10), overdue = Some(20)).amountToPayInPence mustBe 3000
      }
    }
  }
}
