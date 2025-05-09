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

package viewmodels.checkYourAnswer.returns.credits

import org.mockito.ArgumentMatchers
import org.mockito.MockitoSugar.{mock, when}
import org.scalatestplus.play.PlaySpec
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{Text, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow}
import viewmodels.PrintBigDecimal
import viewmodels.checkAnswers.returns.credits.CreditAmountSummary

class CreditAmountSummarySpec extends PlaySpec {

  private val message = mock[Messages]

  "summary" should {
    "return a row" in {

      when(message.apply("confirmPackagingCredit.creditAmount")).thenReturn("credit amount")
      when(message.apply(BigDecimal(200).asPounds)).thenReturn("£200")
      when(message.apply(ArgumentMatchers.eq("confirmPackagingCredit.hiddenText"))).thenReturn("hidden text")

      CreditAmountSummary(200L)(message) mustBe SummaryListRow(
        key = Key(Text("credit amount"), "govuk-!-width-one-half"),
        value = Value(Text("£200"))
      )

    }
  }
}
