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

package views.amends

import base.ViewSpecBase
import play.twirl.api.Html
import support.ViewMatchers
import views.html.amends.AmendExportedPlasticPackagingView

class AmendExportedPlasticPackagingViewSpec extends ViewSpecBase with ViewMatchers {

  private val page             = inject[AmendExportedPlasticPackagingView]
  private def createView: Html = page()(request, messages)

  "view" should {

    val view = createView

    "have a title" in {
      view.select(
        "title"
      ).text() mustBe "Amending exported plastic packaging - Submit return - Plastic Packaging Tax - GOV.UK"
      view.select("title").text() must include(messages("amendExportedPlasticPackaging.title"))
    }

    "have a header" in {
      view.select("h1").text() mustBe "Amending exported plastic packaging"
      view.select("h1").text() mustBe messages("amendExportedPlasticPackaging.heading")
    }

    "have a paragrapth" in {

      val paragraph = view.getElementsByClass("govuk-body").text()
      paragraph must include(
        "Exported plastic packaging total is the sum of 2 weights you entered when you submitted your return."
      )
      paragraph must include(messages("amendExportedPlasticPackaging.paragraph.1"))

      paragraph must include("This is the weight of plastic packaging:")
      paragraph must include(messages("amendExportedPlasticPackaging.paragraph.2"))

      val list = view.getElementsByClass("govuk-list--bullet").text()
      list must include("you exported or intended to export within 12 months")
      list must include(messages("amendExportedPlasticPackaging.bulletPoint.1"))
      list must include("another business exported or converted")
      list must include(messages("amendExportedPlasticPackaging.bulletPoint.2"))

      paragraph must include(
        "To amend either of these weights, you’ll need to enter both. We’ll combine them and this will show as your amended exported plastic packaging total."
      )
      paragraph must include(messages("amendExportedPlasticPackaging.paragraph.3"))
    }

    "have a 'Continue' button link" in {
      val buttonElement = view.getElementById("link-button")

      buttonElement.text() mustBe "Continue"
      buttonElement.text() mustBe messages("site.button.continue")
      buttonElement.select("a").first must haveHref(
        controllers.amends.routes.AmendDirectExportPlasticPackagingController.onPageLoad
      )
    }
  }

}
