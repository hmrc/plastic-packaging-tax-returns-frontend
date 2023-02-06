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
import play.api.mvc.Call
import play.twirl.api.Html
import support.{ViewAssertions, ViewMatchers}
import views.html.returns.credits.TooMuchCreditClaimedView

class TooMuchCreditClaimedViewSpec extends ViewSpecBase  with ViewAssertions with ViewMatchers {

  val page: TooMuchCreditClaimedView = inject[TooMuchCreditClaimedView]

  private def createView(): Html = {
    val changeWeightCall = Call("", "change")
    val cancelClaimCall = Call("", "cancel")
    page(changeWeightCall, cancelClaimCall)(request, messages)
  }

  "view" should {

    val view = createView()

    "have content" in {
      view.select("title").text() must include(messages("too-much-credit-claimed.heading"))
      view.select("h1").text() mustBe messages("too-much-credit-claimed.heading")
      view.getElementById("para1").text() mustBe messages("too-much-credit-claimed.para-1")
      view.getElementById("para2").text() mustBe messages("too-much-credit-claimed.para-2")
    }
    
    "have change button" in {
      view.getElementById("change-weight").text() mustBe messages("too-much-credit-claimed.button-text")
      view.getElementById("change-weight") must haveHref("change")
    }
    
    "have cancel link" in {
      view.getElementById("cancel").text() mustBe messages("too-much-credit-claimed.cancel-link-text")
      view.getElementById("cancel") must haveHref("cancel")
    }
  }

}
