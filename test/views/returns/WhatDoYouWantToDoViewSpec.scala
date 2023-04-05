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
import config.FrontendAppConfig
import forms.returns.credits.DoYouWantToClaimFormProvider
import models.Mode.NormalMode
import models.returns.TaxReturnObligation
import play.twirl.api.Html
import support.{ViewAssertions, ViewMatchers}
import views.ViewUtils
import views.html.returns.credits.WhatDoYouWantToDoView

import java.time.LocalDate

class WhatDoYouWantToDoViewSpec extends ViewSpecBase with ViewAssertions with ViewMatchers {

  val form = new DoYouWantToClaimFormProvider()()
  val page = inject[WhatDoYouWantToDoView]
  val appConfig = inject[FrontendAppConfig]

  val aTaxObligation: TaxReturnObligation = TaxReturnObligation(
    LocalDate.of(2022,7,5),
    LocalDate.of(2022,10,5),
    LocalDate.of(2023,1,5),
    "PK1")

  private def createView: Html =
    page(form, aTaxObligation, NormalMode)(request, messages)

  "WhatDoYouWantToDoView" should {
    val view = createView
    "have a title" in {

      view.select("title").text must include(messages("what-do-you-want-to-do.title"))
    }

    "have a heading" in {

      view.select("h1").text mustBe messages("what-do-you-want-to-do.title")
    }

    "have a first paragraph text" in {

      view.select("p").text must include(messages("what-do-you-want-to-do.paragraph.1"))
    }

    "have a the radio options" in {

      view.select(".govuk-radios__item").get(0).text mustBe messages("what-do-you-want-to-do.credit-and-return", ViewUtils.displayReturnQuarter(aTaxObligation))
      view.select(".govuk-radios__item").get(1).text mustBe messages("what-do-you-want-to-do.just-return", ViewUtils.displayReturnQuarter(aTaxObligation))
    }

    "have a second paragraph text" in {

      view.select("p").text must include(messages("what-do-you-want-to-do.paragraph.2", messages("what-do-you-want-to-do.paragraph.2.link")))
    }

    "have a guidance link" in {

      view.select("#guidance-link").attr("href") mustBe appConfig.claimingCreditGuidanceUrl
    }

    "contain save & continue button" in {

      view.getElementsByClass("govuk-button").text() mustBe "Save and continue"

    }
  }
}
