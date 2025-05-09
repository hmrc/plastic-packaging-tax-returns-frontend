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
import forms.returns.ExportedPlasticPackagingWeightFormProvider
import models.Mode.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.twirl.api.Html
import support.{ViewAssertions, ViewMatchers}
import uk.gov.hmrc.scalatestaccessibilitylinter.AccessibilityMatchers
import views.html.returns.ExportedPlasticPackagingWeightView

class ExportedPlasticPackagingWeightViewSpec
    extends ViewSpecBase
    with ViewAssertions
    with ViewMatchers
    with AccessibilityMatchers {

  val form = new ExportedPlasticPackagingWeightFormProvider()()
  val page = inject[ExportedPlasticPackagingWeightView]

  val totalPlastic = 1234L

  private def createView: Html =
    page(form, NormalMode, totalPlastic)(request, messages)

  "ExportedPlasticPackagingWeightView" should {
    val view = createView

    "have a title" in {

      view.select("title").text mustBe
        "How much of your 1,234kg of finished plastic packaging components did you export, or do you intend to export within 12 months? - Submit return - Plastic Packaging Tax - GOV.UK"
    }

    "have a heading" in {
      view.select("h1").text mustBe
        "How much of your 1,234kg of finished plastic packaging components did you export, or do you intend to export within 12 months?"
    }

    "have a caption" in {

      view.getElementById("section-header").text() mustBe messages("caption.exported.plastic")
    }

    "have a hint" in {
      val view: Html    = createView
      val doc: Document = Jsoup.parse(view.toString())

      doc.getElementById("value-hint").text must include(messages("1 tonne is 1,000kg."))
    }

    "contain paragraph content" in {

      view.getElementsByClass("govuk-body").text() must include(
        messages("Plastic packaging you export will show as a deduction on your tax calculation.")
      )
    }
    "contain save & continue button" in {

      view.getElementsByClass("govuk-button").text() must include(messages("site.continue"))
    }
  }

}
