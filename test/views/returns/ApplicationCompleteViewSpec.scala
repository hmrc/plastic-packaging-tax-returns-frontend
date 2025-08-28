/*
 * Copyright 2025 HM Revenue & Customs
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
import controllers.routes
import play.twirl.api.HtmlFormat
import support.{ViewAssertions, ViewMatchers}
import views.html.returns.ApplicationCompleteView

class ApplicationCompleteViewSpec extends ViewSpecBase with ViewAssertions with ViewMatchers {

  val page: ApplicationCompleteView = inject[ApplicationCompleteView]
  val view: HtmlFormat.Appendable   = page()(request, messages)

  "ApplicationCompleteView" should {

    "have a title" in {

      view.select("title").text() must include("Application Complete")
      view.select("title").text() must include(messages("applicationComplete.title"))
    }

    "have a header" in {
      view.select("h1").text() must include("Application Complete")
      view.select("h1").text() must include(messages("applicationComplete.heading"))
    }

    "have content" in {
      view.select("p").text() must include("Your session has been cleared because you completed your application")
      view.select("p").text() must include(messages("applicationComplete.guidance"))
      view.getElementsByClass("govuk-button").select("a").get(0) must haveHref(routes.IndexController.onPageLoad)
    }
  }

}
