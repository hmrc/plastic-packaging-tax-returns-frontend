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
import uk.gov.hmrc.plasticpackagingtax.returns.forms.{
  ManufacturedPlastic,
  ManufacturedPlasticWeight
}
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.returns.{
  manufactured_plastic_page,
  manufactured_plastic_weight_page
}
import uk.gov.hmrc.plasticpackagingtax.returns.views.tags.ViewTest

@ViewTest
class ManufacturedPlasticViewSpec extends UnitViewSpec with Matchers with TaxReturnBuilder {

  private val page = instanceOf[manufactured_plastic_page]

  private val guidanceLink = Call("GET", "/guidance-link")

  private def createView(form: Form[Boolean] = ManufacturedPlastic.form()): Document =
    page(form, guidanceLink, defaultObligation)(request, messages)

  override def exerciseGeneratedRenderingMethods(): Unit = {
    page.f(ManufacturedPlastic.form(), guidanceLink, defaultObligation)(request, messages)
    page.render(ManufacturedPlastic.form(), guidanceLink, defaultObligation, request, messages)
  }

  "Manufactured Plastic View" should {

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
      view.select("title").text() must include(messages("returns.manufacturedPlastic.title"))
    }

    "display section header" in {
      view.getElementById("section-header").text() must include(
        "April to June 2022"
      ) // from defaultObligation
    }

    "display manufactured question" in {
      view.select("h1").get(0).text() must include(messages("returns.manufacturedPlastic.title"))
    }

    "display hints" in {
      view.select("div#answer-hint").text() must (
        include(messages("returns.manufacturedPlastic.details.link")) and
          include(messages("returns.manufacturedPlastic.details.line1")) and
          include(messages("returns.manufacturedPlastic.details.line2.link"))
      )
    }

    "link to guidance" in {
      view.select("div#answer-hint a").get(0) must haveHref(guidanceLink.url)
    }

    "display yes/no radios" in {
      view.select("div.govuk-radios input").get(0) must haveAttribute("value", "yes")
      view.select("div.govuk-radios input").get(1) must haveAttribute("value", "no")
    }

    "display 'Save and Continue' button" in {
      view must containElementWithID("submit")
      view.getElementById("submit").text() mustBe messages("site.button.continue")
    }

    "preselect the radio" when {
      "true recorded in model" in {
        val form = ManufacturedPlastic.form().fill(true)
        val view = createView(form)

        view.select("div.govuk-radios input").get(0) must haveAttribute("checked")
      }
      "false recorded in model" in {
        val form = ManufacturedPlastic.form().fill(false)
        val view = createView(form)

        view.select("div.govuk-radios input").get(1) must haveAttribute("checked")
      }
    }

    "display error" when {
      "errors present in the form" in {
        val form = ManufacturedPlastic.form().bindFromRequest(Map("answer" -> Seq("")))
        val view = createView(form)

        view must haveGovukGlobalErrorSummary
        view must haveGovukFieldError("answer",
                                      messages("returns.manufacturedPlastic.details.empty.error")
        )
      }
    }
  }

}
