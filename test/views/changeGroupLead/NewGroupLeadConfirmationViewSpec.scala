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
import play.twirl.api.Html
import support.{ViewAssertions, ViewMatchers}
import views.html.changeGroupLead.NewGroupLeadConfirmationView

class NewGroupLeadConfirmationViewSpec extends ViewSpecBase with ViewAssertions with ViewMatchers {

  private val page: NewGroupLeadConfirmationView = inject[NewGroupLeadConfirmationView]

  private def createView(): Html =
    page()(request, messages)

  "NewGroupLeadConfirmationView" should {

    val view = createView()

    "have a title" in {
      view.select("title").text() must include("The representative member will be changed - Account - Plastic Packaging Tax - GOV.UK")
      view.select("title").text() must include(messages("newGroupLeadConfirmation.title"))
    }

    "have a confirmation panel" in {
      view.select("h1").text() must include(messages("newGroupLeadConfirmation.heading"))
    }

    "have a header level 2 for the old representative" in {
      view.select("h2").text() must include(messages("newGroupLeadConfirmation.heading.2"))
    }

    "have a paragraph including link to BTA" in {
      view.getElementsByClass("govuk-body").text() must include("You need to go to your")
      view.getElementById("bta-page-link").text() mustBe messages("newGroupLeadConfirmation.paragraph.1.link")
    }

    "have a header level 2 for the new representative" in {
      view.select("h2").text() must include(messages("newGroupLeadConfirmation.heading.3"))
    }

    "have a paragraph for the new representative" in {
      assertNewRepresentativeParagraph()
    }

    "have a list of bullets for the new representative" in {
      assertBulletList()
    }

    "have a survey link" in {
      view.getElementById("survey-link").text must include(messages("newGroupLeadConfirmation.exitSurvey.link"))
    }

    "have a technical issue link" in {
      view.getElementsByClass("govuk-link hmrc-report-technical-issue ").text must include ("Is this page not working properly?")
    }

    def assertNewRepresentativeParagraph(): Unit = {
      val paragraph = createView.getElementsByClass("govuk-body")
      paragraph.text() must include(messages("newGroupLeadConfirmation.paragraph.2.1"))
      paragraph.text() must include(messages("newGroupLeadConfirmation.paragraph.2.2"))
      paragraph.text() must include(messages("newGroupLeadConfirmation.paragraph.2.3"))
    }

    def assertBulletList(): Unit = {
      val bulletList = createView.getElementsByClass("govuk-list--bullet")
      bulletList.text() must include("Plastic Packaging Tax registration number")
      bulletList.text() must include(messages("newGroupLeadConfirmation.bulletList.first"))
      bulletList.text() must include("date of registration")
      bulletList.text() must include(messages("newGroupLeadConfirmation.bulletList.second"))
    }
  }
}
