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

package views

import base.ViewSpecBase
import controllers.routes
import support.{ViewAssertions, ViewMatchers}
import views.html.JourneyRecoveryView

class JourneyRecoveryViewSpec extends ViewSpecBase with ViewAssertions with ViewMatchers {

  val page = inject[JourneyRecoveryView]
  val view = page()(request, messages)

  "JourneyRecoveryStartAgainView" should {

    "have a title" in {

      view.select("title").text() must
        include("Sorry, there is a problem with the service - Submit return - Plastic Packaging Tax - GOV.UK")
      view.select("title").text() must
        include(messages("journeyRecovery.startAgain.title"))
    }

    "have a header" in {
      view.select("h1").text() must include("Sorry, there is a problem with the service")
      view.select("h1").text() must include(messages("journeyRecovery.startAgain.heading"))
    }

    "contain go to your home account link" in {
      view.getElementById("account-link-body").text() must include("Go to your Plastic Packaging Tax account.")
      view.getElementById("account-link-body").text() must include(
        messages("journeyRecovery.startAgain.link.1", messages("journeyRecovery.startAgain.link.2"))
      )

      view.getElementById("account-link")
        .select("a").get(0) must haveHref(routes.IndexController.onPageLoad)
    }
  }

}
