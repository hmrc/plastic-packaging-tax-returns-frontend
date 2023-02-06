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

package views.changeGroupLead

import base.ViewSpecBase
import forms.changeGroupLead.MainContactJobTitleFormProvider
import models.Mode.NormalMode
import play.api.data.Form
import play.twirl.api.Html
import support.{ViewAssertions, ViewMatchers}
import views.html.changeGroupLead.MainContactJobTitleView


class MainContactJobTitleViewSpec extends ViewSpecBase  with ViewAssertions with ViewMatchers  {
 private val page = inject[MainContactJobTitleView]

  val form: Form[String] = new MainContactJobTitleFormProvider()()

  private def createView: Html =
    page(form,"contact-name", NormalMode)(request, messages)

  "MainContactJobTitleView" should {

    "have a title" in {
      createView.select("title").text must startWith ("What is the main contact’s job title?")
    }

    "have a back link" in {
      createView.getElementsByClass("govuk-back-link").size() mustBe 1
    }

    "have a heading" in {
      createView.select("h1").text mustBe "What is contact-name’s job title?"
    }

    "have string input" in {
      val input = createView.getElementById("value")
      input.attr("type") mustBe "text"
    }

    "have a save & continue button" in {
      createView.getElementsByClass("govuk-button").text mustBe "Save and continue"
    }
  }

}
