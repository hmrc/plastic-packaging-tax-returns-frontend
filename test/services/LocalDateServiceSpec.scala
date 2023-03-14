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

package services

import config.FrontendAppConfig
import org.mockito.MockitoSugar.{mock, when}
import org.scalatestplus.play.PlaySpec

import java.time.LocalDate

class LocalDateServiceSpec extends PlaySpec {

  private val appConfig = mock[FrontendAppConfig]

  "isTodayPostTaxRegimeStartDate" should {
    "return true" when {
      "today is after tax regime start date" in {
        when(appConfig.taxRegimeStartDate).thenReturn(LocalDate.now.minusDays(1))

        val sut = new LocalDateService(appConfig)

        sut.isTodayPostTaxRegimeStartDate mustBe true
      }

      "today is the same as tax regime start date" in {
        when(appConfig.taxRegimeStartDate).thenReturn(LocalDate.now)

        val sut = new LocalDateService(appConfig)

        sut.isTodayPostTaxRegimeStartDate mustBe true
      }
    }

    "return false today is before tax regime start date" in {
      when(appConfig.taxRegimeStartDate).thenReturn(LocalDate.now.plusDays(1))

      val sut = new LocalDateService(appConfig)

      sut.isTodayPostTaxRegimeStartDate mustBe false
    }
  }
}
