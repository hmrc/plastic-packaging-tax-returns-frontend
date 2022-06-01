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

import models.returns.TaxReturnObligation
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{AnyContent, Request}
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.{FakeRequest, Injecting}
import play.twirl.api.Html
import views.html.returns.SubmittedReturnsView

import java.time.LocalDate

class SubmittedReturnsViewSpec extends PlaySpec with GuiceOneAppPerSuite with Injecting {


  val page: SubmittedReturnsView = inject[SubmittedReturnsView]
  val request: Request[AnyContent] = FakeRequest().withCSRFToken
  val aSequenceOfObligations: Seq[TaxReturnObligation] = Seq(TaxReturnObligation(
    LocalDate.now(),
    LocalDate.now().plusWeeks(12),
    LocalDate.now().plusWeeks(16),
    "PK1"), TaxReturnObligation(
    LocalDate.now(),
    LocalDate.now().plusWeeks(4),
    LocalDate.now().plusWeeks(8),
    "PK2"))

  private val realMessagesApi: MessagesApi = inject[MessagesApi]

  implicit def messages: Messages =
    realMessagesApi.preferred(request)

  private def createView(previousReturn: Seq[TaxReturnObligation]): Html =
    page(previousReturn)(request, messages)

  "Submitted returns page" should {

    "has a title" in {
      val view = createView(aSequenceOfObligations)
      val doc: Document = Jsoup.parse(view.toString())

      doc.select("title").text() mustBe "View submitted returns - Plastic Packaging Tax - GOV.UK"
    }

    "tell you when you have no previous tax returns" in {
      val notSubmittedReturns = Seq.empty
      val view = createView(notSubmittedReturns)
      val doc: Document = Jsoup.parse(view.toString())

      doc.getElementById("previous-returns-list").text() mustBe "You have not submitted any returns yet."

    }
    "have a previous return" in {
      val previousReturn = Seq(aSequenceOfObligations(0))
      val view = createView(previousReturn)
      val doc: Document = Jsoup.parse(view.toString())

      doc.getElementsByAttributeValue("id","return-item").size() mustBe 1

    }

    "have multiple returns in list" in {
      val twoSubmittedReturns = aSequenceOfObligations
      val view = createView(twoSubmittedReturns)
      val doc: Document = Jsoup.parse(view.toString())

      doc.getElementsByAttributeValue("id","return-item").size() mustBe 2

    }
  }
}
