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
import views.ViewUtils
import views.html.returns.credits.ConvertedCreditsView

class ConvertedCreditsViewSpec extends ViewSpecBase with ViewAssertions with ViewMatchers {

  val page: ConvertedCreditsView = inject[ConvertedCreditsView]
  val form = new ConvertedCreditsFormProvider()()

  private def createView(form: Form[Boolean] = form): Html =
    page(form,"year-key", NormalMode)(request, messages)

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

    "have paragraph content" in {
      val doc: Document = Jsoup.parse(view.toString())
      doc.text() must include(messages("converted-credits-yes-no.paragraph.1"))
      doc.text() must include(messages("converted-credits-yes-no.paragraph.2"))
    }

    "have a question" in {
      view.getElementsByClass("govuk-fieldset__legend").text mustBe messages("converted-credits-yes-no.question")
    }

    "have radios" in {
      view.select(".govuk-radios__item").get(0).text mustBe messages("site.yes")
      view.select(".govuk-radios__item").get(1).text mustBe messages("site.no")
    }

    "have a submit button" in {
      view.getElementsByClass("govuk-button").text mustBe messages("site.continue")
    }

    "display error" when {

      "the form has an error" in {
        val boundForm = form.withError("requiredKey", "converted-credits-yes-no.error.required")
        val view = createView(boundForm)
        val doc: Document = Jsoup.parse(view.toString())

        doc.getElementsByClass("govuk-error-summary").text() must include("Select yes if you paid tax on plastic packaging before it was converted")
      }
    }
  }

}
