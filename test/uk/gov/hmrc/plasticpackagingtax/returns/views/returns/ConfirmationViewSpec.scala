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

package uk.gov.hmrc.plasticpackagingtax.returns.views.returns

import org.scalatest.matchers.must.Matchers
import play.api.mvc.Flash
import play.twirl.api.Html
import uk.gov.hmrc.plasticpackagingtax.returns.base.unit.UnitViewSpec
import uk.gov.hmrc.plasticpackagingtax.returns.models.response.FlashKeys
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.returns.confirmation_page
import uk.gov.hmrc.plasticpackagingtax.returns.views.tags.ViewTest
import uk.gov.hmrc.plasticpackagingtax.returns.views.utils.ViewUtils

@ViewTest
class ConfirmationViewSpec extends UnitViewSpec with Matchers {

  private val page: confirmation_page = instanceOf[confirmation_page]

  private def createView(flash: Flash = new Flash(Map.empty)): Html =
    page()(journeyRequest, messages, flash)

  override def exerciseGeneratedRenderingMethods(): Unit = {
    page.f()(journeyRequest, messages, new Flash(Map.empty))
    page.render(journeyRequest, messages, new Flash(Map.empty))
  }

  "Confirmation Page view" should {

    val view: Html = createView()

    "contain timeout dialog function" in {

      containTimeoutDialogFunction(view) mustBe true

    }

    "display sign out link" in {

      displaySignOutLink(view)

    }

    "display title" in {

      view.select("title").text() must include(messages("returns.confirmationPage.title"))
    }

    "display panel" when {

      "no 'referenceId' has been provided" in {
        view.getElementsByClass("govuk-panel__title").first() must containMessage(
          "returns.confirmationPage.title"
        )
        view.getElementsByClass("govuk-panel__body").first() must containMessage(
          "returns.confirmationPage.panel.body.default"
        )
      }

      "a 'referenceId' has been provided" in {
        val viewWithReferenceId = createView(flash = Flash(Map(FlashKeys.referenceId -> "PPT123")))
        viewWithReferenceId.getElementsByClass("govuk-panel__title").get(0) must containMessage(
          "returns.confirmationPage.title"
        )
        viewWithReferenceId.getElementsByClass("govuk-panel__body").get(0) must containMessage(
          "returns.confirmationPage.panel.body",
          "PPT123"
        )
      }
    }

    "display body" in {

      view.getElementsByClass("govuk-body").first() must containMessage(
        "returns.confirmationPage.body.2"
      )
      view.getElementsByClass("govuk-body").get(1) must containMessage(
        "returns.confirmationPage.body.3"
      )
    }

    "display 'Pay Tax Owed'" ignore {

      view.getElementsByClass("govuk-heading-m").first() must containMessage(
        "returns.confirmationPage.payTaxOwed.title"
      )
      view.getElementsByClass("govuk-body").get(2) must containMessage(
        "returns.confirmationPage.payTaxOwed.text",
        journeyRequest.taxReturn.taxLiability.taxDue,
        ViewUtils.displayLocalDate(journeyRequest.taxReturn.getTaxReturnObligation().dueDate)
      )
    }

    "have the exit survey" in {
      view.getElementsByClass("govuk-body").get(3) must containMessage(
        "returns.confirmationPage.exitSurvey.text",
        messages("returns.confirmationPage.exitSurvey.text.link")
      )
    }
  }
}
