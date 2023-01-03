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
import forms.returns.NonExportedHumanMedicinesPlasticPackagingFormProvider
import models.Mode.NormalMode
import play.twirl.api.Html
import support.{ViewAssertions, ViewMatchers}
import viewmodels.PrintLong
import views.html.returns.NonExportedHumanMedicinesPlasticPackagingView

class NonExportedHumanMedicinesPlasticPackagingViewSpec extends ViewSpecBase with ViewAssertions with ViewMatchers {

  val form = new NonExportedHumanMedicinesPlasticPackagingFormProvider()()
  val page = inject[NonExportedHumanMedicinesPlasticPackagingView]
  val plastic = 1234L
  val plasticInKg = plastic.asKg

  private def createView(yesNoDirectExportedAnswer: Boolean = true): Html =
    page(plastic, form, NormalMode, yesNoDirectExportedAnswer)(request, messages)

  "NonExportedHumanMedicinesPlasticPackagingView" should {
    val view = createView()

    "have a title" when {
      "directly exported component answer is yes" in {
        view.select("title").text mustBe
          s"A total of $plasticInKg was not exported. Was any of this used for the immediate packaging of licenced human medicines? - Submit return - Plastic Packaging Tax - GOV.UK"

        view.select("title").text must include(messages("nonExportedHumanMedicinesPlasticPackaging.heading", plasticInKg))

      }

      "directly exported component answer is No" in {
        val view = createView(false)

        view.select("title").text mustBe
          s"Were any of your $plasticInKg of finished plastic packaging components used for the immediate packaging of licenced human medicines? - Submit return - Plastic Packaging Tax - GOV.UK"
        view.select("title").text must include(messages("nonExportedHumanMedicinesPlasticPackaging.direct.exported.no.answer.heading", plasticInKg))
      }
    }
    "have a heading" when {
      "directly exported component answer is yes" in {
        view.select("h1").text mustBe
          s"A total of $plasticInKg was not exported. Was any of this used for the immediate packaging of licenced human medicines?"

        view.select("h1").text mustBe messages("nonExportedHumanMedicinesPlasticPackaging.heading", plasticInKg)
      }

      "directly exported component answer is No" in {
        val view = createView(false)

        view.select("h1").text mustBe
          s"Were any of your $plasticInKg of finished plastic packaging components used for the immediate packaging of licenced human medicines?"
        view.select("h1").text must include(messages("nonExportedHumanMedicinesPlasticPackaging.direct.exported.no.answer.heading", plasticInKg))
      }
    }

    "have a caption" in {
      view.getElementById("section-header").text() mustBe messages("nonExportedHumanMedicinesPlasticPackaging.caption")
    }

    "contain paragraph content" in{

      view.getElementById("reveal").text() must include (messages("nonExportedHumanMedicinesPlasticPackaging.reveal"))
      view.getElementById("reveal").text() must include (messages("nonExportedHumanMedicinesPlasticPackaging.reveal.content"))

    }
    "contain save & continue button" in {

      view.getElementsByClass("govuk-button").text() mustBe  messages("site.continue")

    }

  }

}
