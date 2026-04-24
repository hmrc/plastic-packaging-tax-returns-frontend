/*
 * Copyright 2026 HM Revenue & Customs
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

import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play.PlaySpec
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{Text, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow}
import viewmodels.checkAnswers.returns.credits.CreditsTaxRateSummary

class CreditTaxRateSummarySpec extends PlaySpec {

  private val messages = mock[Messages]

  "summary" should {
    "return a row for the tax rate" in {
      when(messages.apply(anyString(), any[Any])).thenReturn("value")
      when(messages.apply("confirmPackagingCredit.hiddenText")).thenReturn("hidden text")

      CreditsTaxRateSummary(0.30)(messages) mustBe SummaryListRow(
        key = Key(Text("value"), "govuk-!-width-one-half"),
        value = Value(Text("value"))
      )
    }
  }
}
