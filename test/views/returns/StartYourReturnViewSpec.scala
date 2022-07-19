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
import forms.returns.StartYourReturnFormProvider
import models.NormalMode
import models.returns.TaxReturnObligation
import play.twirl.api.Html
import support.{ViewAssertions, ViewMatchers}
import views.html.returns.StartYourReturnView

import java.time.LocalDate

class StartYourReturnViewSpec extends ViewSpecBase with ViewAssertions with ViewMatchers {

  val form = new StartYourReturnFormProvider()()
  val page = inject[StartYourReturnView]

  val aTaxObligation: TaxReturnObligation = TaxReturnObligation(LocalDate.now(), LocalDate.now().plusWeeks(12), LocalDate.now().plusWeeks(16), "PK1")

  private def createView: Html =
    page(form, NormalMode, aTaxObligation, true)(request, messages)

  "StartYourReturnView" should {
    val view = createView

    "have a title" in {

      view.select("title").text mustBe
        "Do you want to start your return for the July to October 2022 accounting period? - Submit return - Plastic Packaging Tax - GOV.UK"

    }
    "have a heading" in{

      view.select("h1").text mustBe
        "Do you want to start your return for the July to October 2022 accounting period?"

    }

    "contain save & continue button" in {

      view.getElementsByClass("govuk-button").text() mustBe "Save and continue"

    }

  }

}
