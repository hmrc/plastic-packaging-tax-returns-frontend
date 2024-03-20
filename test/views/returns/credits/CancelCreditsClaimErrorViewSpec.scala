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

package views.returns.credits

import base.ViewSpecBase
import org.mockito.MockitoSugar
import play.twirl.api.Html
import support.{ViewAssertions, ViewMatchers}
import views.html.returns.credits.CancelCreditsClaimErrorView

class CancelCreditsClaimErrorViewSpec extends ViewSpecBase with ViewAssertions with ViewMatchers with MockitoSugar {

  private val page = inject[CancelCreditsClaimErrorView]

  private def createView(): Html =
    page("/continue-url")(request, messages)

  "CancelCreditsClaimView" should {

    val view = createView()

    "have a title" in {
      view.select("title").text mustBe
        "This credit has been removed - Submit return - Plastic Packaging Tax - GOV.UK"
      view.select("title").text must include(messages("cancelCreditsClaimError.title-heading"))
    }

    "have a heading" in {
      view.select("h1").text mustBe "This credit has been removed"
      view.select("h1").text mustBe messages("cancelCreditsClaimError.title-heading")
    }

    "have a continue button" in {
      view.getElementsByClass("govuk-button").text() must include("Continue")
      view.getElementsByClass("govuk-button").text() must include(messages("site.button.continue"))
    }
  }

}
