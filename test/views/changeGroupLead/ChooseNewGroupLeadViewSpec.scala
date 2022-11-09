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
import forms.changeGroupLead.SelectNewGroupLeadForm
import play.api.data.Form
import play.twirl.api.Html
import support.{ViewAssertions, ViewMatchers}
import views.html.changeGroupLead.ChooseNewGroupLeadView


class ChooseNewGroupLeadViewSpec extends ViewSpecBase  with ViewAssertions with ViewMatchers {
  val page: ChooseNewGroupLeadView = inject[ChooseNewGroupLeadView]
  val members: Seq[String] = Seq("Test Company Ltd Asia", "Test Company Ltd Europe", "Test Company Ltd UK")
  val form: Form[String] = new SelectNewGroupLeadForm().apply(members)
  val appConfig = inject[FrontendAppConfig]

  private def createView: Html =
    page(form, members )(request, messages)

  "ChooseNewGroupLeadView" should {
    val view = createView

    "have a title" in {
      view.select("title").text mustBe
      "Select the new representative member - Plastic Packaging Tax - GOV.UK"
    }

    "have a back link" in {
      view.getElementsByClass("govuk-back-link").size() mustBe 1
    }

    "have a heading" in {
      view.select("h1").text mustBe
      "Select the new representative member"
    }

    "have radio options" in {
      view.select(".govuk-radios__item").get(0).text mustBe messages("Test Company Ltd Asia")
      view.select(".govuk-radios__item").get(1).text mustBe messages("Test Company Ltd Europe")
      view.select(".govuk-radios__item").get(2).text mustBe messages("Test Company Ltd UK")
    }

    "have a paragraph" in {
      view.getElementById("paragraph").text mustBe
        "If the organisation is not listed above you will need to add them as a new member first."
    }

    "have add a new member link" in {
      view.getElementById("add-new-member-to-group").text  mustBe messages("Add a new member to the group")
      view.getElementById("add-new-member-to-group")  must haveHref(appConfig.addMemberToGroupUrl)
    }

    "have a save & continue button" in {
      view.getElementsByClass("govuk-button").text mustBe "Save and continue"
    }
  }

}
