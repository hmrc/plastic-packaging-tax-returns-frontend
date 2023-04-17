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
import models.Mode.NormalMode
import play.api.mvc.Call
import play.twirl.api.Html
import support.{ViewAssertions, ViewMatchers}
import uk.gov.hmrc.govukfrontend.views.Aliases.{Text, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{ActionItem, Actions, Key, SummaryListRow}
import views.html.returns.credits.ConfirmPackagingCreditView

class ConfirmPackagingCreditViewSpec extends ViewSpecBase  with ViewAssertions with ViewMatchers{

  private val page: ConfirmPackagingCreditView = inject[ConfirmPackagingCreditView]
  private val requestedCredit = BigDecimal(500)
  private val fromDate = "1 April 2022"
  private val toDate = "31 March 2023"
  private val continueCall = Call("TEST", "/end-point")


  private val summaryList = Seq(
    SummaryListRow(key = Key(Text("tax rate")), value = Value(Text("value in pounds"))),
    SummaryListRow(
      key = Key(Text("exported")),
      value = Value(Text("answer")),
      actions = Some(Actions(items = Seq(ActionItem("/foo", Text("change"))))))
  )
  private def createView(canClaim: Boolean = true): Html =
    page(requestedCredit, canClaim, fromDate, toDate, summaryList, continueCall, NormalMode)(request, messages)

  "View" should {

    val view = createView()

    "have a title" in {
      view.select("title").text() must include("Confirm credit for 1 April 2022 to 31 March 2023 - Submit return - Plastic Packaging Tax - GOV.UK")
      view.select("title").text() must include(messages("confirmPackagingCredit.title", "1 April 2022", "31 March 2023"))
    }

    "have a header" in {
      view.select("h1").text mustBe s"Confirm credit for 1 April 2022 to 31 March 2023"
      view.select("h1").text mustBe messages("confirmPackagingCredit.title", "1 April 2022", "31 March 2023")
    }

    "display summary list row" in {
      view.getElementsByClass("govuk-summary-list__row").size() mustEqual 2
    }

    "display conditional paragraph" when{
      "only one year available to claim" in {
        view.getElementsByClass("govuk-body").text() must include("Your £500.00 credit will be applied against your total balance in your Plastic Packaging Tax account.")
        view.getElementsByClass("govuk-body").text() must include(messages("confirmPackagingCredit.requestedCredits", "£500.00"))

        withClue("should not show too much credit paragraph") {
          view.select("h2").text() must not include("You are claiming too much credit")
        }
      }

      "one year to claim back and too much credits" in {
        val tooMuchCreditView = createView(false)
        tooMuchCreditView.select("h2").text() must include("You are claiming too much credit")
        tooMuchCreditView.getElementsByClass("govuk-body").text must include("This credit amount is more than the total tax you paid between 1 April 2022 and 31 March 2023.")
        tooMuchCreditView.select("h3").text must include("What you need to do")
        tooMuchCreditView.getElementsByClass("govuk-body").text must
          include("To continue, you need to change one or more of your answers. Check how much plastic packaging you paid tax on between 1 April 2022 and 31 March 2023. You might need to check more than one previously submitted return .")

        tooMuchCreditView.getElementById("previous-submitted-return").select("a").get(0) must
          haveHref(controllers.amends.routes.SubmittedReturnsController.onPageLoad().url)

        tooMuchCreditView.getElementsByClass("govuk-body").text must
          include("We have saved your answers. You can check your records and come back later.")

        tooMuchCreditView.getElementsByClass("govuk-body").text must
          include("You must have sufficient evidence to claim tax back as credit.")

        withClue("should not show available credit applied mnessage") {
          tooMuchCreditView.getElementsByClass("govuk-body").text() must not include("Your £500.00 credit will be applied against your total balance in your Plastic Packaging Tax account.")
        }
      }
    }

    "show cancel credit claim link when too much credit" in {
      createView(false).getElementById("cancel-credit-claim").text() mustBe "Cancel credit claim"
    }

    "should not show button when too much credit" in {
      createView(false).getElementById("link-button") mustBe null
    }

    "have a confirm and continue button" in {
      view.getElementById("link-button") must haveHref(continueCall.url)
      view.getElementById("link-button").text() mustBe  "Confirm and continue"
      view.getElementById("link-button").text() mustBe messages("site.continue.confirm")
    }

  }
}
