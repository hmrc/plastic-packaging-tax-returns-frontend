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

package views.returns

import base.ViewSpecBase
import forms.returns.PlasticExportedByAnotherBusinessFormProvider
import models.Mode.NormalMode
import org.jsoup.Jsoup
import play.api.data.Form
import play.twirl.api.Html
import support.ViewAssertions
import views.html.returns.PlasticExportedByAnotherBusinessView

class PlasticExportedByAnotherBusinessViewSpec extends ViewSpecBase with ViewAssertions {

  private val page = inject[PlasticExportedByAnotherBusinessView]
  private val form = new PlasticExportedByAnotherBusinessFormProvider()()

  private def createView(form: Form[Boolean] = form): Html =
    page(form, NormalMode, 200L)(request, messages)

  "view" should {
    val view = createView()
    "have a title" in {
      view.select("title").text() mustBe "In this period, has another business exported or converted any of your 200kg of manufactured or imported finished plastic packaging components? - Submit return - Plastic Packaging Tax - GOV.UK"
      view.select("title").text() must include(messages("plasticExportedByAnotherBusiness.title", "200kg"))
    }

    "have an header" in {
      view.select("h1").text() mustBe "In this period, has another business exported or converted any of your 200kg of manufactured or imported finished plastic packaging components?"
      view.select("h1").text() mustBe messages("plasticExportedByAnotherBusiness.heading", "200kg")
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text() mustBe "Exported plastic packaging"
      view.getElementsByClass("govuk-caption-l").text() mustBe messages("plasticExportedByAnotherBusiness.caption")
    }

    "have a paragraph" in {
      val paraText = view.getElementsByClass("govuk-body").text()

      paraText must include("You will not be charged tax on these but you must still tell us about them.")
      paraText must include(messages("plasticExportedByAnotherBusiness.paragraph.1"))
      paraText must include("You must have evidence that the export or conversion has taken place.")
      paraText must include(messages("plasticExportedByAnotherBusiness.paragraph.1"))
    }

    "contain save & continue button" in {
      view.getElementsByClass("govuk-button").text() mustBe "Save and continue"
    }

    "error when no answer provided" in {
      val view = createView(form.bind(Map("value" -> "")))

      Jsoup.parse(view.toString()).text() must include("Select yes if another business has exported or converted any of your finished plastic packaging components")
    }

    "display an error summary box"  in {
      val view = createView(form.withError("error", "error message"))

      view.getElementsByClass("govuk-error-summary__title").text() mustBe "There is a problem"
      view.getElementsByClass("govuk-error-summary__title").text() mustBe messages("error.summary.title")
      view.getElementsByClass("govuk-error-summary__list").get(0).text() mustBe "error message"
    }
  }

}
