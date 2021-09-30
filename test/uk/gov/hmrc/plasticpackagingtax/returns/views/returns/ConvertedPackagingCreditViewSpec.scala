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
import uk.gov.hmrc.plasticpackagingtax.returns.forms.ConvertedPackagingCredit
import uk.gov.hmrc.plasticpackagingtax.returns.forms.ConvertedPackagingCredit.form
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.returns.converted_packaging_credit_page
import uk.gov.hmrc.plasticpackagingtax.returns.views.tags.ViewTest

@ViewTest
class ConvertedPackagingCreditViewSpec extends UnitViewSpec with Matchers {

  private val page = instanceOf[converted_packaging_credit_page]

  private def createView(
    form: Form[ConvertedPackagingCredit] = ConvertedPackagingCredit.form()
  ): Document =
    page(form)(request, messages)

  override def exerciseGeneratedRenderingMethods(): Unit = {
    page.f(form())(request, messages)
    page.render(form(), request, messages)
  }

  "Converted Packaging Credit View" should {

    "have proper messages for labels" in {
      messages must haveTranslationFor("returns.convertedPackagingCredit.meta.title")
      messages must haveTranslationFor("returns.convertedPackagingCredit.title")
      messages must haveTranslationFor("returns.convertedPackagingCredit.sectionHeader")
      messages must haveTranslationFor("returns.convertedPackagingCredit.total.weight")

      messages must haveTranslationFor("returns.convertedPackagingCredit.details.link")
      messages must haveTranslationFor("returns.convertedPackagingCredit.details.line1")
      messages must haveTranslationFor("returns.convertedPackagingCredit.details.line2")

      messages must haveTranslationFor("returns.convertedPackagingCredit.empty.error")
      messages must haveTranslationFor("returns.convertedPackagingCredit.format.error")
      messages must haveTranslationFor("returns.convertedPackagingCredit.aboveMax.error")
    }

    val view = createView()

    "display 'Back' button" in {

      view.getElementById("back-link") must haveHref(
        routes.RecycledPlasticWeightController.displayPage()
      )
    }

    "display title" in {

      view.select("title").text() must include(
        messages("returns.convertedPackagingCredit.meta.title")
      )
    }

    "display header" in {

      view.getElementById("section-header").text() must include(
        messages("returns.convertedPackagingCredit.sectionHeader")
      )
    }

    "display total weight label" in {

      view.getElementsByAttributeValueMatching("for", "totalInPence").text() must include(
        messages("returns.convertedPackagingCredit.total.weight")
      )
    }

    "display total weight input box" in {

      view must containElementWithID("totalInPence")
    }

    "display 'Save and Continue' button" in {

      view must containElementWithID("submit")
      view.getElementById("submit").text() mustBe "Save and Continue"
    }

    "display 'Save and come back later' button" in {

      view.getElementById("save_and_come_back_later").text() mustBe "Save and come back later"
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
