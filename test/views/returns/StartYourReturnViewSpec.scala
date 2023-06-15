/*
 * Copyright 2023 HM Revenue & Customs
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

import base.ViewSpecBase
import forms.returns.StartYourReturnFormProvider
import models.returns.TaxReturnObligation
import play.api.data.Form
import play.twirl.api.Html
import support.{ViewAssertions, ViewMatchers}
import views.html.returns.StartYourReturnView

import java.time.LocalDate

class StartYourReturnViewSpec extends ViewSpecBase with ViewAssertions with ViewMatchers {

  val form = new StartYourReturnFormProvider()()
  val page = inject[StartYourReturnView]

  val aTaxObligation: TaxReturnObligation = TaxReturnObligation(
    LocalDate.of(2022,7,5),
    LocalDate.of(2022,10,5),
    LocalDate.of(2023,1,5),
    "PK1")

  private def createView(form: Form[Boolean] = form, isFirstReturn: Boolean): Html =
    page(form, aTaxObligation, isFirstReturn)(request, messages)

  "StartYourReturnView" should {

    "when not first return" when {
      val view = createView(isFirstReturn = false)

      "have a title" in {
        view.select("title").text mustBe
          "Do you want to start your return for the July to October 2022 accounting period? - Submit return - Plastic Packaging Tax - GOV.UK"
        view.select("title").text must include(messages("startYourReturn.title", "July", "October", "2022"))
      }

      "have a heading" in {
        view.select("h1").text mustBe
          "Do you want to start your return for the July to October 2022 accounting period?"

      }
    }

    "when is first return"  when {
      val view = createView(isFirstReturn = true)

      "have a title" in {
        view.select("title").text mustBe
          "Your tax start date is 5 July 2022. Do you want to start your return for 5 July 2022 to 5 October 2022? - Submit return - Plastic Packaging Tax - GOV.UK"

        view.select("title").text must include(
          messages(
            "startYourReturn.firstReturn.title",
            "5 July 2022",
            "5 July 2022",
            "5 October 2022"
          ))
      }

      "have a heading" in {
        view.select("h1").text mustBe
          "Your tax start date is 5 July 2022. Do you want to start your return for 5 July 2022 to 5 October 2022?"

        view.select("h1").text must include(
          messages(
            "startYourReturn.firstReturn.title",
            "5 July 2022",
            "5 July 2022",
            "5 October 2022"
          ))

      }
    }


    "contain save & continue button" in {
      createView(isFirstReturn = true).getElementsByClass("govuk-button").text() must
        include("Save and continue")
    }

    "error" when {
      "answer not selected" in {
        val view = createView(form.bind(Map("value" -> "")), true)

        view.getElementsByClass("govuk-error-summary__body").text() must
          include("Select yes if you want to start your return")

        view.getElementById("value-error").text() must include("Select yes if you want to start your return")
      }
    }

  }

}
