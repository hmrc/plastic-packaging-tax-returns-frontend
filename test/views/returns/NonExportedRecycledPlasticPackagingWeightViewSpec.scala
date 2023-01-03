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
import forms.returns.NonExportedRecycledPlasticPackagingWeightFormProvider
import models.Mode.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import play.twirl.api.Html
import support.{ViewAssertions, ViewMatchers}
import viewmodels.PrintLong
import views.html.returns.NonExportedRecycledPlasticPackagingWeightView

class NonExportedRecycledPlasticPackagingWeightViewSpec extends ViewSpecBase with ViewAssertions with ViewMatchers {

  val page: NonExportedRecycledPlasticPackagingWeightView = inject[NonExportedRecycledPlasticPackagingWeightView]
  val form: Form[Long] = new NonExportedRecycledPlasticPackagingWeightFormProvider()()
  val amount = 321L
  val amountAsKg = amount.asKg

  private def createView(form: Form[Long] = form, directlyExportedAnswer: Boolean = true): Html =
    page(form, NormalMode, amount, directlyExportedAnswer)(request, messages)

  "NonExportedRecycledPlasticPackagingWeightView" should {
    val view: Html = createView()
    val doc: Document = Jsoup.parse(view.toString())

    "have a title" when {
      "directly exported component answer is Yes" in {
        view.select("title").text mustBe
          s"Out of the $amountAsKg of finished plastic packaging components that you did not export, how much contained 30% or more recycled plastic? - Submit return - Plastic Packaging Tax - GOV.UK"
        view.select("title").text must include(messages("NonExportRecycledPlasticPackagingWeight.heading", amountAsKg))
      }

      "directly exported component answer is No" in {
        val newView = createView(directlyExportedAnswer = false)

        newView.select("title").text() mustBe
          s"How much of your $amountAsKg of finished plastic packaging components contained 30% or more recycled plastic? - Submit return - Plastic Packaging Tax - GOV.UK"
        newView.select("title").text must include(messages("NonExportRecycledPlasticPackagingWeight.directly.export.no.heading", amountAsKg))

      }
    }

    "have a heading" when {
      "directly exported component answer is Yes" in {
        view.select("h1").text mustBe
          s"Out of the $amountAsKg of finished plastic packaging components that you did not export, how much contained 30% or more recycled plastic?"
        view.select("h1").text mustBe messages("NonExportRecycledPlasticPackagingWeight.heading", amountAsKg)
      }

      "directly exported component answer is No" in {
        val newView = createView(directlyExportedAnswer = false)

        newView.select("h1").text mustBe
          s"How much of your $amountAsKg of finished plastic packaging components contained 30% or more recycled plastic?"
        newView.select("h1").text must include(messages("NonExportRecycledPlasticPackagingWeight.directly.export.no.heading", amountAsKg))
      }
    }

    "have a hint" in {

      doc.getElementById("value-hint").text must include(messages("NonExportRecycledPlasticPackagingWeight.hint"))
    }

    "have a caption" in {

      view.getElementById("section-header").text() mustBe messages("caption.non.exported.plastic")
    }

    "contain paragraph content" in {

      view.getElementsByClass("govuk-body").text() must include(messages("NonExportRecycledPlasticPackagingWeight.paragraph"))
    }
    "contain save & continue button" in {

      view.getElementsByClass("govuk-button").text() mustBe messages("site.continue")
    }
    "display error" when {
      "negative number submitted" in {
        val view: Html = createView(form.fillAndValidate(-1))
        val doc: Document = Jsoup.parse(view.toString())

        doc.text() must include(messages("NonExportRecycledPlasticPackagingWeight.error.outOfRange.low"))
      }

      "number submitted is greater than maximum" in {
        val view: Html = createView(form.fillAndValidate(999999999999L))
        val doc: Document = Jsoup.parse(view.toString())

        doc.text() must include(messages("NonExportRecycledPlasticPackagingWeight.error.outOfRange.high"))
      }
    }
  }
}
