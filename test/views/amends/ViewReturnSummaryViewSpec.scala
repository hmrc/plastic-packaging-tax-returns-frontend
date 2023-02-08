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
import models.returns.{IdDetails, ReturnDisplayApi, ReturnDisplayChargeDetails, ReturnDisplayDetails}
import play.api.mvc.Call
import play.api.test.Helpers.GET
import play.twirl.api.Html
import support.{ViewAssertions, ViewMatchers}
import viewmodels.checkAnswers.ViewReturnSummaryViewModel
import views.html.amends.ViewReturnSummaryView

class ViewReturnSummaryViewSpec extends ViewSpecBase with ViewAssertions with ViewMatchers {

  val page = inject[ViewReturnSummaryView]

  private val returnDisplayChargeDetails = ReturnDisplayChargeDetails(
    "21C2", Some("charge-ref-no"), "2022-04-01", "2022-06-30", "2022-07-03", "New"
  )

  private val debitForPeriod = 100

  private val returnDetails = ReturnDisplayDetails(
    10, 2, 3, 4, 5, 6, debitForPeriod, 8, 9, 10
  )

  private val displayReturn = ReturnDisplayApi(
    "2019-08-28T09:30:47Z",
    IdDetails("XMPPT0000000001", "00-11-submission-id"),
    Some(returnDisplayChargeDetails),
    returnDetails
  )

  val summaryRow = ViewReturnSummaryViewModel(displayReturn)(messages)

  def createView: Html = {
    page("anyPeriod", summaryRow, Some(Call(GET, "/foo")), "£300")(request, messages)
  }

  "view" should {
    val view = createView
    "contains a credits section" in {
      view.select("h3").text() must include("Credits")
      view.select("h3").text() must include(messages("viewReturnSummary.credit.heading"))
    }

    "display Total Credits claimed" in {
      view.getElementsByClass("govuk-summary-list__row").select("dt").text() must include("Credit total")
      view.getElementsByClass("govuk-summary-list__row").get(2).select("dd").text() mustBe "£100.00"
    }


    "contain a how much the plastic is charged for tonne" in {
      view.select("p").text() must include("Tax is £300 per tonne on chargeable plastic packaging.")
    }
  }

}
