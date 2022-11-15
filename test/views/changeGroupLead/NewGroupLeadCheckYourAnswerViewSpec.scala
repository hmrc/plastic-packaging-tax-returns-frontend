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
import models.changeGroupLead.RepresentativeMemberDetails
import play.api.mvc.Call
import play.twirl.api.Html
import support.{ViewAssertions, ViewMatchers}
import views.html.changeGroupLead.NewGroupLeadCheckYourAnswerView

class NewGroupLeadCheckYourAnswerViewSpec extends ViewSpecBase  with ViewAssertions with ViewMatchers{

  private val page: NewGroupLeadCheckYourAnswerView = inject[NewGroupLeadCheckYourAnswerView]

  private def createView: Html = {
    page(RepresentativeMemberDetails("Member 1", "henry hoover", "chief suction officer"), Call("get", "away-somewhere"))(request, messages)
  }

  "view" should {

    val view = createView

    "have a title" in {
      view.select("title").text() mustBe "Check your answers - Plastic Packaging Tax - GOV.UK"
      view.select("title").text() must include(messages("newGroupLeadCheckYourAnswers.heading"))
    }

    "have a header" in {
      view.select("h1").text() mustBe "Check your answers"
    }

    "has a list" in {
      view.getElementsByClass("govuk-summary-list") must not be empty
    }

    "show the representative member" in {

      getKeyAtRowIndex(view,0).text() mustBe "New representative member"
      getKeyAtRowIndex(view,0).text() mustBe messages("newGroupLeadCheckYourAnswers.representative.member.key")
      getValueAtRowIndex(view, 0).text() mustBe "Member 1"
    }

    "show change link" in {
      getActionAtRowIndex(view, 0).select("a").get(0).text() mustBe "Change"
      getActionAtRowIndex(view, 0).select("a").get(0).text() mustBe messages("site.change")
    }

    "have a now send your request header" in {
      view.getElementById("sent-your-request").text() mustBe "Now send your change"
      view.getElementById("sent-your-request").text() mustBe messages("newGroupLeadCheckYourAnswers.sendYourRequest")
    }

    "have a paraghraph description" in {
      view.getElementsByClass("govuk-body").text() must include("By sending this change to the representative member of the group you are confirming that, to the best of your knowledge, the details you are providing are correct.")
      view.getElementsByClass("govuk-body").text() must include(messages("newGroupLeadCheckYourAnswers.body"))
    }

    "have a Send Change button" in {
      view.getElementsByClass("govuk-button-group").text() mustBe "Send change"
      view.getElementsByClass("govuk-button-group").text() mustBe messages("newGroupLeadCheckYourAnswers.button")
      view.getElementsByClass("govuk-button-group").select("a").get(0) must haveHref("away-somewhere")
    }
  }

  private def getKeyAtRowIndex(view: Html, index: Int) = {
    view.getElementsByClass("govuk-summary-list__row").get(index)
      .getElementsByClass("govuk-summary-list__key").get(0)
  }

  private def getValueAtRowIndex(view: Html, index: Int) = {
    view.getElementsByClass("govuk-summary-list__row").get(index)
      .getElementsByClass("govuk-summary-list__value").get(0)
  }

  private def getActionAtRowIndex(view: Html, index: Int) = {
    view.getElementsByClass("govuk-summary-list__row").get(index)
      .getElementsByClass("govuk-summary-list__actions").get(0)
  }

}
