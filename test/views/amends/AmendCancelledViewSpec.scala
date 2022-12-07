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

package views.amends

import base.ViewSpecBase
import support.ViewMatchers
import views.html.amends.AmendCancelledView


class AmendCancelledViewSpec extends ViewSpecBase with ViewMatchers {

  private val page = inject[AmendCancelledView]

  "View" should {
    val view = page()(request, messages)

    "have a title" in {
      view.select("title").text() mustBe "You have cancelled amending this return - Submit return - Plastic Packaging Tax - GOV.UK"
      view.select("title").text() must include(messages("amendCancelledView.heading"))
    }

    "have a header" in {
      view.select("h1").text() mustBe "You have cancelled amending this return"
      view.select("h1").text() mustBe messages("amendCancelledView.heading")
    }

    "have a paragraph" in {
      view.getElementsByClass("govuk-body").text() must include("You can:")

      val listText = view.getElementsByClass("govuk-list").select("a").text()
      listText must include("go to your Plastic Packaging Tax account")
      listText must include("view or amend all submitted returns")
    }

    "have a link to the tax account page" in {
      view.getElementsByClass("govuk-list").select("a").first must haveHref(
        controllers.routes.IndexController.onPageLoad
      )
    }

    "have a link to the view or amend all submitted return account page" in {
      view.getElementsByClass("govuk-list").select("a").get(1) must haveHref(
        controllers.amends.routes.SubmittedReturnsController.onPageLoad
      )
    }

  }
}
