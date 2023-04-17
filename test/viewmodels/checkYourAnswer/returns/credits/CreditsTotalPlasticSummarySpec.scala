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
import org.mockito.MockitoSugar.{mock, when}
import org.scalatestplus.play.PlaySpec
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{HtmlContent, Text, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow}
import viewmodels.PrintLong
import viewmodels.checkAnswers.returns.credits.CreditsTotalPlasticSummary

class CreditsTotalPlasticSummarySpec extends PlaySpec {

  private val message = mock[Messages]

  "summary" should {
    "return a row" in {

      when(message.apply("confirmPackagingCredit.totalPlastic")).thenReturn("total plastic")
      when(message.apply(200L.asKg)).thenReturn("200kg")
      when(message.apply(ArgumentMatchers.eq("confirmPackagingCredit.hiddenText"))).thenReturn("hidden text")

      CreditsTotalPlasticSummary(200L)(message) mustBe SummaryListRow(
        key = Key(Text("total plastic"), "govuk-!-width-one-half"),
        value = Value(HtmlContent(s"""<p>200kg<span class="govuk-visually-hidden">hidden text</span></p>"""))
      )

    }
  }
}
