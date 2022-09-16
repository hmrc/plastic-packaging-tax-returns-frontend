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
import config.FrontendAppConfig
import controllers.helpers.TaxReturnViewModel
import models.Mode.CheckMode
import models.requests.{DataRequest, IdentifiedRequest}
import models.returns.{Calculations, CreditsAnswer, CreditsClaimedDetails, TaxReturnObligation}
import models.{CreditBalance, UserAnswers}
import org.jsoup.nodes.Element
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.mock
import pages.returns._
import pages.returns.credits.{ConvertedCreditsPage, ExportedCreditsPage, WhatDoYouWantToDoPage}
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Call
import play.twirl.api.Html
import support.{PptTestData, ViewAssertions, ViewMatchers}
import views.html.returns.ReturnsCheckYourAnswersView

import java.time.LocalDate

class ReturnsCheckYourAnswersViewSpec extends ViewSpecBase with ViewAssertions with ViewMatchers {

  lazy val page: ReturnsCheckYourAnswersView = inject[ReturnsCheckYourAnswersView]
  private val appConfig                      = mock[FrontendAppConfig]

  private val aTaxObligation: TaxReturnObligation = TaxReturnObligation(LocalDate.now(), LocalDate.now().plusWeeks(12), LocalDate.now().plusWeeks(16), "PK1")

  private val userAnswer = UserAnswers("123")
    .set(ManufacturedPlasticPackagingPage, false).get
    .set(ImportedPlasticPackagingPage, false).get
    .set(DirectlyExportedComponentsPage, false).get
    .set(NonExportedHumanMedicinesPlasticPackagingPage, false).get
    .set(NonExportedRecycledPlasticPackagingPage, false).get
    .set(ExportedCreditsPage, CreditsAnswer(false, None)).get
    .set(ConvertedCreditsPage, CreditsAnswer(true, Some(0))).get
    .set(WhatDoYouWantToDoPage, true).get

  private val calculations    = Calculations(1, 2L, 3L, 5L, true)
  private val returnViewModel = createViewModel(userAnswer)

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .overrides(bind[FrontendAppConfig].toInstance(appConfig)).build()

  def createViewModel(answers: UserAnswers): TaxReturnViewModel = {
    val identifiedRequest: IdentifiedRequest[_] = IdentifiedRequest(request, PptTestData.newUser(), Some("123"))
    val dataRequest: DataRequest[_]             = DataRequest(identifiedRequest, answers)

    TaxReturnViewModel(dataRequest, aTaxObligation, calculations)
  }

  private def createView(credits: CreditsClaimedDetails = CreditsClaimedDetails(userAnswer, CreditBalance(0, 0, 0L, true)), taxReturn: TaxReturnViewModel = returnViewModel): Html =
    page(taxReturn, credits)(request, messages)

  "View" should {

    when(appConfig.userResearchUrl).thenReturn("/foo")

    "have a Credits section" in {
      when(appConfig.isCreditsForReturnsFeatureEnabled).thenReturn(true)
      val view = createView()

      getText(view, "credit-section-header") mustBe "Credits"
      getText(view, "credit-section-header") mustBe
        messages("submit-return.check-your-answers.credits.heading")
    }
  }
  "Credits section" should {
    "display guidance" when {
      "credits feature is toggled off" in {

        when(appConfig.isCreditsForReturnsFeatureEnabled).thenReturn(false)
        val view = createView()

        val paragraphText = view.getElementsByClass("govuk-body").text()
        paragraphText must include("You cannot claim credits yet. This is because this is your first Plastic Packaging Tax return.")
        paragraphText must include(messages("submit-return.check-your-answers.credits.line1"))
        paragraphText must include("You may be able to claim credit in future if the packaging is either:")
        paragraphText must include(messages("submit-return.check-your-answers.credits.line2"))
        paragraphText must include("Find out more about claiming credits (opens in new tab)")
        paragraphText must include(
          messages("submit-return.check-your-answers.credits.line5",
            messages("submit-return.check-your-answers.credits.line5.link-text"))
        )

        val bulletListText = view.getElementsByClass("govuk-list--bullet").text()
        bulletListText must include("exported")
        bulletListText must include(messages("submit-return.check-your-answers.credits.line3"))
        bulletListText must include("converted into different packaging")
        bulletListText must include(messages("submit-return.check-your-answers.credits.line4"))

        view.getElementById("credits-line-5").select("a").first() must
          haveHref(appConfig.creditsGuidanceUrl)
      }

    }

    "hide guidance" when {
      "credits feature is toggled on" in {
        when(appConfig.isCreditsForReturnsFeatureEnabled).thenReturn(true)
        createView().getElementById("credits-line-1") mustBe null
      }
    }

    "have no credits claimed" when {
      "both exported and converted answer is no" in {
        when(appConfig.isCreditsForReturnsFeatureEnabled).thenReturn(true)
        val ans  = userAnswer.set(WhatDoYouWantToDoPage, false).get
        val view = createView(credits = CreditsClaimedDetails(ans, CreditBalance(0, 0, 0, true)), taxReturn = createViewModel(ans))

        val paragraphText = view.getElementsByClass("govuk-body").text()
        paragraphText must include("If you want to claim tax back as credit, you must do this when you submit your return. " +
          "If you do not claim it now, you must wait until your next return.")
        paragraphText must include(messages("submit-return.check-your-answers.credits.not.claimed.hint"))
        paragraphText must include("Claim tax back as credit")

      }
    }

    "have claimed credit" when {
      "exported and converted both answered yest" in {
        when(appConfig.isCreditsForReturnsFeatureEnabled).thenReturn(true)
        val view = createView(credits = CreditsClaimedDetails(createUserAnswerForClaimedCredit, CreditBalance(10, 40, 300L, true)))

        view.getElementById("exported-answer").children().size() mustBe 6
        assertExportedCreditsAnswer(view, "Yes", "site.yes")
        assertExportedCreditsWeight(view)
        assertConvertedCreditsAnswer(view, 2, "Yes", "site.yes")
        assertConvertedCreditsWeight(view)
        assertCreditsTotalWight(view, 4)
        assertTotalCredits(view, 5)
      }

      "no exported and converted answer no" in {
        when(appConfig.isCreditsForReturnsFeatureEnabled).thenReturn(true)
        val ans = UserAnswers("123")
          .set(ExportedCreditsPage, CreditsAnswer(false, None)).get
          .set(ConvertedCreditsPage, CreditsAnswer(false, None)).get
          .set(WhatDoYouWantToDoPage, true).get

        val view = createView(credits = CreditsClaimedDetails(ans, CreditBalance(10, 40, 300L, true)))

        view.getElementById("exported-answer").children().size() mustBe 4
        assertExportedCreditsAnswer(view, "No", "site.no")
        assertConvertedCreditsAnswer(view, 1, "No", "site.no")
        assertCreditsTotalWight(view, 2)
        assertTotalCredits(view, 3)
      }
    }

    "have change your answer credit link" when {
      "credits is claimed" in {
        when(appConfig.isCreditsForReturnsFeatureEnabled).thenReturn(true)
        val view = createView(credits = CreditsClaimedDetails(createUserAnswerForClaimedCredit, CreditBalance(10, 40, 300L, true)))

        getText(view, "change-credit-link") mustBe "Change any answer from credits"
        getText(view, "change-credit-link") mustBe messages("submit-return.check-your-answers.credits.change.text.link")
        view.getElementById("change-credit-link").select("a").first() must haveHref(controllers.returns.credits.routes.ExportedCreditsController.onPageLoad(CheckMode))
      }
    }

    "can remove a credit" when {
      "credit is claimed" in {
        when(appConfig.isCreditsForReturnsFeatureEnabled).thenReturn(true)
        val view = createView(credits = CreditsClaimedDetails(createUserAnswerForClaimedCredit, CreditBalance(10, 40, 300L, true)))

        getText(view, "remove-credit-link") mustBe "Remove credits"
        getText(view, "remove-credit-link") mustBe messages("submit-return.check-your-answers.credits.remove.text.link")

        view.getElementById("remove-credit-link").select("a").first() must
          haveHref(Call("GET", "#"))
      }
    }

  }

  private def createUserAnswerForClaimedCredit: UserAnswers =
    UserAnswers("123")
      .set(ExportedCreditsPage, CreditsAnswer(true, Some(100L))).get
      .set(ConvertedCreditsPage, CreditsAnswer(true, Some(200L))).get
      .set(WhatDoYouWantToDoPage, true).get

  private def assertExportedCreditsAnswer(view: Html, expectedValue: String, expectedKey: String): Unit = {
    getCreditCellText(view, 0, "dt") mustBe "Tax paid on plastic packaging that has since been exported"
    getCreditCellText(view, 0, "dt") mustBe messages("submit-return.check-your-answers.credits.exported.answer")
    getCreditCellText(view, 0, "dd") mustBe expectedValue
    getCreditCellText(view, 0, "dd") mustBe messages(expectedKey)
  }

  private def assertExportedCreditsWeight(view: Html): Unit = {
    getCreditCellText(view, 1, "dt") mustBe "Weight of exported plastic packaging"
    getCreditCellText(view, 1, "dt") mustBe messages("submit-return.check-your-answers.credits.exported.weight")
    getCreditCellText(view, 1, "dd") mustBe "100kg"
  }

  private def assertConvertedCreditsAnswer(view: Html, row: Int, expectedValue: String, expectedKey: String): Unit = {
    getCreditCellText(view, row, "dt") mustBe "Tax paid on plastic packaging that has since been converted"
    getCreditCellText(view, row, "dt") mustBe messages("submit-return.check-your-answers.credits.converted.answer")
    getCreditCellText(view, row, "dd") mustBe expectedValue
    getCreditCellText(view, row, "dd") mustBe messages(expectedKey)
  }

  private def assertConvertedCreditsWeight(view: Html): Unit = {
    getCreditCellText(view, 3, "dt") mustBe "Weight of converted plastic packaging"
    getCreditCellText(view, 3, "dt") mustBe messages("submit-return.check-your-answers.credits.converted.weight")
    getCreditCellText(view, 3, "dd") mustBe "200kg"
  }

  private def assertCreditsTotalWight(view: Html, row: Int): Unit = {
    getCreditCellText(view, row, "dt") mustBe "Total weight"
    getCreditCellText(view, row, "dt") mustBe messages("submit-return.check-your-answers.credits.total.weight")
    getCreditCellText(view, row, "dd") mustBe "300kg"
  }

  private def assertTotalCredits(view: Html, row: Int): Unit = {
    getCreditCellText(view, row, "dt") mustBe "Credit total"
    getCreditCellText(view, row, "dt") mustBe messages("submit-return.check-your-answers.credits.total")
    getCreditCellText(view, row, "dd") mustBe "£40.00"
  }

  private def getCreditCellText(view: Html, row: Int, cell: String) =
    getCreditRow(view, row).select(cell).text()

  private def getCreditRow(view: Html, row: Int): Element =
    view.getElementById("exported-answer").child(row)

  private def getText(view: Html, id: String): String =
    view.getElementById(id).text()

}
