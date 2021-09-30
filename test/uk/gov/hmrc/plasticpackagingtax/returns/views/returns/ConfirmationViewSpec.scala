/*
 * Copyright 2021 HM Revenue & Customs
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
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.home.{routes => homeRoutes}
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.returns.{routes => returnRoutes}
import uk.gov.hmrc.plasticpackagingtax.returns.models.response.FlashKeys
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.returns.confirmation_page
import uk.gov.hmrc.plasticpackagingtax.returns.views.tags.ViewTest

@ViewTest
class ConfirmationViewSpec extends UnitViewSpec with Matchers {

  private val page: confirmation_page = instanceOf[confirmation_page]

  private def createView(flash: Flash = new Flash(Map.empty)): Html =
    page()(request, messages, flash)

  override def exerciseGeneratedRenderingMethods(): Unit = {
    page.f()(request, messages, new Flash(Map.empty))
    page.render(request, messages, new Flash(Map.empty))
  }

  "Confirmation Page view" should {

    "have proper messages for labels" in {

      messages must haveTranslationFor("returns.confirmationPage.title")
      messages must haveTranslationFor("returns.confirmationPage.panel.body")
      messages must haveTranslationFor("returns.confirmationPage.panel.body.default")
      messages must haveTranslationFor("returns.confirmationPage.body")
      messages must haveTranslationFor("returns.confirmationPage.body.print")
      messages must haveTranslationFor("returns.confirmationPage.body.downloadPdf")
      messages must haveTranslationFor("returns.confirmationPage.whatHappensNext.title")
      messages must haveTranslationFor("returns.confirmationPage.whatHappensNext.liable.title")
      messages must haveTranslationFor("returns.confirmationPage.inTheMeantime.title")
      messages must haveTranslationFor("returns.confirmationPage.inTheMeantime.payLink")
      messages must haveTranslationFor("returns.confirmationPage.inTheMeantime.homeLink")
      messages must haveTranslationFor("returns.confirmationPage.exitSurvey.text.link")
      messages must haveTranslationFor("returns.confirmationPage.exitSurvey.text")
    }

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
        "returns.confirmationPage.body"
      )
    }

    "display print page link" in {
      view.getElementById("printPage") must (containMessage("returns.confirmationPage.body.print")
        and haveHref(returnRoutes.ConfirmationController.displayPage()))
    }

    "display download pdf link" in {
      view.getElementById("downloadPdf") must (containMessage(
        "returns.confirmationPage.body.downloadPdf"
      ) and haveHref(returnRoutes.ConfirmationController.displayPage()))
    }

    "display 'What happens next'" in {

      view.getElementsByClass("govuk-heading-m").first() must containMessage(
        "returns.confirmationPage.whatHappensNext.title"
      )
      view.getElementsByClass("govuk-body").get(1) must containMessage(
        "returns.confirmationPage.whatHappensNext.liable.title"
      )
      view.getElementsByClass("govuk-heading-m").get(1) must containMessage(
        "returns.confirmationPage.inTheMeantime.title"
      )
      val bulletPoint1 = view.getElementsByClass("dashed-list-item").get(0)
      bulletPoint1 must containMessage("returns.confirmationPage.inTheMeantime.payLink")
      bulletPoint1.getElementsByClass("govuk-link").first() must haveHref(
        returnRoutes.ConfirmationController.displayPage()
      )
      val bulletPoint2 = view.getElementsByClass("dashed-list-item").get(1)
      bulletPoint2 must containMessage("returns.confirmationPage.inTheMeantime.homeLink")
      bulletPoint2.getElementsByClass("govuk-link").first() must haveHref(
        homeRoutes.HomeController.displayPage()
      )
      view.getElementsByClass("govuk-body").get(2) must containMessage(
        "returns.confirmationPage.exitSurvey.text",
        messages("returns.confirmationPage.exitSurvey.text.link")
      )
    }
  }
}
