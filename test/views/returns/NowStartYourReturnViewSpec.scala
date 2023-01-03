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

package views.returns

import base.ViewSpecBase
import play.api.mvc.Call
import play.twirl.api.Html
import support.{ViewAssertions, ViewMatchers}
import views.html.returns.NowStartYourReturnView

class NowStartYourReturnViewSpec extends ViewSpecBase with ViewAssertions with ViewMatchers {

  val page: NowStartYourReturnView = inject[NowStartYourReturnView]
  val obligationPeriod = "Month to Month Year"
  val buttonLink: Call = Call("#", "/foo")

  private def createView(creditsClaimed: Boolean = true): Html =
    page(obligationPeriod, creditsClaimed, buttonLink)(request, messages)

  "NowStartYourReturnView" should {

    val view = createView()

    "have a title" in {

      view.select("title").text mustBe
        "Now start your return for Month to Month Year - Submit return - Plastic Packaging Tax - GOV.UK"
    }

    "have a heading" in {

      view.select("h1").text mustBe
        "Now start your return for Month to Month Year"
    }

    "have a paragraph when credits have been claimed" in {
      view.getElementById("credits-saved-content").text mustBe messages("nowStartYourReturn.paragraph.1")
    }

    "not have a paragraph when not credit has been claimed" in {
      val notClaimedView = createView(false)
      notClaimedView.getElementById("credits-saved-content") mustBe null
    }

    "have a button" in {
      view.getElementById("link-button").text mustBe
        "Start your return"

    }
  }
}
