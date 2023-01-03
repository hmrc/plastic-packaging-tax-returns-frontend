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
import forms.returns.NonExportedRecycledPlasticPackagingFormProvider
import models.Mode.NormalMode
import play.api.data.Form
import play.twirl.api.Html
import support.{ViewAssertions, ViewMatchers}
import viewmodels.PrintLong
import views.html.returns.NonExportedRecycledPlasticPackagingView

class NonExportedRecycledPlasticPackagingViewSpec extends ViewSpecBase with ViewAssertions with ViewMatchers {

  val form = new NonExportedRecycledPlasticPackagingFormProvider()()
  val page = inject[NonExportedRecycledPlasticPackagingView]
  val plastic = 1234L
  val plasticAsKg = plastic.asKg

  private def createView(form: Form[Boolean] = form, directlyExportAnswer: Boolean = true): Html =
    page(form, NormalMode, plastic, directlyExportAnswer)(request, messages)

  "NonExportedRecycledPlasticPackagingView" should {
    val view = createView()

    "have a title" when {
      "directly exported component answer is yes" in {
        view.select("title").text mustBe
          s"A total of $plasticAsKg was not exported. Did any of this contain 30% or more recycled plastic? - Submit return - Plastic Packaging Tax - GOV.UK"
        view.select("title").text must include(messages("NonExportRecycledPlasticPackaging.heading", plasticAsKg))

      }

      "directly exported component answer is No" in {
        val newView = createView(directlyExportAnswer = false)

        newView.select("title").text mustBe
          s"Did any of your $plasticAsKg of your total finished plastic packaging components contain 30% or more recycled plastic? - Submit return - Plastic Packaging Tax - GOV.UK"
        newView.select("title").text() must include(messages("NonExportRecycledPlasticPackaging.directly.export.no.heading", plasticAsKg))
      }
    }
    "have a heading" when {

      "directly exported component answer is yes" in {
        view.select("h1").text mustBe
          s"A total of $plasticAsKg was not exported. Did any of this contain 30% or more recycled plastic?"
        view.select("h1").text() mustBe messages("NonExportRecycledPlasticPackaging.heading", plasticAsKg)
      }

      "directly exported component answer is No" in {
        val newView = createView(directlyExportAnswer = false)

        newView.select("h1").text mustBe
          s"Did any of your $plasticAsKg of your total finished plastic packaging components contain 30% or more recycled plastic?"
        newView.select("h1").text() must include(messages("NonExportRecycledPlasticPackaging.directly.export.no.heading", plasticAsKg))
      }
    }

    "have a caption" in {
      view.getElementById("section-header").text() mustBe messages("nonExportedHumanMedicinesPlasticPackaging.caption")
    }

    "contain paragraph content" in{
      val text = "You will not be charged tax on these but you must still tell us about them. Find out what we mean by recycled plastic packaging."
      view.getElementsByClass("govuk-body").text() must include(text)
    }
    "contain save & continue button" in {
      view.getElementsByClass("govuk-button").text() mustBe  messages("site.continue")
    }

    "contain an error" when {
      "no answer is selected" in {
        val view = createView(form.bind(Map("value" -> "")))

        assertErrorBoxMsg(view)
        assertErrorMsg(view)
      }
    }
  }

  private def assertErrorBoxMsg(view: Html): Unit = {
    val actualErrorBoxMsg = view.getElementsByClass("govuk-error-summary__body").text()
    actualErrorBoxMsg mustBe "Select yes if any of your finished plastic packaging components contained 30% or more recycled plastic"
    actualErrorBoxMsg mustBe messages("NonExportRecycledPlasticPackaging.error.required")
  }

  private def assertErrorMsg(view: Html): Unit = {
    val actualErrorMessage = view.getElementsByClass("govuk-error-message").text()

    actualErrorMessage must include("Select yes if any of your finished plastic packaging components contained 30% or more recycled plastic")
    actualErrorMessage must include(messages("NonExportRecycledPlasticPackaging.error.required"))
  }
}
