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

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{AnyContent, Request}
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.{FakeRequest, Injecting}
import play.twirl.api.Html
import views.html.returns.ReturnConfirmationView

class ReturnConfirmationViewSpec extends PlaySpec with GuiceOneAppPerSuite with Injecting {


  val page: ReturnConfirmationView = inject[ReturnConfirmationView]
  val request: Request[AnyContent] = FakeRequest().withCSRFToken

  private val realMessagesApi: MessagesApi = inject[MessagesApi]

  implicit def messages: Messages =
    realMessagesApi.preferred(request)

  private def createView(chargeRef: Option[String]): Html =
    page(chargeRef)(request, messages)

  "Submitted returns page" should {

    "have a title" in {

      val view = createView(Some("ABC123"))
      val doc: Document = Jsoup.parse(view.toString())

      doc.select("title").text() mustBe "Return submitted - Plastic Packaging Tax - GOV.UK"

    }

    "have a charge reference" in {

      val view = createView(Some("ABC123"))
      val doc: Document = Jsoup.parse(view.toString())

      doc.getElementsByClass("govuk-panel__body").text must include ("Your charge reference for this return is ABC123")

    }

    "have no charge reference" in {

      val view = createView(None)
      val doc: Document = Jsoup.parse(view.toString())

      doc.getElementsByClass("govuk-panel__body").text must include ("You have nothing to pay for this return")

    }

    "have find details paragraph" in {

      val view = createView(Some("ABC123"))
      val doc: Document = Jsoup.parse(view.toString())

      doc.getElementById("find-details").text must include (
        "You can find details of this return in the submitted returns section of your Plastic Packaging Tax (PPT) account."
      )

    }

    "have confirmation paragraph" in {

      val view = createView(Some("ABC123"))
      val doc: Document = Jsoup.parse(view.toString())

      doc.getElementById("confirmation").text must include (
        "We will not email you a confirmation."
      )

    }

    "have return link" in {

      val view = createView(Some("ABC123"))
      val doc: Document = Jsoup.parse(view.toString())

      doc.getElementById("account-link").text must include (
        "Return to your Plastic Packaging Tax account"
      )

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
}
