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

package viewmodels.checkYourAnswer.returns.credits

import models.returns.credits.CreditSummaryRow
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar.{mock, when}
import org.scalatestplus.play.PlaySpec
import play.api.i18n.Messages
import viewmodels.checkAnswers.returns.credits.CreditTotalSummary

class CreditTotalSummarySpec extends PlaySpec {

  private val message = mock[Messages]

  "createRow" should {
    "create a row" in {
      when(message.apply(any[String])).thenAnswer((s: String) => s)
      CreditTotalSummary.createRow(200)(message) mustBe CreditSummaryRow(CreditTotalSummary.key, "£200.00")
    }
  }

}
