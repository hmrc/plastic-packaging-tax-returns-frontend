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
import forms.returns.credits.ExportedCreditsFormProvider
import models.Mode.NormalMode
import models.returns.CreditRangeOption
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import play.twirl.api.Html
import support.{ViewAssertions, ViewMatchers}
import views.html.returns.credits.ExportedCreditsView

import java.time.LocalDate

class ExportedCreditsViewSpec extends ViewSpecBase with ViewAssertions with ViewMatchers {
  //todo add tests
  val page: ExportedCreditsView = inject[ExportedCreditsView]
  val form = new ExportedCreditsFormProvider()()
  val creditRangeOption = CreditRangeOption(LocalDate.of(2023, 4, 1), LocalDate.of(2024, 3, 31))

  private def createView(form: Form[Boolean] = form): Html =
    page(form, "year-key", NormalMode, creditRangeOption)(request, messages)

  "Exported Credits View" should {

    val view = createView()

    "have a title" in {
      view.select("title").text must include("Plastic packaging you paid tax on before it was exported - Submit return - Plastic Packaging Tax - GOV.UK")
      view.select("title").text must include(messages("exportedCredits.heading"))
    }

    "have a heading" in {
      view.select("h1").text mustBe messages("Plastic packaging you paid tax on before it was exported")
      view.select("h1").text mustBe messages("exportedCredits.heading")
    }

    "have a caption/section header" in {
      view.getElementById("section-header").text mustBe messages("Credit for 1 April 2023 to 31 March 2024")
      view.getElementById("section-header").text mustBe messages("credits.caption", "1 April 2023 to 31 March 2024")
    }

    "have a field set legend" in {
      view.getElementsByClass("govuk-fieldset__legend").text mustBe messages("exportedCredits.heading.2", "1 April 2023 and 31 March 2024")
    }

    "have paragraph content" in {
      val doc: Document = Jsoup.parse(view.toString())

      doc.text() must include("If you paid tax on plastic packaging and then you exported it, you can claim tax back as credit.")
      doc.text() must include(messages("exportedCredits.paragraph.1"))
      doc.text() must include("You can also claim tax back as credit if another business exported it.")
      doc.text() must include(messages("exportedCredits.paragraph.2"))

    }

    "have a submit button" in {
      view.getElementsByClass("govuk-button").text mustBe messages("site.continue")
    }

    "display error" when {

      "nothing has been checked" in {
        val boundForm = form.withError("requiredKey", "exportedCredits.error.required")
        val view = createView(boundForm)
        val doc: Document = Jsoup.parse(view.toString())

        doc.text() must include("Select yes if you paid tax on plastic packaging before it was exported")
      }
    }
  }
}