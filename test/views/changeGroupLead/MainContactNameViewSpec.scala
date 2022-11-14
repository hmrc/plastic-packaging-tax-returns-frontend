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

package views.changeGroupLead

import base.ViewSpecBase
import config.FrontendAppConfig
import forms.changeGroupLead.MainContactNameFormProvider
import models.Mode.NormalMode
import play.api.data.Form
import play.twirl.api.Html
import support.{ViewAssertions, ViewMatchers}
import views.html.changeGroupLead.MainContactNameView


class MainContactNameViewSpec extends ViewSpecBase  with ViewAssertions with ViewMatchers {
  val page: MainContactNameView = inject[MainContactNameView]

  val form: Form[String] = new MainContactNameFormProvider()()
  val appConfig = inject[FrontendAppConfig]


  private def createView: Html =
    page(form, "company-name",NormalMode)(request, messages)

  "MainContactNameView" should {
    val view = createView

    "have a title" in {
      view.select("title").text mustBe
      "Who is the main contact for company-name? - Account - Plastic Packaging Tax - GOV.UK"
      //todo this shouldnt have the compnay name in
    }

    "have a back link" in {
      view.getElementsByClass("govuk-back-link").size() mustBe 1
    }

    "have a heading" in {
      view.select("h1").text mustBe
      "Who is the main contact for company-name?"
    }

    "have string input" in {
      val input = view.getElementById("value")
      input.attr("type") mustBe "text"
    }

    "have a save & continue button" in {
      view.getElementsByClass("govuk-button").text mustBe "Save and continue"
    }
  }

}
