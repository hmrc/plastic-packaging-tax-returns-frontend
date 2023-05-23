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

package views.returns

import base.ViewSpecBase
import config.FrontendAppConfig
import forms.returns.credits.DoYouWantToClaimFormProvider
import models.Mode.NormalMode
import models.returns.TaxReturnObligation
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.twirl.api.Html
import support.{ViewAssertions, ViewMatchers}
import views.ViewUtils
import views.html.returns.credits.DoYouWantToClaimView

import java.time.LocalDate

class DoYouWantToClaimViewSpec extends ViewSpecBase with ViewAssertions with ViewMatchers {

  val form = new DoYouWantToClaimFormProvider()()
  val page = inject[DoYouWantToClaimView]
  val appConfig = inject[FrontendAppConfig]

  val aTaxObligation: TaxReturnObligation = TaxReturnObligation(
    LocalDate.of(2022,7,5),
    LocalDate.of(2022,10,5),
    LocalDate.of(2023,1,5),
    "PK1")

  private def createView: Html =
    page(form, aTaxObligation, NormalMode)(request, messages)

  "view" should {
    val view = createView
    "have a title" in {
      view.select("title").text must include(messages("do-you-want-to-claim.title"))
    }

    "have a heading" in {
      view.select("h1").text mustBe messages("do-you-want-to-claim.title")
    }

    "have all of the content" in {

      view.text() must include(messages("do-you-want-to-claim.p1"))
      view.text() must include(messages("do-you-want-to-claim.p1.b1"))
      view.text() must include(messages("do-you-want-to-claim.p1.b2"))
      view.text() must include(messages("do-you-want-to-claim.p2"))
      view.text() must include(messages("do-you-want-to-claim.p3"))
      view.text() must include(messages("do-you-want-to-claim.p3.b1"))
      view.text() must include(messages("do-you-want-to-claim.p3.b2"))
      view.text() must include(messages("do-you-want-to-claim.p4"))
    }

    "have the links to guidance" in {
      view.getElementById("credit-info-link").text() mustBe messages("do-you-want-to-claim.p4.b1.a")
      view.getElementById("credit-info-link").attr("href") mustBe "https://www.gov.uk/guidance/claim-a-credit-or-defer-paying-plastic-packaging-tax#components-youve-already-paid-tax-on-which-are-exported-or-converted"

      view.getElementById("records-info-link").text() mustBe messages("do-you-want-to-claim.p4.b2.a")
      view.getElementById("records-info-link").attr("href") mustBe "https://www.gov.uk/guidance/record-keeping-and-accounts-for-plastic-packaging-tax#records-to-keep-to-claim-a-credit"
    }

    "have a the radio options" in {

      view.select(".govuk-radios__item").get(0).text mustBe messages("site.yes")
      view.select(".govuk-radios__item").get(1).text mustBe messages("site.no")
    }

    "contain save & continue button" in {
      view.getElementsByClass("govuk-button").text() mustBe "Save and continue"
    }

    "have the error summary when form error" in {
      val errorForm = form.withError("error-key", "error message")
      val errorView = page(errorForm, aTaxObligation, NormalMode)(request, messages)

      val doc: Document = Jsoup.parse(errorView.toString())

      doc.getElementsByClass("govuk-error-summary").text() must include("error message")
    }
  }
}
