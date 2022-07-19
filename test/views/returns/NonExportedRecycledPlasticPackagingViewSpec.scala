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

import base.ViewSpecBase
import forms.returns.{NonExportedHumanMedicinesPlasticPackagingFormProvider, NonExportedRecycledPlasticPackagingFormProvider}
import models.NormalMode
import play.twirl.api.Html
import support.{ViewAssertions, ViewMatchers}
import views.html.returns.{NonExportedHumanMedicinesPlasticPackagingView, NonExportedRecycledPlasticPackagingView}

class NonExportedRecycledPlasticPackagingViewSpec extends ViewSpecBase with ViewAssertions with ViewMatchers {

  val form = new NonExportedRecycledPlasticPackagingFormProvider()()
  val page = inject[NonExportedRecycledPlasticPackagingView]
  val plastic = 1234L

  private def createView: Html =
    page(form, NormalMode, plastic)(request, messages)

  "NonExportedRecycledPlasticPackagingView" should {
    val view = createView

    "have a title" in {

      view.select("title").text mustBe
        "You did not export 1,234kg of your total finished plastic packaging components. Did any of this contain 30% or more recycled plastic? - Submit return - Plastic Packaging Tax - GOV.UK"

    }
    "have a heading" in{

      view.select("h1").text mustBe
        "You did not export 1,234kg of your total finished plastic packaging components. Did any of this contain 30% or more recycled plastic?"

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

  }

}
