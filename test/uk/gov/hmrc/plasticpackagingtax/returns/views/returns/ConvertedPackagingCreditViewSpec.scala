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
import uk.gov.hmrc.plasticpackagingtax.returns.models.obligations.Obligation
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.returns.converted_packaging_credit_page
import uk.gov.hmrc.plasticpackagingtax.returns.views.tags.ViewTest

import java.time.LocalDate

@ViewTest
class ConvertedPackagingCreditViewSpec extends UnitViewSpec with Matchers {

  private val page             = instanceOf[converted_packaging_credit_page]
  private val balanceAvailable = Some(BigDecimal("1.23"))

  private val obligation = Obligation(fromDate = LocalDate.parse("2022-04-01"),
                                      toDate = LocalDate.parse("2022-06-30"),
                                      dueDate = LocalDate.parse("2022-09-01"),
                                      periodKey = "22C1"
  )

  private def createView(
    form: Form[ConvertedPackagingCredit] = ConvertedPackagingCredit.form(),
    balance: Option[BigDecimal] = balanceAvailable
  ): Document =
    page(form, balance, obligation)(request, messages)

  override def exerciseGeneratedRenderingMethods(): Unit = {
    page.f(form(), balanceAvailable, obligation)(request, messages)
    page.render(form(), balanceAvailable, obligation, request, messages)
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
      view.getElementById("section-header").text() must include("April to June 2022")
    }

    "display balance available when present" in {

      val view = createView()
      view.getElementsByAttributeValueMatching("for", "totalInPounds").text() must include(
        messages("returns.convertedPackagingCredit.balanceAvailable", "£1.23")
      )
    }

    "display unavailable for balance when none is present" in {

      val view = page(form(), None, obligation)(request, messages)
      view.getElementsByAttributeValueMatching("for", "totalInPounds").text() must include(
        messages("returns.convertedPackagingCredit.balanceUnavailable")
      )
    }

    "display credit claimed input box" in {

      val view = createView()
      view must containElementWithID("totalInPounds")
    }

    "display 'Save and Continue' button" in {

      val view = createView()
      view must containElementWithID("submit")
      view.getElementById("submit").text() mustBe messages("site.button.continue")
    }

  }

  "Converted Packaging Credit View when filled" should {

    "display populated credit claimed input box" in {

      val form = ConvertedPackagingCredit
        .form()
        .fill(ConvertedPackagingCredit("1001"))
      val view = createView(form)

      view.getElementById("totalInPounds").attr("value") mustBe "1001"
    }
  }

  "display error" when {

    "credit claimed is not entered" in {

      val form = ConvertedPackagingCredit
        .form()
        .fillAndValidate(ConvertedPackagingCredit(""))
      val view = createView(form)

      view must haveGovukGlobalErrorSummary

      view must haveGovukFieldError("totalInPounds", "Enter £0.00 or higher to continue")
    }
  }
}
