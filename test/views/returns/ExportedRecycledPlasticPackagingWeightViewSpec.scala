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

package views.returns

import forms.ExportedRecycledPlasticPackagingWeightFormProvider
import forms.returns.RecycledPlasticPackagingWeightFormProvider
import models.NormalMode
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{AnyContent, Request}
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.{FakeRequest, Injecting}
import play.twirl.api.Html
import support.{ViewAssertions, ViewMatchers}
import views.html.returns.ExportedRecycledPlasticPackagingWeightView

class ExportedRecycledPlasticPackagingWeightViewSpec extends PlaySpec
  with GuiceOneAppPerSuite
  with Injecting
  with ViewAssertions
  with ViewMatchers
  with TableDrivenPropertyChecks {

  val page: ExportedRecycledPlasticPackagingWeightView = inject[ExportedRecycledPlasticPackagingWeightView]
  val request: Request[AnyContent] = FakeRequest().withCSRFToken
  val formProvider = new ExportedRecycledPlasticPackagingWeightFormProvider()

  private val realMessagesApi: MessagesApi = inject[MessagesApi]

  implicit def messages: Messages = realMessagesApi.preferred(request)
  val weight: Long = 200

  private def createView(form: Form[Long]): Html = {
    page(form, NormalMode, weight)(request, messages)
  }

  val view = createView(formProvider().fill(weight))

  "view" should {
    "have a title" in {
      view.select("title").text() mustBe
        s"How much of your exported ${weight}kg of finished plastic packaging components contained 30% or more recycled plastic? - Submit return - Plastic Packaging Tax - GOV.UK"
      view.select("title").text() must include(messages("exportedRecycledPlasticPackagingWeight.title", weight))
    }

    "have a header" in {
      view.select("h1").text() mustBe s"How much of your exported ${weight}kg of finished plastic packaging components contained 30% or more recycled plastic?"
      view.select("h1").text() mustBe messages("exportedRecycledPlasticPackagingWeight.heading", weight)
    }

    "have a caption" in {
      view.getElementById("section-header").text() mustBe "Exported plastic packaging"
      view.getElementById("section-header").text() mustBe messages("exportedRecycledPlasticPackagingWeight.sectionHeader")
    }

    "contain paragraph content" in{
      view.getElementById("value-hint").text() must include ("1 tonne is 1,000kg.")
      view.getElementById("value-hint").text() must include (messages("exportedRecycledPlasticPackagingWeight.hint"))
    }

    "contains a text box with Kg suffix" in {
      view.getElementsByClass("govuk-input").size() mustBe 1
      view.getElementsByClass("govuk-input__suffix").text() must include("kg")
    }

    "contain save & continue button" in {

      view.getElementsByClass("govuk-button").text() mustBe  messages("site.continue")
    }

    "show an error" when {

      val testData = Table(("Test Name", "Input Value", "Error Message", "Error Constant"),
        ("entered value is empty", "", "Enter the weight, in kilograms", "exportedRecycledPlasticPackagingWeight.error.required"),
        ("entered value is less than 0", "-1", "Weight must be 0kg or more", "exportedRecycledPlasticPackagingWeight.error.outOfRange.low"),
        ("entered value is greater than threshold", "99999999999999999", "Weight must be between 0kg and 99,999,999,999kg", "exportedRecycledPlasticPackagingWeight.error.outOfRange.high"),
        ("entered value is not a number", "avbnm123", "Weight must be entered as numbers only", "exportedRecycledPlasticPackagingWeight.error.nonNumeric"),
        ("entered value contains spaces", "123 452", "Weight must not include spaces", "exportedRecycledPlasticPackagingWeight.error.spaces"),
        ("entered value is decimal number", "10.23", "Weight must not include decimals", "exportedRecycledPlasticPackagingWeight.error.wholeNumber"),
        ("entered value contain an accent", "12èü123", "Weight must be entered as numbers only", "exportedRecycledPlasticPackagingWeight.error.nonNumeric"),
        ("entered value contain a unicode character", "Ω≈123", "Weight must be entered as numbers only", "exportedRecycledPlasticPackagingWeight.error.nonNumeric"),
      )

      forAll(testData) {
        (
          testName: String,
          inputValue: String,
          errorMessage: String,
          errorConstant: String
          ) =>
          testName in {
            val invalidFormat: Map[String, String] = Map("value" -> inputValue)
            val view = createView(formProvider().bind(invalidFormat))

            view.getElementsByClass("govuk-error-summary").text() must include(errorMessage)
            view.getElementsByClass("govuk-error-summary").text() must include(messages(errorConstant))
          }
      }
    }
  }

}
