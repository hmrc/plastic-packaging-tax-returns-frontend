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

package uk.gov.hmrc.plasticpackagingtax.returns.views.returns

import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers
import play.api.data.Form
import uk.gov.hmrc.plasticpackagingtax.returns.base.unit.UnitViewSpec
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.returns.routes
import uk.gov.hmrc.plasticpackagingtax.returns.forms.ImportedPlastic
import uk.gov.hmrc.plasticpackagingtax.returns.forms.ImportedPlastic.form
import uk.gov.hmrc.plasticpackagingtax.returns.models.obligations.Obligation
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.returns.imported_plastic_page
import uk.gov.hmrc.plasticpackagingtax.returns.views.tags.ViewTest

import java.time.LocalDate

@ViewTest
class ImportedPlasticPageViewSpec extends UnitViewSpec with Matchers {

  private val page = instanceOf[imported_plastic_page]

  private val obligation = Obligation(LocalDate.of(2022, 4, 1),
                                      LocalDate.of(2022, 7, 31),
                                      LocalDate.of(2023, 3, 31),
                                      "22C2"
  )

  private def createView(form: Form[Boolean] = ImportedPlastic.form()): Document =
    page(form, obligation)(request, messages)

  override def exerciseGeneratedRenderingMethods(): Unit = {
    page.f(form(), obligation)(request, messages)
    page.render(form(), obligation, request, messages)
  }

  "Imported Plastic Page View" should {

    val view = createView()

    "contain timeout dialog function" in {

      containTimeoutDialogFunction(view) mustBe true
    }

    "display sign out link" in {

      displaySignOutLink(view)
    }

    "display 'Back' button" in {

      view.getElementById("back-link") must haveHref(
        routes.ManufacturedPlasticController.contribution()
      )
    }

    "display title" in {

      view.select("title").text() must include(
        messages("returns.importedPlasticWeight.component.title")
      )
    }

    "display header" in {

      view.getElementById("section-header").text() must include(
        messages("returns.sectionHeader", "April", "July", "2022")
      )
    }

    "display radio buttons for contribution" in {

      view must containElementWithID("answer")
    }

    "display 'Save and Continue' button" in {

      view must containElementWithID("submit")
      view.getElementById("submit").text() mustBe messages("site.button.saveAndContinue")
    }

  }

  "Imported Plastic Page when filled" should {

    "display data in contribution input" in {

      val form = ImportedPlastic
        .form()
        .fill(true)
      val view = createView(form)

      view.getElementById("answer").attr("value") mustBe "yes"
    }
  }
}
