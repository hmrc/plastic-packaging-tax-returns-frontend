/*
 * Copyright 2025 HM Revenue & Customs
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

package models.obligations

import models.returns.TaxReturnObligation
import org.scalatestplus.play.PlaySpec

import java.time.LocalDate

class PPTObligationsSpec extends PlaySpec {

  "nextObligationToReturn" must {
    "return the oldestOverdueObligation" when {
      "it exists" in {
        val sut = PPTObligations(
          nextObligation = None,
          oldestOverdueObligation = Some(obligation("oldest-obligation")),
          0,
          isNextObligationDue = false,
          false
        )

        sut.nextObligationToReturn mustBe Some(obligation("oldest-obligation"))
      }

      "it exists and nextObligation also exists and is due" in {
        val sut = PPTObligations(
          nextObligation = Some(obligation("next-obligation")),
          oldestOverdueObligation = Some(obligation("oldest-obligation")),
          0,
          isNextObligationDue = true,
          false
        )

        sut.nextObligationToReturn mustBe Some(obligation("oldest-obligation"))
      }
    }
    "return nextObligation" when {
      "it is due" in {
        val sut = PPTObligations(
          nextObligation = Some(obligation("next-obligation")),
          oldestOverdueObligation = None,
          0,
          isNextObligationDue = true,
          false
        )

        sut.nextObligationToReturn mustBe Some(obligation("next-obligation"))
      }
    }
    "return None" when {
      "there is no overdue obligations and nextObligation is not due" in {
        val sut = PPTObligations(
          nextObligation = Some(obligation("next-not-due")),
          oldestOverdueObligation = None,
          0,
          isNextObligationDue = false,
          false
        )

        sut.nextObligationToReturn mustBe None
      }
      "there is no overdue obligations or nextObligation" in {
        val sut = PPTObligations(
          nextObligation = None,
          oldestOverdueObligation = None,
          0,
          isNextObligationDue = false,
          false
        )

        sut.nextObligationToReturn mustBe None
      }
    }
  }

  def obligation(key: String): TaxReturnObligation =
    TaxReturnObligation(LocalDate.now(), LocalDate.now(), LocalDate.now(), key)

}
