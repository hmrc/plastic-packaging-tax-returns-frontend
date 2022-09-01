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
import views.html.returns.ConfirmPackagingCreditView

class ConfirmPackagingCreditViewSpec extends ViewSpecBase  with ViewAssertions with ViewMatchers{

  val page: ConfirmPackagingCreditView   = inject[ConfirmPackagingCreditView]

  private def createView: Html =
    page(Some("200"))(request, messages)

  "View" should {

    val view = createView

    "have a title" in {
      view.select("title").text() must include("Confirm £200 credit - Submit return - Plastic Packaging Tax - GOV.UK")
      view.select("title").text() must include(messages("confirmPackagingCredit.title", "200"))
    }

    "have a header" in {
      view.select("h1").text mustBe "Confirm £200 credit"
    }

    "have a confirm button" in {
      view.getElementsByClass("govuk-button").text() mustBe  "Confirm credit amount"
      view.getElementsByClass("govuk-button").text() mustBe messages("site.confirm.credit")
    }
  }
}
