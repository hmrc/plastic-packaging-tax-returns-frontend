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

package views.returns.credits

import base.ViewSpecBase
import forms.returns.credits.ConvertedCreditsWeightFormProvider
import models.returns.CreditRangeOption
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.ArgumentMatchers.any
import org.mockito.{ArgumentMatchers, MockitoSugar}
import org.mockito.integrations.scalatest.ResetMocksAfterEachTest
import play.api.i18n.Messages
import play.api.mvc.Call
import play.twirl.api.Html
import support.{ViewAssertions, ViewMatchers}
import uk.gov.hmrc.scalatestaccessibilitylinter.AccessibilityMatchers
import views.html.returns.credits.ConvertedCreditsWeightView

import java.time.LocalDate

class ConvertedCreditsWeightViewSpec extends ViewSpecBase
  with ViewAssertions
  with ViewMatchers
  with AccessibilityMatchers
  with MockitoSugar {

  override val messages = spy(super.messages)
  private val form = new ConvertedCreditsWeightFormProvider()()
  private val page = inject[ConvertedCreditsWeightView]
  private val creditRangeOption = CreditRangeOption(LocalDate.of(2023, 4, 1), LocalDate.of(2024, 3, 31))
  private def createView: Html = page(form, Call("method", "/submit-url"), creditRangeOption)(request, messages)

  "It" should {
    
    // Note as this only runs once, no mocks are reset
    val view = createView

    "have a title and heading" in {
      verify(messages, times(2)).apply("converted-credits-weight.heading-title")
      view.select("title").text must include ("Submit return - Plastic Packaging Tax - GOV.UK")
      view.select("h1").text must include ("")
    }

    "have a caption" in {
      verify(messages, times(1)).apply(ArgumentMatchers.eq("credits.caption"), any())
      view.getElementById("section-header").text mustBe ("Credit for 1 April 2023 to 31 March 2024")
    }

    "have a hint" in {
      verify(messages, times(1)).apply("converted-credits-weight.hint")

      val doc: Document = Jsoup.parse(view.toString())
      doc.getElementById("value-hint").text must include (messages("Enter the weight in kilograms. 1 tonne is 1,000kg."))
    }

    "contain save & continue button" in {
      verify(messages, times(1)).apply("site.continue")
      view.getElementsByClass("govuk-button").text() mustBe messages("site.continue")
    }
  }

}
