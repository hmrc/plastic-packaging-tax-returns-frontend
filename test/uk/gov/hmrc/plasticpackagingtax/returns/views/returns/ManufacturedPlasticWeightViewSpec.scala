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
import play.api.mvc.Call
import uk.gov.hmrc.plasticpackagingtax.returns.base.unit.UnitViewSpec
import uk.gov.hmrc.plasticpackagingtax.returns.builders.TaxReturnBuilder
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.returns.{routes => returnRoutes}
import uk.gov.hmrc.plasticpackagingtax.returns.forms.ManufacturedPlasticWeight
import uk.gov.hmrc.plasticpackagingtax.returns.forms.ManufacturedPlasticWeight.form
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.returns.manufactured_plastic_weight_page
import uk.gov.hmrc.plasticpackagingtax.returns.views.tags.ViewTest

@ViewTest
class ManufacturedPlasticWeightViewSpec extends UnitViewSpec with Matchers with TaxReturnBuilder {

  private val page = instanceOf[manufactured_plastic_weight_page]

  private val guidanceLink = Call("GET", "/guidance-link")

  private def createView(
    form: Form[ManufacturedPlasticWeight] = ManufacturedPlasticWeight.form()
  ): Document =
    page(form, guidanceLink, defaultObligation)(request, messages)

  override def exerciseGeneratedRenderingMethods(): Unit = {
    page.f(form(), guidanceLink, defaultObligation)(request, messages)
    page.render(form(), guidanceLink, defaultObligation, request, messages)
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
      view.getElementById("back-link") must haveHref(
        returnRoutes.ManufacturedPlasticController.contribution()
      )
    }

    "display title" in {
      view.select("title").text() must include(messages("returns.manufacturedPlasticWeight.title"))
    }

    "display section header" in {
      view.getElementById("section-header").text() must include(
        "April to June 2022"
      ) // from defaultObligation
    }

    "display manufactured weight label" in {
      view.select("label").text() must include(messages("returns.manufacturedPlasticWeight.title"))
    }

    "display hints on excluded items" in {
      view.select("div#totalKg-hint").text() must (
        include(messages("returns.manufacturedPlasticWeight.hint")) and
          include(messages("returns.manufacturedPlasticWeight.hint.1")) and
          include(messages("returns.manufacturedPlasticWeight.hint.2")) and
          include(messages("returns.manufacturedPlasticWeight.hint.3")) and
          include(messages("returns.manufacturedPlasticWeight.hint.4")) and
          include(messages("returns.manufacturedPlasticWeight.hint.4.link"))
      )
    }

    "link to guidance" in {
      view.select("div#totalKg-hint a").get(0) must haveHref(guidanceLink.url)
    }

    "display total weight input box" in {
      view must containElementWithID("totalKg")
    }

    "display 'Save and Continue' button" in {
      view must containElementWithID("submit")
      view.getElementById("submit").text() mustBe messages("site.button.continue")
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

      view must haveGovukFieldError("totalKg", "Enter the weight, in kilograms")
    }
  }
}
