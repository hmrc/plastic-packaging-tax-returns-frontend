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
import forms.returns.credits.ConvertedCreditsFormProvider
import models.Mode.NormalMode
import models.returns.TaxReturnObligation
import org.jsoup.Jsoup
import play.api.data.Form
import play.twirl.api.Html
import support.{ViewAssertions, ViewMatchers}
import views.html.returns.credits.WhatDoYouWantToDoView

import java.time.LocalDate


class WhatDoYouWantToDoViewSpec extends ViewSpecBase with ViewAssertions with ViewMatchers {

  val page: WhatDoYouWantToDoView = inject[WhatDoYouWantToDoView]
  val form = new ConvertedCreditsFormProvider()()

  def someDate = LocalDate.now
  val obligation = TaxReturnObligation(someDate, someDate, someDate, "some-key")

  private def createView(form: Form[Boolean] = form): Html =
    page(form, obligation, NormalMode)(request, messages)

  "view" must {
    val doc = Jsoup.parse(createView().toString())

    "have heading" in {
      doc.getElementsByTag("H1").text() mustBe "What do you want to do?"
    }
    "have p1" in {
      doc.getElementsByTag("p").text() must include("If you want to claim tax back as credit, you must do this when you submit your return. If you do not claim it now, you must wait until your next return.")
    }

    "have p2" in {
      doc.getElementsByTag("p").text() must include("Find out about claiming tax back as credit for packaging you’ve paid tax on.")
    }

    "have a submit button" in {
      doc.getElementsByClass("govuk-button").text mustBe messages("site.continue")
    }
  }

}
