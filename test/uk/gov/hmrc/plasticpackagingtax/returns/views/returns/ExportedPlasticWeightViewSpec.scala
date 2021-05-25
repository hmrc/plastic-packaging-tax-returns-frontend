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
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.returns.{routes => returnRoutes}
import uk.gov.hmrc.plasticpackagingtax.returns.forms.ExportedPlasticWeight
import uk.gov.hmrc.plasticpackagingtax.returns.forms.ExportedPlasticWeight.form
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.returns.exported_plastic_weight_page
import uk.gov.hmrc.plasticpackagingtax.returns.views.tags.ViewTest

@ViewTest
class ExportedPlasticWeightViewSpec extends UnitViewSpec with Matchers {

  private val page = instanceOf[exported_plastic_weight_page]

  private def createView(
    form: Form[ExportedPlasticWeight] = ExportedPlasticWeight.form()
  ): Document =
    page(form)(request, messages)

  "Exported Plastic Weight View" should {

    "have proper messages for labels" in {
      messages must haveTranslationFor("returns.exportedPlasticWeight.title")
      messages must haveTranslationFor("returns.exportedPlasticWeight.sectionHeader")
      messages must haveTranslationFor("returns.exportedPlasticWeight.total.weight.label")

      messages must haveTranslationFor("returns.exportedPlasticWeight.details.link")
      messages must haveTranslationFor("returns.exportedPlasticWeight.details.line1")
      messages must haveTranslationFor("returns.exportedPlasticWeight.details.line2")
      messages must haveTranslationFor("returns.exportedPlasticWeight.details.line3")
      messages must haveTranslationFor("returns.exportedPlasticWeight.details.line4")
      messages must haveTranslationFor("returns.exportedPlasticWeight.details.line5")

      messages must haveTranslationFor("returns.exportedPlasticWeight.totalKg.empty.error")
      messages must haveTranslationFor("returns.exportedPlasticWeight.totalKg.format.error")
      messages must haveTranslationFor("returns.exportedPlasticWeight.weight.aboveMax.error")
    }

    val view = createView()

    "validate other rendering  methods" in {
      page.f(form())(request, messages).select("title").text() must include(
        messages("returns.exportedPlasticWeight.title")
      )
      page.render(form(), request, messages).select("title").text() must include(
        messages("returns.exportedPlasticWeight.title")
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
        returnRoutes.HumanMedicinesPlasticWeightController.displayPage()
      )
    }

    "display title" in {

      view.select("title").text() must include(messages("returns.exportedPlasticWeight.title"))
    }

    "display header" in {

      view.getElementsByClass("govuk-caption-xl").text() must include(
        messages("returns.exportedPlasticWeight.sectionHeader")
      )
    }

    "display total weight label" in {

      view.getElementsByClass("govuk-label--s").text() must include(
        messages("returns.exportedPlasticWeight.total.weight.label")
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

  "Exported Plastic Weight View when filled" should {

    "display data in total weight input" in {

      val form = ExportedPlasticWeight
        .form()
        .fill(ExportedPlasticWeight("1000"))
      val view = createView(form)

      view.getElementById("totalKg").attr("value") mustBe "1000"
    }
  }

  "display error" when {

    "weight is not entered" in {

      val form = ExportedPlasticWeight
        .form()
        .fillAndValidate(ExportedPlasticWeight(""))
      val view = createView(form)

      view must haveGovukGlobalErrorSummary

      view must haveGovukFieldError("totalKg", "Enter an amount to continue")
    }
  }
}
