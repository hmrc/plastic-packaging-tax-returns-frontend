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

package views.returns.credits

import base.ViewSpecBase
import forms.returns.credits.ClaimForWhichYearFormProvider.CreditRangeOption
import forms.returns.credits.DoYouWantToClaimFormProvider
import models.Mode.NormalMode
import play.api.mvc.Call
import play.twirl.api.Html
import support.{ViewAssertions, ViewMatchers}
import views.html.returns.credits.ClaimForWhichYearView

import java.time.LocalDate

class ClaimForWhichYearViewSpec extends ViewSpecBase with ViewAssertions with ViewMatchers{

  val page = inject[ClaimForWhichYearView]
  val form = new DoYouWantToClaimFormProvider()()

  private def createView: Html = page(form, Seq(CreditRangeOption(LocalDate.of(2022, 4, 1), LocalDate.of(2023, 3, 31))), NormalMode)(request, messages)

  "ClaimForWhichYearView" should {
    val view = createView

    "have a title" in {
      view.select("title").text mustBe
        "Which year do you want to claim tax back as credit for? - Submit return - Plastic Packaging Tax - GOV.UK"
    }

    "have heading" in {
      view.select("h1").text mustBe "Which year do you want to claim tax back as credit for?"
    }

    "have paragraph content" in {
      val paragraph = view.getElementsByClass("govuk-body").text()

      paragraph must include("Plastic Packaging Tax rates have changed. We need to make sure you claim tax back at the correct rate.")
      paragraph must include("If you need to claim tax back as credit for more than one year you will be given the option to do this later.")
    }

    "have radio options" in {
      view.select(".govuk-radios__item").get(0).text mustBe "1 April 2022 to 31 March 2023"
    }

    "have a save and continue button" in {
      view.getElementsByClass("govuk-button").text() mustBe "Save and continue"
    }

  }

}
