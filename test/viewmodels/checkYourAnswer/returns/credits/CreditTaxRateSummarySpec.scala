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

import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.anyString
import org.mockito.MockitoSugar.{mock, when}
import org.scalatestplus.play.PlaySpec
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.{Text, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow}
import viewmodels.checkAnswers.returns.credits.CreditTaxRateSummary

class CreditTaxRateSummarySpec extends PlaySpec {

  private val messages = mock[Messages]

  "summary" should {
    "return a row for the tax rate" in {
      when(messages.apply(anyString())).thenReturn("value")

      CreditTaxRateSummary(0.30)(messages) mustBe SummaryListRow(
        key = Key(Text("value")),
        value = Value(Text("value")))
    }
  }
}
