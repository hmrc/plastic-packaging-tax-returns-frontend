/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.plasticpackagingtax.returns.views.returns

import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers
import play.api.data.Form
import uk.gov.hmrc.plasticpackagingtax.returns.base.unit.UnitViewSpec
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.returns.routes
import uk.gov.hmrc.plasticpackagingtax.returns.forms.ImportedPlasticWeight
import uk.gov.hmrc.plasticpackagingtax.returns.forms.ImportedPlasticWeight.form
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.returns.imported_plastic_weight_page
import uk.gov.hmrc.plasticpackagingtax.returns.views.tags.ViewTest

@ViewTest
class ImportedPlasticWeightViewSpec extends UnitViewSpec with Matchers {

  private val page = instanceOf[imported_plastic_weight_page]

  private def createView(
    form: Form[ImportedPlasticWeight] = ImportedPlasticWeight.form()
  ): Document =
    page(form)(request, messages)

  "Imported Plastic Weight View" should {

    "have proper messages for labels" in {
      messages must haveTranslationFor("returns.importedPlasticWeight.meta.title")
      messages must haveTranslationFor("returns.importedPlasticWeight.title")
      messages must haveTranslationFor("returns.importedPlasticWeight.hint")
      messages must haveTranslationFor("returns.importedPlasticWeight.sectionHeader")
      messages must haveTranslationFor("returns.importedPlasticWeight.total.weight")

      messages must haveTranslationFor("returns.importedPlasticWeight.details.link")
      messages must haveTranslationFor("returns.importedPlasticWeight.details.line1")
      messages must haveTranslationFor("returns.importedPlasticWeight.details.line2")
      messages must haveTranslationFor("returns.importedPlasticWeight.details.line3")
      messages must haveTranslationFor("returns.importedPlasticWeight.details.line4")
      messages must haveTranslationFor("returns.importedPlasticWeight.details.line5")

      messages must haveTranslationFor("returns.importedPlasticWeight.empty.error")
      messages must haveTranslationFor("returns.importedPlasticWeight.format.error")
      messages must haveTranslationFor("returns.importedPlasticWeight.aboveMax.error")
    }

    val view = createView()

    "validate other rendering  methods" in {
      page.f(form())(request, messages).select("title").text() must include(
        messages("returns.importedPlasticWeight.meta.title")
      )
      page.render(form(), request, messages).select("title").text() must include(
        messages("returns.importedPlasticWeight.meta.title")
      )
    }

    "contain timeout dialog function" in {

      containTimeoutDialogFunction(view) mustBe true
    }

    "display sign out link" in {

      displaySignOutLink(view)
    }

    "display 'Back' button" in {

      view.getElementById("back-link") must haveHref(
        routes.ManufacturedPlasticWeightController.displayPage()
      )
    }

    "display title" in {

      view.select("title").text() must include(messages("returns.importedPlasticWeight.meta.title"))
    }

    "display header" in {

      view.getElementById("section-header").text() must include(
        messages("returns.importedPlasticWeight.sectionHeader")
      )
    }

    "display hint" in {

      view.getElementsByClass("govuk-body-m").text() must include(
        messages("returns.importedPlasticWeight.hint")
      )
    }

    "display total weight label" in {

      view.getElementsByClass("govuk-label--s").text() must include(
        messages("returns.importedPlasticWeight.total.weight")
      )
    }

    "display total weight input box" in {

      view must containElementWithID("totalKg")
    }

    "display 'Save and Continue' button" in {

      view must containElementWithID("submit")
      view.getElementById("submit").text() mustBe "Save and Continue"
    }

    "display 'Save and come back later' button" in {

      view.getElementById("save_and_come_back_later").text() mustBe "Save and come back later"
    }
  }

  "Imported Plastic Weight View when filled" should {

    "display data in total weight input" in {

      val form = ImportedPlasticWeight
        .form()
        .fill(ImportedPlasticWeight("1000"))
      val view = createView(form)

      view.getElementById("totalKg").attr("value") mustBe "1000"
    }
  }

  "display error" when {

    "weight is not entered" in {

      val form = ImportedPlasticWeight
        .form()
        .fillAndValidate(ImportedPlasticWeight(""))
      val view = createView(form)

      view must haveGovukGlobalErrorSummary

      view must haveGovukFieldError("totalKg", "Enter an amount to continue")
    }
  }
}
