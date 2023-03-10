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
import controllers.returns.routes
import models.Mode.CheckMode
import models.returns.Credits.{NoCreditAvailable, NoCreditsClaimed}
import models.returns._
import models.{CreditBalance, UserAnswers}
import org.jsoup.nodes.Element
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.mock
import pages.returns._
import pages.returns.credits.{ConvertedCreditsPage, ExportedCreditsPage, WhatDoYouWantToDoPage}
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.twirl.api.Html
import repositories.SessionRepository
import support.{ViewAssertions, ViewMatchers}
import viewmodels.TaxReturnViewModel
import views.html.returns.ReturnsCheckYourAnswersView

import java.time.LocalDate

class ReturnsCheckYourAnswersViewSpec extends ViewSpecBase with ViewAssertions with ViewMatchers {

  private lazy val page: ReturnsCheckYourAnswersView = inject[ReturnsCheckYourAnswersView]
  private val appConfig                      = mock[FrontendAppConfig]

  private val aTaxObligation: TaxReturnObligation = TaxReturnObligation(
    LocalDate.of(2022, 4, 1),
    LocalDate.of(2022, 6, 30),
    LocalDate.of(2022, 8,1), "PK1")



  private val calculations    = Calculations(1, 2L, 3L, 5L, true, 0.3)
  private val returnViewModel = createViewModel(createUserAnswer)

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .overrides(
        bind[FrontendAppConfig].toInstance(appConfig),
        bind[SessionRepository].toInstance(mock[SessionRepository])
      ).build()

  def createViewModel(answers: UserAnswers, calculations: Calculations = calculations): TaxReturnViewModel = {
    TaxReturnViewModel(answers, "reg-number", aTaxObligation, calculations)
  }

  private def createView (
    credits: Credits = CreditsClaimedDetails(createUserAnswer, CreditBalance(0, 0, 0L, true)),
    taxReturn: TaxReturnViewModel = returnViewModel
  ): Html =
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

  "display Business details section" should {
    val view = createView()
    val text = view.getElementsByClass("govuk-summary-list").text()

    "display header" in {
      view.select("h2").get(1).text() mustBe "Business details"
      view.select("h2").get(1).text() mustBe messages("submit-return.check-your-answers.business-details.heading")
    }

    "display the registration number" in {
      text must include("Plastic Packaging Tax registration number")
      text must include(messages("submit-return.check-your-answers.business-details.row1"))
      text must include("reg-number")
    }

    "display return period start date" in {
      text must include("Return period start date")
      text must include(messages("submit-return.check-your-answers.business-details.periodStartDate"))
      text must include("1 April 2022")
    }

    "display return period end date" in {
      text must include("Return period end date")
      text must include(messages("submit-return.check-your-answers.business-details.periodEndDate"))
      text must include("30 June 2022")
    }
  }

  "display guidance" when {
    "is first return" in {
      when(appConfig.isCreditsForReturnsFeatureEnabled).thenReturn(true)
      val view = createView(credits = NoCreditAvailable)
      assertNoCreditsAvailable(view)
    }
  }

  "Credits section" should {
    "display guidance" when {
      "credits feature is toggled off" in {
        when(appConfig.isCreditsForReturnsFeatureEnabled).thenReturn(false)
        val ans  = createUserAnswer.set(WhatDoYouWantToDoPage, false).get

        val view = createView(CreditsClaimedDetails(ans, CreditBalance(0, 0, 0, true)))

        assertNoCreditsAvailable(view)
      }
    }

    "hide guidance" when {
      "credits feature is toggled on" in {
        when(appConfig.isCreditsForReturnsFeatureEnabled).thenReturn(true)
        createView().getElementById("credits-line-1") mustBe null
      }
    }

    "have no credits claimed" in {
      when(appConfig.isCreditsForReturnsFeatureEnabled).thenReturn(true)
      val ans  = createUserAnswer.set(WhatDoYouWantToDoPage, false).get
      val view = createView(
        credits = NoCreditsClaimed,
        taxReturn = createViewModel(ans))

      val paragraphText = view.getElementsByClass("govuk-body").text()
      paragraphText must include("If you want to claim tax back as credit, you must do this when you submit your return. " +
        "If you do not claim it now, you must wait until your next return.")
      paragraphText must include(messages("submit-return.check-your-answers.credits.not.claimed.hint"))
      paragraphText must include("Claim tax back as credit")

    }

    "have claimed credit" when {
      "exported and converted both answered yes" in {
        when(appConfig.isCreditsForReturnsFeatureEnabled).thenReturn(true)
        val view = createView(
          credits = CreditsClaimedDetails(createUserAnswerForClaimedCredit, CreditBalance(10, 40, 300L, true)))

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

        val view = createView(
          credits = CreditsClaimedDetails(ans, CreditBalance(10, 40, 300L, true)))

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
        val view = createView(
          credits = CreditsClaimedDetails(createUserAnswerForClaimedCredit, CreditBalance(10, 40, 300L, true)))

        getText(view, "change-credit-link") mustBe "Change any answer from credits"
        getText(view, "change-credit-link") mustBe messages("submit-return.check-your-answers.credits.change.text.link")
        view.getElementById("change-credit-link").select("a").first() must haveHref(controllers.returns.credits.routes.ExportedCreditsController.onPageLoad(CheckMode))
      }
    }

    "can remove a credit" when {
      "credit is claimed" in {
        when(appConfig.isCreditsForReturnsFeatureEnabled).thenReturn(true)
        val view = createView(
          credits = CreditsClaimedDetails(createUserAnswerForClaimedCredit, CreditBalance(10, 40, 300L, true))
        )

        getText(view, "remove-credit-link") mustBe "Remove credits"
        getText(view, "remove-credit-link") mustBe messages("submit-return.check-your-answers.credits.remove.text.link")

        view.getElementById("remove-credit-link").select("a").first() must
          haveHref(controllers.returns.credits.routes.RemoveCreditController.onPageLoad())
      }
    }

  }

  "Exported Plastic Packaging section" should {
    val view = createView()
    val summaryListTexts = view.getElementsByClass("govuk-summary-list").get(2).text()

    "display header" in {
      view.select("h3").text() must include("Exported plastic packaging")
      view.select("h3").text() must include(messages("submit-return.check-your-answers.exported-packaging.heading"))
    }

    "display 'Plastic packaging exported by you' question and answer" in {
      summaryListTexts must include("Plastic packaging exported by you Yes")
      summaryListTexts must include(messages("submit-return.check-your-answers.exported-packaging.row1"))
    }

    "display 'Weight of plastic packaging exported by you' question and answer" in {
      summaryListTexts must include("Weight of plastic packaging exported by you 50kg")
      summaryListTexts must include(messages("submit-return.check-your-answers.exported-packaging.row2"))
    }

    "display 'Plastic packaging exported or converted by another business' question and answer" in {
      summaryListTexts must include("Plastic packaging exported or converted by another business Yes")
      summaryListTexts must include(messages("submit-return.check-your-answers.exported-packaging-by-another-business-label"))
    }

    "display 'Weight of plastic packaging exported or converted by another business' question and answer" in {
      summaryListTexts must include("Weight of plastic packaging exported or converted by another business 150kg")
      summaryListTexts must include(messages("submit-return.check-your-answers.exported-packaging-by-another-business-weight"))
    }

    "should no able to change answer" in {

      val ans = createUserAnswer
        .set(ManufacturedPlasticPackagingWeightPage, 20L).get
        .set(ImportedPlasticPackagingWeightPage, 40L).get
        .set(DirectlyExportedWeightPage, 0L).get
        .set(AnotherBusinessExportedWeightPage, 0L).get

      val view = createView(taxReturn = createViewModel(ans, Calculations(1,1,1,0, true, 200.0)))
      val text = view.select("p").text()

      text must include("To change an answer from exported plastic packaging you must have manufactured or imported plastic packaging.")
      text must include(messages("submit-return.check-your-answers.exported-packaging.no-change-reason"))
    }

    "should be able to change answer" in {

      val ans = createUserAnswer
        .set(ManufacturedPlasticPackagingWeightPage, 20L).get
        .set(ImportedPlasticPackagingWeightPage, 40L).get
        .set(DirectlyExportedWeightPage, 0L).get
        .set(AnotherBusinessExportedWeightPage, 50L).get

      val view = createView(taxReturn = createViewModel(ans))

      val text = view.getElementsByClass("govuk-body").text()
      text must include("Change any answer from exported plastic packaging")
      text must include(messages("submit-return.check-your-answers.exported-packaging.change-link-text"))

      view.getElementById("exported-packaging") must haveHref(routes.DirectlyExportedComponentsController.onPageLoad(CheckMode).url)
    }

  }

  "Deduction section" should {
    val view = createView()
    val summaryListTexts = view.getElementsByClass("govuk-summary-list").get(4).text()

    "display header" in {
      view.select("h3").text() must include("Deductions")
      view.select("h3").text() must include(messages("submit-return.check-your-answers.deductions.heading"))
    }

    "display exported plastic packaging  weight" in {
      summaryListTexts must include("Weight of plastic packaging exported by you 50kg")
      summaryListTexts must include(messages("submit-return.check-your-answers.deductions.row1"))
    }

    "display exported by another business plastic packaging  weight" in {
      summaryListTexts must include("Weight of plastic packaging exported or converted by another business 150kg")
      summaryListTexts must include(messages("submit-return.check-your-answers.deductions.row2"))
    }

    "display non exported plastic packaging weight" in {
      summaryListTexts must include("Weight of non-exported plastic packaging used for licenced human medicines 20kg")
      summaryListTexts must include(messages("submit-return.check-your-answers.deductions.row3"))
    }

    "display non exported recycled plastic packaging weight" in {
      summaryListTexts must include("Weight of non-exported plastic packaging containing 30% or more recycled 25kg")
      summaryListTexts must include(messages("submit-return.check-your-answers.deductions.row4"))
    }

    "display total deduction" in {
      summaryListTexts must include("Deductions total 3kg")
      summaryListTexts must include(messages("submit-return.check-your-answers.deductions.row5"))
    }
  }

  "display tax calculation" should {
    val view = createView()

    "have a header" in {
      view.select("h3").text() must include("Your tax calculation")
      view.select("h3").text() must include(messages("submit-return.check-your-answers.tax-calc.heading"))
    }

    "display tax rate" in {
      view.getElementsByClass("govuk-body").text() must include("For this period, tax is charged at a rate of £300 per tonne.")
      view.getElementsByClass("govuk-body").text() must include(
        messages("submit-return.check-your-answers.tax-calc.footnote", "£300"))
    }
  }


  private def assertNoCreditsAvailable(view: Html) = {
    val paragraphText = view.getElementsByClass("govuk-body").text()
    paragraphText must include(
      "You cannot claim tax back as credit yet. This is because this is your first return."
    )
    paragraphText must include(messages("submit-return.check-your-answers.credits.line1"))

    paragraphText must include(
     "You may be able to claim tax back as credit in the future if packaging you’ve paid tax on is either:"
    )
    paragraphText must include(messages("submit-return.check-your-answers.credits.line2"))
    paragraphText must include("Find out more about claiming tax back as credit (opens in new tab).")
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
    assertIfCellIsBold(view, row, "dt")
    getCreditCellText(view, row, "dd") mustBe "£40.00"
    assertIfCellIsBold(view, row, "dd")
  }

  private def assertIfCellIsBold(view: Html, row: Int, cell: String): Unit = {
    getCreditRow(view, row).select(cell).first().getElementsByClass(
      "govuk-!-font-weight-bold"
    ).size() mustBe 1
  }
  private def getCreditCellText(view: Html, row: Int, cell: String) =
    getCreditRow(view, row).select(cell).text()

  private def getCreditRow(view: Html, row: Int): Element =
    view.getElementById("exported-answer").child(row)

  private def getText(view: Html, id: String): String =
    view.getElementById(id).text()

  private def createUserAnswer = UserAnswers("reg-number")
    .set(ManufacturedPlasticPackagingPage, false).get
    .set(ImportedPlasticPackagingPage, false).get
    .set(DirectlyExportedPage, true).get
    .set(DirectlyExportedWeightPage, 50L).get
    .set(AnotherBusinessExportedPage, true).get
    .set(AnotherBusinessExportedWeightPage, 150L).get
    .set(NonExportedHumanMedicinesPlasticPackagingPage, true).get
    .set(NonExportedHumanMedicinesPlasticPackagingWeightPage, 20L).get
    .set(NonExportedRecycledPlasticPackagingPage, true).get
    .set(NonExportedRecycledPlasticPackagingWeightPage, 25L).get
    .set(ExportedCreditsPage, CreditsAnswer(false, None)).get
    .set(ConvertedCreditsPage, CreditsAnswer(true, Some(0))).get
    .set(WhatDoYouWantToDoPage, true).get
}

