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

package views.amends

import base.ViewSpecBase
import forms.amends.CancelAmendFormProvider
import models.returns.TaxReturnObligation
import play.twirl.api.Html
import support.{ViewAssertions, ViewMatchers}
import views.html.amends.CancelAmendView

import java.time.LocalDate

class CancelAmendViewSpec extends ViewSpecBase with ViewAssertions with ViewMatchers{

  val form = new CancelAmendFormProvider()()
  val page = inject[CancelAmendView]

  val aTaxObligation: TaxReturnObligation = TaxReturnObligation(
    LocalDate.of(2022,7,5),
    LocalDate.of(2022,10,5),
    LocalDate.of(2023,1,5),
    "PK1")

  private def createView(taxObligation: TaxReturnObligation): Html =
    page(form, taxObligation)(request, messages)

  "CancelAmendView Yes and No page" should {
    val view = createView(aTaxObligation)

    "have a title" in {

      view.select("title").text mustBe
        "Are you sure you want to cancel amending your return for July to October 2022? - Submit return - Plastic Packaging Tax - GOV.UK"

    }
    "have a heading" in{

      view.select("h1").text mustBe
        "Are you sure you want to cancel amending your return for July to October 2022?"

    }


    "contain continue button" in {

      view.getElementsByClass("govuk-button").text() must include("Continue")

    }

  }
}
