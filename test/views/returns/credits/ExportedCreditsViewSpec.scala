/*
 * Copyright 2022 HM Revenue & Customs
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
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import play.twirl.api.Html
import support.{ViewAssertions, ViewMatchers}
import views.html.returns.credits.ExportedCreditsView

class ExportedCreditsViewSpec extends ViewSpecBase with ViewAssertions with ViewMatchers {

  val page: ExportedCreditsView = inject[ExportedCreditsView]
  val form = new ExportedCreditsFormProvider()()

  private def createView: Html =
    page(form, NormalMode)(request, messages)

  "Exported Credits View" should {

    val view = createView

    "have a title" in {
      view.select("title").text must include("- Submit return - Plastic Packaging Tax - GOV.UK")
      view.select("title").text must include(messages("exported.credits.title"))
    }

    "have a heading" in {
      view.select("h1").text mustBe messages("exported.credits.heading")
    }

    "have a caption/section header" in {
      view.getElementById("section-header").text mustBe messages("credits.caption")
    }

    "have a secondary heading" in {
      view.getElementById("exported-credits-h2").text mustBe messages("exported.credits.heading.2")
    }

    "have paragraph content" in {
      val doc: Document = Jsoup.parse(view.toString())

      doc.text() must include(messages("exported.credits.paragraph.1"))
      doc.text() must include(messages("exported.credits.paragraph.2"))
      doc.text() must include(messages("exported.credits.paragraph.3"))
    }

    "have a hint" in {
      view.getElementById("exported-credits-weight-hint").text mustBe messages("exported.credits.weight.hint")
    }

    "have a submit button" in {
      view.getElementsByClass("govuk-button").text mustBe messages("site.continue")
    }

  }

}