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
import play.twirl.api.Html
import support.ViewMatchers
import views.html.amends.AmendConfirmation

class AmendConfirmationViewSpec extends ViewSpecBase with ViewMatchers{

  val page: AmendConfirmation = inject[AmendConfirmation]

  private def createView(chargeRef: Option[String] = Some("1234")): Html =
    page(chargeRef)(request, messages)

  "view" should {

    "have a title" in {
      val view = createView()

      view.select("title").text() must include("Your return will be amended - Submit return - Plastic Packaging Tax - GOV.UK")
      view.select("title").text() must include(messages("amend.confirmation.submitted"))
    }

    "have a header" in {
      val view = createView()

      view.select("h1").text() must include("Your return will be amended")
      view.select("h1").text() must include(messages("amend.confirmation.submitted"))
    }

    "contain reference charge in the header" in {
      val view = createView(Some("ABC123"))

      view.getElementsByClass("govuk-panel__body").text must include("Your charge reference for this amendment is ABC123")
      view.getElementsByClass("govuk-panel__body").text must include(messages("amend.confirmation.panel"))
    }

    "not contain reference charge in the header" in {
      val view =createView(None)

      view.getElementsByClass("govuk-panel__body").text mustBe ""
    }

    "contains a link to the account page" in {
      val view = createView().getElementById("main-content")

      view.select("a").get(0) must
        haveHref(controllers.routes.IndexController.onPageLoad.url)
    }

    "contains what happen next message" in {
      val view = createView()

      view.getElementById("main-content").text must include(messages("amend.confirmation.subheading"))
      view.getElementById("main-content").text must include(messages("amend.confirmation.paragraph.1"))
      view.getElementById("main-content").text must include(messages("amend.confirmation.paragraph.2"))
      view.getElementById("main-content").text must include(messages("amend.confirmation.check.account") + " " +
        messages("amend.confirmation.paragraph.3"))
    }
  }
}
