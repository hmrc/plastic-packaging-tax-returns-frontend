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

package models.returns

import org.mockito.MockitoSugar.{mock, when}
import org.scalatestplus.play.PlaySpec
import util.EdgeOfSystem

import java.time.{LocalDate, LocalDateTime}

class TaxReturnObligationSpec extends PlaySpec {

  val allNowOb                       = TaxReturnObligation(LocalDate.now(), LocalDate.now(), LocalDate.now(), "PPKY")
  val mockEdgeOfSystem: EdgeOfSystem = mock[EdgeOfSystem]
  when(mockEdgeOfSystem.localDateTimeNow).thenReturn(LocalDateTime.now())

  "tooOldToAmend" must {
    "return false" when {
      "return is exactly 4 years old" in {
        val sut = allNowOb.copy(dueDate = LocalDate.now().minusYears(4))

        sut.tooOldToAmend(mockEdgeOfSystem) mustBe false
      }
      "return is less than 4 years old" in {
        val sut = allNowOb.copy(dueDate = LocalDate.now().minusYears(4).plusDays(1))

        sut.tooOldToAmend(mockEdgeOfSystem) mustBe false
      }
    }
    "return true" when {
      "return is more than 4 years old" in {
        val sut = allNowOb.copy(dueDate = LocalDate.now().minusYears(4).minusDays(1))

        sut.tooOldToAmend(mockEdgeOfSystem) mustBe true
      }
    }
  }

}
