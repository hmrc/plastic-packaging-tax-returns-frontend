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

package views.returns

import base.ViewSpecBase
import play.twirl.api.Html
import support.{ViewAssertions, ViewMatchers}
import views.html.returns.AlreadySubmittedView

class AlreadySubmittedViewSpec extends ViewSpecBase  with ViewAssertions with ViewMatchers {

  private val page: AlreadySubmittedView = inject[AlreadySubmittedView]

  private val period = "April to June 2022"
  private def createView: Html = {
    page(period)(request, messages)
  }

  "view" should {
    "have title" in {
      val title = createView.select("title").text()

      title mustBe "We’ve received your return - Submit return - Plastic Packaging Tax - GOV.UK"
      title must include(messages("return-already-submitted.heading"))
    }

    "have a heading" in {
      val heading = createView.select("h1").text()

      heading mustBe "We’ve received your return"
      heading must include(messages("return-already-submitted.heading"))
    }

    "have a hint" in {
      val hint = createView.getElementsByClass("govuk-body").text()

      hint must include(s"We’ve received your return for $period. You do not need to submit it again.")
      hint must include(messages("return-already-submitted.para.1", period))
      hint must include("You can:")
      hint must include(messages("return-already-submitted.para.2"))

      assertBulletList()

    }
  }

  private def assertBulletList(): Unit = {
    val bulletList = createView.getElementsByClass("govuk-list--bullet")
    bulletList.text() must include("view or amend submitted returns")
    bulletList.text() must include(messages("return-already-submitted.list.1"))
    bulletList.text() must include("check what you owe")
    bulletList.text() must include(messages("return-already-submitted.list.2"))

    val links = bulletList.select("a")
    links.get(0) must haveHref(controllers.amends.routes.SubmittedReturnsController.onPageLoad)
    links.get(1) must haveHref(controllers.routes.IndexController.onPageLoad)
  }
}
