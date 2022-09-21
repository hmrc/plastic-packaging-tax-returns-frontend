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
import models.UserAnswers
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import pages.returns.ManufacturedPlasticPackagingPage
import play.twirl.api.Html
import support.{ViewAssertions, ViewMatchers}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.checkAnswers.returns.ManufacturedPlasticPackagingSummary.ConfirmManufacturedPlasticPackaging
import viewmodels.govuk.summarylist._
import views.html.returns.ConfirmPlasticPackagingTotalView

class ConfirmPlasticPackagingTotalViewSpec extends ViewSpecBase  with ViewAssertions with ViewMatchers {

  val page: ConfirmPlasticPackagingTotalView = inject[ConfirmPlasticPackagingTotalView]

  private def createView(list: SummaryList): Html =
    page(list)(request, messages)

  "view" should {

    val view = createView(createSummaryList)

    "have title" in {

      view.select("title").text() must include(messages("confirmPlasticPackagingTotal.title"))

      view.select("title").text() mustBe
        ("Confirm your plastic packaging total - Submit return - Plastic Packaging Tax - GOV.UK")
    }

    "contain section header" in {
      view.getElementById("section-header").text() mustBe "Total plastic packaging"
      view.getElementById("section-header").text() mustBe messages("confirmPlasticPackagingTotal.sectionHeader")
    }

    "contain header" in {
      view.getElementsByClass("govuk-heading-l").text() mustBe "Confirm your plastic packaging total"
      view.getElementsByClass("govuk-heading-l").text() mustBe messages("Confirm your plastic packaging total")
    }

    "contain a summary" in {
      view.getElementsByClass("govuk-summary-list__row").size() mustBe 1
    }

    "display the continue button" in {
      view.getElementsByClass("govuk-button").text() mustBe messages("site.button.continue")
    }
  }

  def createSummaryList: SummaryList = {
    val answer = UserAnswers("123").set(ManufacturedPlasticPackagingPage, true).success.value
    SummaryListViewModel(
      Seq(ConfirmManufacturedPlasticPackaging).flatMap(_.row(answer))
    )
  }
}
