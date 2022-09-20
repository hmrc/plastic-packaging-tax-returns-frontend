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

package views.returns.credits

import base.ViewSpecBase
import models.Mode.NormalMode
import play.api.mvc.Call
import play.twirl.api.Html
import support.{ViewAssertions, ViewMatchers}
import viewmodels.{PrintBigDecimal, PrintLong}
import views.html.returns.credits.ConfirmPackagingCreditView

class ConfirmPackagingCreditViewSpec extends ViewSpecBase  with ViewAssertions with ViewMatchers{

  val page: ConfirmPackagingCreditView = inject[ConfirmPackagingCreditView]
  val weight = 200L
  val requestedCredit = BigDecimal(500)
  val continueCall = Call("TEST", "/end-point")

  private def createView: Html =
    page(requestedCredit, weight, continueCall)(request, messages)

  "View" should {

    val view = createView

    "have a title" in {
      view.select("title").text() must include("Confirm credit amount - Submit return - Plastic Packaging Tax - GOV.UK")
      view.select("title").text() must include(messages("confirmPackagingCredit.title"))
    }

    "have a header" in {
      view.select("h1").text mustBe s"Confirm ${requestedCredit.asPounds} of credit"
      view.select("h1").text mustBe messages("confirmPackagingCredit.heading", requestedCredit.asPounds)
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text() mustBe "Credits"
      view.getElementsByClass("govuk-caption-l").text() mustBe messages("confirmPackagingCredit.subHeading")
    }

    "have a hint" in {
      view.getElementById("paragraph-body-1").text() mustBe s"You told us that you paid tax on ${weight.asKg} of plastic packaging from a previous return, and it has since been exported or converted."
      view.getElementById("paragraph-body-1").text() mustBe messages("confirmPackagingCredit.hint.p1", weight.asKg)

      view.getElementById("paragraph-body-2").text() mustBe s"Plastic Packaging Tax is calculated at £200 per tonne."
      view.getElementById("paragraph-body-2").text() mustBe messages("confirmPackagingCredit.hint.p2")

      view.getElementById("paragraph-body-3").text() mustBe s"This means the amount of tax you’ll get back as credit will be ${requestedCredit.asPounds}."
      view.getElementById("paragraph-body-3").text() mustBe messages("confirmPackagingCredit.hint.p3", requestedCredit.asPounds)

      view.getElementById("paragraph-body-4").text() mustBe s"Your credit will be applied against your total balance in your Plastic Packaging Tax account."
      view.getElementById("paragraph-body-4").text() mustBe messages("confirmPackagingCredit.hint.p4")
    }

    "have a confirm button" in {
      view.getElementById("link-button") must haveHref(continueCall.url)
      view.getElementById("link-button").text() mustBe  "Confirm credit amount"
      view.getElementById("link-button").text() mustBe messages("confirmPackagingCredit.confirm.credit.button")
    }

    "allow to change the amount of credit" in {
      view.getElementById("change-credit-amount") must haveHref(controllers.returns.credits.routes.ExportedCreditsController.onPageLoad(NormalMode).url)
      view.getElementById("change-credit-amount").text() mustBe "Change the amount of credit"
      view.getElementById("change-credit-amount").text() mustBe messages("confirmPackagingCredit.change.credit.paragraph")
    }

  }
}
