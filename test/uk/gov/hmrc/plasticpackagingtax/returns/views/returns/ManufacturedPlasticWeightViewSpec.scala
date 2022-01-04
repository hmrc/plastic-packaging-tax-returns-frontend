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
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.returns.{routes => returnRoutes}
import uk.gov.hmrc.plasticpackagingtax.returns.forms.ManufacturedPlasticWeight
import uk.gov.hmrc.plasticpackagingtax.returns.forms.ManufacturedPlasticWeight.form
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.returns.manufactured_plastic_weight_page
import uk.gov.hmrc.plasticpackagingtax.returns.views.tags.ViewTest

@ViewTest
class ManufacturedPlasticWeightViewSpec extends UnitViewSpec with Matchers {

  private val page = instanceOf[manufactured_plastic_weight_page]

  private def createView(
    form: Form[ManufacturedPlasticWeight] = ManufacturedPlasticWeight.form()
  ): Document =
    page(form)(request, messages)

  override def exerciseGeneratedRenderingMethods(): Unit = {
    page.f(form())(request, messages)
    page.render(form(), request, messages)
  }

  "Manufactured Plastic Weight View" should {

    val view = createView()

    "contain timeout dialog function" in {

      containTimeoutDialogFunction(view) mustBe true
    }

    "display sign out link" in {

      displaySignOutLink(view)
    }

    "display 'Back' button" in {

      view.getElementById("back-link") must haveHref(returnRoutes.StartController.displayPage())
    }

    "display title" in {

      view.select("title").text() must include(
        messages("returns.manufacturedPlasticWeight.meta.title")
      )
    }

    "display header" in {

      view.getElementById("section-header").text() must include(
        messages("returns.manufacturedPlasticWeight.sectionHeader")
      )
    }

    "display total weight label" in {

      view.getElementsByClass("govuk-label--s").text() must include(
        messages("returns.manufacturedPlasticWeight.total.weight")
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

  "Manufactured Plastic Weight View when filled" should {

    "display data in total weight input" in {

      val form = ManufacturedPlasticWeight
        .form()
        .fill(ManufacturedPlasticWeight("1000"))
      val view = createView(form)

      view.getElementById("totalKg").attr("value") mustBe "1000"
    }
  }

  "display error" when {

    "weight is not entered" in {

      val form = ManufacturedPlasticWeight
        .form()
        .fillAndValidate(ManufacturedPlasticWeight(""))
      val view = createView(form)

      view must haveGovukGlobalErrorSummary

      view must haveGovukFieldError("totalKg", "Enter an amount to continue")
    }
  }
}
