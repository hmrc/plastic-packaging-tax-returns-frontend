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
import forms.returns.credits.ConvertedCreditsFormProvider
import models.Mode.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import play.twirl.api.Html
import support.{ViewAssertions, ViewMatchers}
import views.html.returns.credits.ConvertedCreditsView

class ConvertedCreditsViewSpec extends ViewSpecBase with ViewAssertions with ViewMatchers {
//todo add tests
  val page: ConvertedCreditsView = inject[ConvertedCreditsView]
  val form = new ConvertedCreditsFormProvider()()

  private def createView(form: Form[Boolean] = form): Html =
    page(form, NormalMode)(request, messages)

  //todo: re-instate the test when updating content
  "Converted Credits View" should {

    val view = createView()

    "have a title" in {
      view.select("title").text must include("- Submit return - Plastic Packaging Tax - GOV.UK")
      view.select("title").text must include(messages("converted-credits-yes-no.title-heading"))
    }

    "have a heading" in {
      view.select("h1").text mustBe messages("converted-credits-yes-no.title-heading")
    }

    "have a caption/section header" in {
      view.getElementById("section-header").text mustBe "Credit for 1 April 2022 to 31 March 2023"
    }

    "have a fieldset legend" ignore { // todo needs fixing
      view.getElementsByClass("govuk-fieldset__legend").text mustBe messages("converted-credits-yes-no.heading.2")
    }

    "have a hint" ignore { // todo needs fixing
      view.getElementsByAttributeValue("for", "converted-credits-weight").text() mustBe "How much weight, in kilograms?"
      view.getElementsByAttributeValue("for", "converted-credits-weight").text() mustBe messages("converted-credits-yes-no.weight.label")
      view.getElementById("converted-credits-weight-hint").text mustBe messages("1 tonne is 1,000kg.")
      view.getElementById("converted-credits-weight-hint").text mustBe messages("converted-credits-yes-no.weight.hint")
    }

    "have paragraph content" in {
      val doc: Document = Jsoup.parse(view.toString())
      doc.text() must include(messages("converted-credits-yes-no.paragraph.1"))
      doc.text() must include(messages("converted-credits-yes-no.paragraph.2"))
    }

    "have a submit button" in {
      view.getElementsByClass("govuk-button").text mustBe messages("site.continue")
    }

    "display error" when {

      "nothing has been checked" in { // todo sort
        //todo: change to a generic error or bind the form to an empty value to see the proper error.
        /*
          If the intent here is to test that we get the converted-credits-yes-no.error.required error message
          then we should bind the form to an empty value

          Else if the intent is just to test we get any error message when the form has an error then we
          should use any generic test error message as using the real one it may make the intent
          of the test obscured.
        */
        val boundForm = form.withError("requiredKey", "converted-credits-yes-no.error.required")
        val view = createView(boundForm)
        val doc: Document = Jsoup.parse(view.toString())

        doc.text() must include("Select yes if you paid tax on plastic packaging before it was converted")
      }
    }
  }

}
