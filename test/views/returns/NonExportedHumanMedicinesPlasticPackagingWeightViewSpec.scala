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
import forms.returns.NonExportedHumanMedicinesPlasticPackagingWeightFormProvider
import models.Mode.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.twirl.api.Html
import support.{ViewAssertions, ViewMatchers}
import viewmodels.PrintLong
import views.html.returns.NonExportedHumanMedicinesPlasticPackagingWeightView

class NonExportedHumanMedicinesPlasticPackagingWeightViewSpec
    extends ViewSpecBase
    with ViewAssertions
    with ViewMatchers {

  val form                = new NonExportedHumanMedicinesPlasticPackagingWeightFormProvider()()
  val page                = inject[NonExportedHumanMedicinesPlasticPackagingWeightView]
  val plastic             = 1234L
  private val plasticAsKg = plastic.asKg

  private def createView(
    directlyExportedYesNoAnswer: Boolean = true,
    anotherBusinessExportedYesNoAnswer: Boolean = true
  ): Html =
    page(plastic, form, NormalMode, directlyExportedYesNoAnswer, anotherBusinessExportedYesNoAnswer)(request, messages)

  "NonExportedHumanMedicinesPlasticPackagingWeightView" should {
    val view = createView()

    "have a title" when {
      "directly exported component answer is yes and another business exported answer is yes" in {
        view.select(
          "title"
        ).text mustBe s"Out of the $plasticAsKg that was not exported, how much was used for the immediate packaging of licenced human medicines? - Submit return - Plastic Packaging Tax - GOV.UK"
        view.select("title").text must include(
          messages("nonExportedHumanMedicinesPlasticPackagingWeight.heading", plasticAsKg)
        )
      }

      "directly exported component answer is yes and another business exported answer is no" in {
        val view = createView(true, false)

        view.select(
          "title"
        ).text mustBe s"Out of the $plasticAsKg that was not exported, how much was used for the immediate packaging of licenced human medicines? - Submit return - Plastic Packaging Tax - GOV.UK"
        view.select("title").text must include(
          messages("nonExportedHumanMedicinesPlasticPackagingWeight.heading", plasticAsKg)
        )
      }

      "directly exported component answer is no and another business exported answer is yes" in {
        val view = createView(false, true)

        view.select(
          "title"
        ).text mustBe s"Out of the $plasticAsKg that was not exported, how much was used for the immediate packaging of licenced human medicines? - Submit return - Plastic Packaging Tax - GOV.UK"
        view.select("title").text must include(
          messages("nonExportedHumanMedicinesPlasticPackagingWeight.heading", plasticAsKg)
        )
      }

      "directly exported component answer is No and another business exported answer is no" in {
        val view = createView(false, false)

        view.select(
          "title"
        ).text mustBe s"How much of your $plasticAsKg of finished plastic packaging components was used for the immediate packaging of licenced human medicines? - Submit return - Plastic Packaging Tax - GOV.UK"
        view.select("title").text must include(
          messages("nonExportedHumanMedicinesPlasticPackagingWeight.direct.exported.no.answer.heading", plastic.asKg)
        )
      }
    }

    "have a heading" when {
      "directly exported component answer is yes and another business exported answer is yes" in {
        view.select(
          "h1"
        ).text mustBe s"Out of the $plasticAsKg that was not exported, how much was used for the immediate packaging of licenced human medicines?"
        view.select("h1").text mustBe messages("nonExportedHumanMedicinesPlasticPackagingWeight.heading", plasticAsKg)
      }

      "directly exported component answer is yes and another business exported answer is no" in {
        val view = createView(true, false)

        view.select(
          "h1"
        ).text mustBe s"Out of the $plasticAsKg that was not exported, how much was used for the immediate packaging of licenced human medicines?"
        view.select("h1").text mustBe messages("nonExportedHumanMedicinesPlasticPackagingWeight.heading", plasticAsKg)
      }

      "directly exported component answer is no and another business exported answer is yes" in {
        val view = createView(false, true)

        view.select(
          "h1"
        ).text mustBe s"Out of the $plasticAsKg that was not exported, how much was used for the immediate packaging of licenced human medicines?"
        view.select("h1").text mustBe messages("nonExportedHumanMedicinesPlasticPackagingWeight.heading", plasticAsKg)
      }

      "directly exported component answer is no and another business exported answer is no" in {
        val view = createView(false, false)

        view.select(
          "h1"
        ).text mustBe s"How much of your $plasticAsKg of finished plastic packaging components was used for the immediate packaging of licenced human medicines?"
        view.select("h1").text must include(
          messages("nonExportedHumanMedicinesPlasticPackagingWeight.direct.exported.no.answer.heading", plasticAsKg)
        )
      }
    }

    "have a caption" in {
      view.getElementById("section-header").text() mustBe messages(
        "nonExportedHumanMedicinesPlasticPackagingWeight.caption"
      )
    }

    "have a hint" in {
      val view: Html    = createView()
      val doc: Document = Jsoup.parse(view.toString())

      doc.getElementById("value-hint").text must include(messages("1 tonne is 1,000kg."))
    }

    "contain save & continue button" in {
      view.getElementsByClass("govuk-button").text() must include(messages("site.continue"))
    }
  }
}
