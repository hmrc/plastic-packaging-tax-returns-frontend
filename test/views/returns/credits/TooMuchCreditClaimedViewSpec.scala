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

package views.returns.credits

import base.ViewSpecBase
import play.twirl.api.Html
import support.{ViewAssertions, ViewMatchers}
import views.html.returns.credits.TooMuchCreditClaimedView

class TooMuchCreditClaimedViewSpec extends ViewSpecBase  with ViewAssertions with ViewMatchers {

  val page: TooMuchCreditClaimedView = inject[TooMuchCreditClaimedView]

  private def createView(): Html =
    page()(request, messages)

  "view" should {

    val view = createView()

    "have title" in {
      view.select("title").text() must include(messages("TooMuchCreditClaimed.title"))
    }

    "have a h1" in {
      view.select("h1").text() must include("Page still to be designed.")
    }
  }

}
