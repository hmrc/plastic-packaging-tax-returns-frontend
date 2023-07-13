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

package views

import base.ViewSpecBase
import forms.AgentsFormProvider
import play.api.data.Form
import play.twirl.api.Html
import support.{ViewAssertions, ViewMatchers}
import views.html.AgentsView


class AgentsViewSpec extends ViewSpecBase  with ViewAssertions with ViewMatchers {
  val page: AgentsView = inject[AgentsView]
  val form: Form[String] = new AgentsFormProvider()()

  private def createView(form: Form[String] = form): Html =
    page(form)(request, messages)

  "AgentsView" should {
    val view = createView()

    "have a title" in {
      view.select("title").text mustBe
      "What is your client’s Plastic Packaging Tax registration number? - Plastic Packaging Tax - GOV.UK"
    }

    "have a back link" in {
      view.getElementsByClass("govuk-back-link").size() mustBe 1
    }

    "have a heading" in {
      view.select("h1").text mustBe messages("agents.heading")
    }

    "have string input" in {
      val input = view.getElementById("value")
      input.attr("type") mustBe "text"
    }

    "have a save & continue button" in {
      view.getElementsByClass("govuk-button").text() must include("Save and continue")
    }

    "display an error summary box"  in {
      val view = createView(form.withError("error", "error message"))

      view.getElementsByClass("govuk-error-summary__title").text() mustBe "There is a problem"
      view.getElementsByClass("govuk-error-summary__title").text() mustBe messages("error.summary.title")
      view.getElementsByClass("govuk-error-summary__list").get(0).text() mustBe "error message"
    }
  }

}
