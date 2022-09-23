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
import controllers.routes
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.twirl.api.Html
import support.ViewMatchers
import views.html.returns.ReturnConfirmationView

class ReturnConfirmationViewSpec extends ViewSpecBase with ViewMatchers {

  val page: ReturnConfirmationView = inject[ReturnConfirmationView]

  private def createView(chargeRef: Option[String]): Html =
    page(chargeRef, false)(request, messages)

  "Submitted returns page" should {

    "have a title" in {
      val view = createView(Some("ABC123"))
      val doc: Document = Jsoup.parse(view.toString())

      doc.select("title").text() mustBe "Return submitted - Plastic Packaging Tax - GOV.UK"
      doc.getElementsByClass("govuk-panel__title").text() must include("Return submitted")
      doc.getElementsByClass("govuk-panel__title").text() must include(messages("return.confirmation.submitted"))
    }

    "have a charge reference" in {
      val view = createView(Some("ABC123"))
      val doc: Document = Jsoup.parse(view.toString())

      doc.getElementsByClass("govuk-panel__body").text must include("Your charge reference for this return is ABC123")
    }

    "have no charge reference" in {
      val view = createView(None)
      val doc: Document = Jsoup.parse(view.toString())

      doc.getElementsByClass("govuk-panel__body").text mustBe ""
    }

    "have find details paragraph" in {
      val view = createView(Some("ABC123"))
      val doc: Document = Jsoup.parse(view.toString())

      doc.getElementById("find-details").text must include(
        "You can find details of this return in the submitted returns section of your Plastic Packaging Tax (PPT) account."
      )
    }

    "have confirmation paragraph" in {
      val view = createView(Some("ABC123"))
      val doc: Document = Jsoup.parse(view.toString())

      doc.getElementById("confirmation").text must include(
        "We will not email you a confirmation."
      )
    }

    "have nothing to pay when there is no charge reference" in {
      val view = createView(None)
      val doc: Document = Jsoup.parse(view.toString())

      doc.getElementById("nothing-to-pay").text must include(
        "You have nothing to pay for this return."
      )
      doc.getElementById("nothing-to-pay").text must include(
        messages("return.confirmation.panel.empty")
      )
    }

    "contain 'Check what you owe' label" in {
      val view = createView(Some("ABC123"))
      val doc: Document = Jsoup.parse(view.toString())

      doc.getElementById("check-what-you-owe").text() must include("Check what you owe")
      doc.getElementById("check-what-you-owe").text() must include(messages("return.confirmation.checkWhatYouOwe"))
    }

    "contain 'Go to your PPT account' link" when {
      "have no charge reference" in {
        val view = createView(None)
        val doc: Document = Jsoup.parse(view.toString())

        doc.getElementById("account-link-body").text must
          include("Go to your PPT account")
        doc.getElementById("account-link-body").text must
          include(messages("return.confirmation.homePage.link2"))
        doc.getElementById("account-link").select("a").get(0) must
          haveHref(routes.IndexController.onPageLoad)
        doc.getElementsByClass("govuk-list--bullet").size() mustBe 0
      }

      "charge reference is present" in {
        val view = createView(Some("ABCVF"))
        val doc: Document = Jsoup.parse(view.toString())

        doc.getElementById("account-link-body").text must
          include("Go to your PPT account to:" )
        doc.getElementById("account-link-body").text must include(
          messages("return.confirmation.homePage.link1",
            messages("return.confirmation.homePage.link2"))
        )
        doc.getElementById("account-link").select("a").get(0) must
          haveHref(routes.IndexController.onPageLoad)

        assertBulletList(doc)
      }
    }

    "have survey link" in {

      val view = createView(Some("ABC123"))
      val doc: Document = Jsoup.parse(view.toString())

      doc.getElementById("survey-link").text must include (
        "What did you think of this service?"
      )

    }

    "have technical issue link" in {

      val view = createView(None)
      val doc: Document = Jsoup.parse(view.toString())

      doc.getElementsByClass("govuk-link hmrc-report-technical-issue ").text must include ("Is this page not working properly?")

    }
  }

  private def assertBulletList(doc: Document) = {
    doc.getElementsByClass("dashed-list-item").size() mustBe 3
    val listNode = doc.getElementsByClass("dashed-list-item")
    listNode.get(0).text() mustBe "see the total tax you owe"
    listNode.get(0).text() mustBe messages("return.confirmation.bulletList.first")
    listNode.get(1).text() mustBe "make a payment"
    listNode.get(1).text() mustBe messages("return.confirmation.bulletList.second")
    listNode.get(2).text() mustBe "view payment due date"
    listNode.get(2).text() mustBe messages("return.confirmation.bulletList.third")
  }
}
