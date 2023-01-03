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
import forms.returns.ImportedPlasticPackagingFormProvider
import models.Mode.NormalMode
import models.returns.TaxReturnObligation
import play.twirl.api.Html
import support.{ViewAssertions, ViewMatchers}
import views.html.returns.ImportedPlasticPackagingView

import java.time.LocalDate

class ImportedPlasticPackagingViewSpec extends ViewSpecBase with ViewAssertions with ViewMatchers {

  val form = new ImportedPlasticPackagingFormProvider()()
  val page = inject[ImportedPlasticPackagingView]

  val aTaxObligation: TaxReturnObligation = TaxReturnObligation(LocalDate.now(), LocalDate.now().plusWeeks(12), LocalDate.now().plusWeeks(16), "PK1")

  private def createView: Html =
    page(form, NormalMode, aTaxObligation)(request, messages)

  "ImportedPlasticPackagingView" should {
    val view = createView

    "have a title" in {

      view.select("title").text mustBe
        "Have you imported finished plastic packaging components in this period? - Submit return - Plastic Packaging Tax - GOV.UK"

    }
    "have a heading" in{

      view.select("h1").text mustBe
        "Have you imported finished plastic packaging components in this period?"

    }
    "have a caption" in {

      view.getElementById("section-header").text() mustBe "Total plastic packaging"

    }

    "contain paragraph content" in{

      val text = "Tax is chargeable on finished plastic packaging components you import. Plastic packaging is finished when the last " +
        "substantial modification in the manufacturing process has been made. If you’re still not sure, check when packaging is classed as " +
        "finished and what we mean by components and substantial modifications."

      view.getElementsByClass("govuk-body").text() must include(text)

    }
    "contain save & continue button" in {

      view.getElementsByClass("govuk-button").text() mustBe "Save and continue"

    }

  }

}
