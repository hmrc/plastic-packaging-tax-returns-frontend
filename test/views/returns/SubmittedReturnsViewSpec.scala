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
import models.returns.TaxReturnObligation
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.twirl.api.Html
import views.html.returns.SubmittedReturnsView

import java.time.LocalDate

class SubmittedReturnsViewSpec extends ViewSpecBase {


  val page: SubmittedReturnsView = inject[SubmittedReturnsView]
  val aSequenceOfObligations: Seq[TaxReturnObligation] = Seq(TaxReturnObligation(
    LocalDate.now(),
    LocalDate.now().plusWeeks(12),
    LocalDate.now().plusWeeks(16),
    "PK1"), TaxReturnObligation(
    LocalDate.now(),
    LocalDate.now().plusWeeks(4),
    LocalDate.now().plusWeeks(8),
    "PK2"))

  private def createView(previousReturn: Seq[TaxReturnObligation], amendFeatureEnabled: Boolean): Html =
    page(previousReturn, amendFeatureEnabled)(request, messages)

  "Submitted returns page" should {

    "have a title without amend returns if feature is not enabled" in {
      val view = createView(aSequenceOfObligations, amendFeatureEnabled = false)
      val doc: Document = Jsoup.parse(view.toString())

      doc.select("title")
        .text() mustBe "View submitted returns - Submit return - Plastic Packaging Tax - GOV.UK"
    }

    "have a title with amend returns if feature is enabled" in {
      val view = createView(aSequenceOfObligations, amendFeatureEnabled = true)
      val doc: Document = Jsoup.parse(view.toString())

      doc.select("title")
        .text() mustBe "View or amend submitted returns - Submit return - Plastic Packaging Tax - GOV.UK"
    }

    "tell you when you have no previous tax returns" in {
      val notSubmittedReturns = Seq.empty
      val view = createView(notSubmittedReturns, amendFeatureEnabled = false)
      val doc: Document = Jsoup.parse(view.toString())

      doc.getElementById("previous-returns-list").text() mustBe "You have not submitted any returns yet."

    }
    "have a previous return" in {
      val previousReturn = Seq(aSequenceOfObligations(0))
      val view = createView(previousReturn, amendFeatureEnabled = false)
      val doc: Document = Jsoup.parse(view.toString())

      doc.getElementsByAttributeValue("id","return-item-1").size() mustBe 1
    }

    "have multiple returns in list" in {
      val twoSubmittedReturns = aSequenceOfObligations
      val view = createView(twoSubmittedReturns, amendFeatureEnabled = false)
      val doc: Document = Jsoup.parse(view.toString())

      doc.getElementsByAttributeValueStarting("id","return-item-").size() mustBe 2

    }

    "have return link" in {

      val view = createView(aSequenceOfObligations, amendFeatureEnabled = false)
      val doc: Document = Jsoup.parse(view.toString())

      doc.getElementById("account-homepage").text must include (
        "Go to your Plastic Packaging Tax account"
      )

    }

    "have technical issue link" in {

      val view = createView(aSequenceOfObligations, amendFeatureEnabled = false)
      val doc: Document = Jsoup.parse(view.toString())

      doc.getElementsByClass("govuk-link hmrc-report-technical-issue ").text must include ("Is this page not working properly?")

    }
  }
}
