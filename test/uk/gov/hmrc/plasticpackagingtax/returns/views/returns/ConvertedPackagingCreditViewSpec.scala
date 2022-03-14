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
import uk.gov.hmrc.plasticpackagingtax.returns.forms.ConvertedPackagingCredit
import uk.gov.hmrc.plasticpackagingtax.returns.forms.ConvertedPackagingCredit.form
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.returns.converted_packaging_credit_page
import uk.gov.hmrc.plasticpackagingtax.returns.views.tags.ViewTest

@ViewTest
class ConvertedPackagingCreditViewSpec extends UnitViewSpec with Matchers {

  private val page             = instanceOf[converted_packaging_credit_page]
  private val balanceAvailable = Some(BigDecimal("1.23"))

  private def createView(
    form: Form[ConvertedPackagingCredit] = ConvertedPackagingCredit.form(),
    balance: Option[BigDecimal] = balanceAvailable
  ): Document =
    page(form, balance)(request, messages)

  override def exerciseGeneratedRenderingMethods(): Unit = {
    page.f(form(), balanceAvailable)(request, messages)
    page.render(form(), balanceAvailable, request, messages)
  }

  "Converted Packaging Credit View" should {

    "display 'Back' button" in {

      val view = createView()
      view.getElementById("back-link") must haveHref(
        routes.RecycledPlasticWeightController.displayPage()
      )
    }

    "display title" in {

      val view = createView()
      view.select("title").text() must include(
        messages("returns.convertedPackagingCredit.meta.title")
      )
    }

    "display header" in {

      val view = createView()
      view.getElementById("section-header").text() must include(
        messages("returns.convertedPackagingCredit.sectionHeader")
      )
    }

    "display balance available when present" in {

      val view = createView()
      view.getElementsByAttributeValueMatching("for", "totalInPence").text() must include(
        "Credit balance £1.23"
      )
    }

    "display unavailable for balance when none is present" in {

      val view = page(form(), None)(request, messages)
      view.getElementsByAttributeValueMatching("for", "totalInPence").text() must include(
        "£unavailable"
      )
    }

    "display total weight text" in {

      val view = createView()
      view.getElementsByClass("govuk-body").text() must include(
        messages("returns.convertedPackagingCredit.total.weight")
      )
    }

    "display total weight input box" in {

      val view = createView()
      view must containElementWithID("totalInPence")
    }

    "display 'Save and Continue' button" in {

      val view = createView()
      view must containElementWithID("submit")
      view.getElementById("submit").text() mustBe messages("site.button.continue")
    }

  }

  "Converted Packaging Credit View when filled" should {

    "display data in total weight input" in {

      val form = ConvertedPackagingCredit
        .form()
        .fill(ConvertedPackagingCredit("1001"))
      val view = createView(form)

      view.getElementById("totalInPence").attr("value") mustBe "1001"
    }
  }

  "display error" when {

    "weight is not entered" in {

      val form = ConvertedPackagingCredit
        .form()
        .fillAndValidate(ConvertedPackagingCredit(""))
      val view = createView(form)

      view must haveGovukGlobalErrorSummary

      view must haveGovukFieldError("totalInPence", "Enter £0.00 or higher to continue")
    }
  }
}
