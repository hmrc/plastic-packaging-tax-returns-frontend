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

package navigation

import config.FrontendAppConfig
import controllers.helpers.NonExportedAmountHelper
import controllers.returns.credits.{routes => creditRoutes}
import controllers.returns.{routes => returnsRoutes}
import forms.returns.credits.ClaimForWhichYearFormProvider.CreditRangeOption
import models.Mode.{CheckMode, NormalMode}
import models.UserAnswers
import models.returns.CreditsAnswer
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.Mockito.verify
import org.mockito.MockitoSugar.{mock, reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import queries.Gettable

class ReturnsJourneyNavigatorSpec extends PlaySpec with BeforeAndAfterEach {

  private val frontendConfig = mock[FrontendAppConfig]
  private val userAnswers = mock[UserAnswers]
  private val nonExportedAmountHelper = mock[NonExportedAmountHelper]

  private val navigator = new ReturnsJourneyNavigator(frontendConfig, nonExportedAmountHelper)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(userAnswers, frontendConfig, nonExportedAmountHelper)
  }

  //todo add in credits navigation that is currently tested in controllers
  
  "The start your return page" must {
    "goto the what do you want to do page" in {
      when(frontendConfig.isFeatureEnabled(any)) thenReturn true
      navigator.startYourReturn(true, false) mustBe 
        creditRoutes.WhatDoYouWantToDoController.onPageLoad(NormalMode)
    }
    "except when the credits feature is disabled" in {
      when(frontendConfig.isFeatureEnabled(any)) thenReturn false
      navigator.startYourReturn(true, false) mustBe
        returnsRoutes.ManufacturedPlasticPackagingController.onPageLoad(NormalMode)
    }
    "except when it is the users first return" in {
      when(frontendConfig.isFeatureEnabled(any)) thenReturn true
      navigator.startYourReturn(true, isFirstReturn = true) mustBe
        returnsRoutes.ManufacturedPlasticPackagingController.onPageLoad(NormalMode)
    }
    "go back to the account home page" in {
      when(frontendConfig.isFeatureEnabled(any)) thenReturn true
      navigator.startYourReturn(doesUserWantToStartReturn = false, false) mustBe
        returnsRoutes.NotStartOtherReturnsController.onPageLoad()
    }
  }
  
  "what do you want to do page" should {
    "go to claim for which year" in {
      navigator.whatDoYouWantDo(NormalMode, isClaimingCredit = true) mustBe creditRoutes.ClaimForWhichYearController.onPageLoad(NormalMode)
      navigator.whatDoYouWantDo(CheckMode, isClaimingCredit = true) mustBe creditRoutes.ClaimForWhichYearController.onPageLoad(CheckMode)
    }
    "skip credit, go to the first page of the return" in {
      navigator.whatDoYouWantDo(NormalMode, isClaimingCredit = false) mustBe returnsRoutes.ManufacturedPlasticPackagingController.onPageLoad(NormalMode)
    }
    "go back to return CYA" in {
      navigator.whatDoYouWantDo(CheckMode, isClaimingCredit = false) mustBe returnsRoutes.ReturnsCheckYourAnswersController.onPageLoad()
    }
  }
  
  "claim for which year page" should {
    "include the key" in {
      val year = mock[CreditRangeOption]
      when(year.key) thenReturn "a-key"
      navigator.claimForWhichYear(year, NormalMode) mustBe creditRoutes.ExportedCreditsController.onPageLoad("a-key", NormalMode)
      navigator.claimForWhichYear(year, CheckMode) mustBe creditRoutes.ExportedCreditsController.onPageLoad("a-key", CheckMode)
    }
  }

  "exportedCreditsYesNo" must {
    "correctly redirect" when {
      "user answer is yes, in normal mode" in {
        when(userAnswers.get(any[Gettable[Any]])(any)) thenReturn Some(CreditsAnswer.noClaim)
        val call = navigator.exportedCreditsYesNo("year-key", NormalMode, true, userAnswers)
        call mustBe controllers.returns.credits.routes.ExportedCreditsWeightController.onPageLoad("year-key", NormalMode)
      }

      "user answer is yes, in check mode" in {
        when(userAnswers.get(any[Gettable[Any]])(any)) thenReturn Some(CreditsAnswer.noClaim)
        val call = navigator.exportedCreditsYesNo("year-key", CheckMode, true, userAnswers)
        call mustBe controllers.returns.credits.routes.ExportedCreditsWeightController.onPageLoad("year-key", CheckMode)
      }

      "user answer is no, it is in normal mode" in {
        when(userAnswers.get(any[Gettable[Any]])(any)) thenReturn Some(CreditsAnswer.noClaim)
        val call = navigator.exportedCreditsYesNo("year-key", NormalMode, false, userAnswers)
        call mustBe controllers.returns.credits.routes.ConvertedCreditsController.onPageLoad("year-key", NormalMode)
      }
      "user answer is no, it is in Check mode but converted questions have not been done" in {
        when(userAnswers.get(any[Gettable[Any]])(any)) thenReturn None
        val call = navigator.exportedCreditsYesNo("year-key", CheckMode, false, userAnswers)
        call mustBe controllers.returns.credits.routes.ConvertedCreditsController.onPageLoad("year-key", CheckMode)
      }
      "user answer is no, it is in Check mode and converted questions have been done" in {
        when(userAnswers.get(any[Gettable[Any]])(any)) thenReturn Some(CreditsAnswer.noClaim)
        val call = navigator.exportedCreditsYesNo("year-key", CheckMode, false, userAnswers)
        call mustBe controllers.returns.credits.routes.ConfirmPackagingCreditController.onPageLoad("year-key", CheckMode)
      }
    }
  }
  
  "exportedCreditsWeight" must {

    "redirect to converted yes-no page" when {
      "in normal mode" in {
        when(userAnswers.get(any[Gettable[Any]])(any)) thenReturn None
        val call = navigator.exportedCreditsWeight("year-key", NormalMode, userAnswers)
        call mustBe creditRoutes.ConvertedCreditsController.onPageLoad("year-key", NormalMode)
      }

      "in check mode and Converted questions are NOT done" in {
        when(userAnswers.get(any[Gettable[Any]])(any)) thenReturn None
        val call = navigator.exportedCreditsWeight("year-key", CheckMode, userAnswers)
        call mustBe creditRoutes.ConvertedCreditsController.onPageLoad("year-key", CheckMode)
      }

      "in check mode and Converted questions are done" in {
        when(userAnswers.get(any[Gettable[Any]])(any)) thenReturn Some(CreditsAnswer.noClaim)
        val call = navigator.exportedCreditsWeight("year-key", CheckMode, userAnswers)
        call mustBe creditRoutes.ConfirmPackagingCreditController.onPageLoad("year-key", CheckMode)
      }
    }
  }

  "convertedCreditsYesNo" must {
    
    "redirect to weight page when user answers 'yes'" when {
      "normal mode" in {
        val call = navigator.convertedCreditsYesNo(NormalMode,"year-key",  true)
        call mustBe creditRoutes.ConvertedCreditsWeightController.onPageLoad("year-key", NormalMode)
      }
      "check mode" in {
        val call = navigator.convertedCreditsYesNo(CheckMode,"year-key",  true)
        call mustBe creditRoutes.ConvertedCreditsWeightController.onPageLoad("year-key", CheckMode)
      }
    }
    
    "skip weight page when user answers 'no'" when {
      "normal mode" in {
        val call = navigator.convertedCreditsYesNo(NormalMode,"year-key",  false)
        call mustBe creditRoutes.ConfirmPackagingCreditController.onPageLoad("year-key", NormalMode)
      }
      "check mode" in {
        val call = navigator.convertedCreditsYesNo(CheckMode,"year-key", false)
        call mustBe creditRoutes.ConfirmPackagingCreditController.onPageLoad("year-key", CheckMode)
      }
    }
    
  }

  "convertedCreditsWeight" must {
    "redirect to confirm-or-correct-credit page" when {
      "in NormalMode" in {
        val call = navigator.convertedCreditsWeight("year-key", NormalMode)
        call mustBe creditRoutes.ConfirmPackagingCreditController.onPageLoad("year-key", NormalMode)
      }

      "in CheckMode" in {
        val call = navigator.convertedCreditsWeight("year-key", CheckMode)
        call mustBe creditRoutes.ConfirmPackagingCreditController.onPageLoad("year-key", CheckMode)
      }
    }
  }

  "confirmCreditRoute" must {
    "lead to the credit year list summary" in {
      navigator.confirmCredit(NormalMode) mustBe creditRoutes.CreditsClaimedListController.onPageLoad(NormalMode)
      navigator.confirmCredit(CheckMode) mustBe creditRoutes.CreditsClaimedListController.onPageLoad(CheckMode)
    }
  }
  
  "summary list of credit years claimed" when {
    
    "in normal mode" in {
      navigator.creditClaimedList(NormalMode, isAddingAnotherYear = false, userAnswers) mustBe 
        returnsRoutes.NowStartYourReturnController.onPageLoad
      navigator.creditClaimedList(NormalMode, isAddingAnotherYear = true, userAnswers) mustBe 
        creditRoutes.ClaimForWhichYearController.onPageLoad(NormalMode)
    }
    
    "in check mode without returns section completed" in {
      when(nonExportedAmountHelper.returnsQuestionsAnswered(any)) thenReturn false
      navigator.creditClaimedList(CheckMode, isAddingAnotherYear = false, userAnswers) mustBe
        returnsRoutes.NowStartYourReturnController.onPageLoad
      navigator.creditClaimedList(CheckMode, isAddingAnotherYear = true, userAnswers) mustBe 
        creditRoutes.ClaimForWhichYearController.onPageLoad(CheckMode)
    }

    "in check mode and returns section has been completed" in {
      when(nonExportedAmountHelper.returnsQuestionsAnswered(any)) thenReturn true
      navigator.creditClaimedList(CheckMode, isAddingAnotherYear = false, userAnswers) mustBe
        returnsRoutes.ReturnsCheckYourAnswersController.onPageLoad
      navigator.creditClaimedList(CheckMode, isAddingAnotherYear = true, userAnswers) mustBe
        creditRoutes.ClaimForWhichYearController.onPageLoad(CheckMode)
    }    

    "redirect to CYA page in CheckMode Mode and have NOT done Returns questions" in {
      navigator.creditClaimedList(CheckMode, false, userAnswers) mustBe returnsRoutes.NowStartYourReturnController.onPageLoad
    }

    "redirect to CYA page in CheckMode Mode and have done Returns questions" in {
      when(nonExportedAmountHelper.returnsQuestionsAnswered(userAnswers)).thenReturn(true)
      navigator.creditClaimedList(CheckMode, false, userAnswers) mustBe returnsRoutes.ReturnsCheckYourAnswersController.onPageLoad()
    }

  }
  
  "start your return" should {
    "go to the first page of the return" in {
      navigator.startYourReturn mustBe returnsRoutes.ManufacturedPlasticPackagingController.onPageLoad(NormalMode)
    }
  }

  "manufacturedPlasticPackagingRoute" must {
    
    "redirect to manufacturedWeight page in normal mode" when {
      "answer is Yes" in {
        val call = navigator.manufacturedPlasticPackaging(NormalMode, hasAnswerChanged = true, usersAnswer = true)
        call mustBe returnsRoutes.ManufacturedPlasticPackagingWeightController.onPageLoad(NormalMode)
      }
      "answer is Yes and not been changed" in {
        val call = navigator.manufacturedPlasticPackaging(NormalMode, hasAnswerChanged = false, usersAnswer = true)
        call mustBe returnsRoutes.ManufacturedPlasticPackagingWeightController.onPageLoad(NormalMode)
      }
    }

    "redirect to to Imported yes/no page in normal mode" when {
      "answer is No" in {
        val call = navigator.manufacturedPlasticPackaging(NormalMode, hasAnswerChanged = true, usersAnswer = false)
        call mustBe returnsRoutes.ImportedPlasticPackagingController.onPageLoad(NormalMode)
      }

      "answer is No and not been changed" in {
        val call = navigator.manufacturedPlasticPackaging(NormalMode, hasAnswerChanged = false, usersAnswer = false)
        call mustBe returnsRoutes.ImportedPlasticPackagingController.onPageLoad(NormalMode)
      }
    }

    "redirect to manufacturedWeight for check mode" when {
      "answer is Yes and has been changed" in {
        val call = navigator.manufacturedPlasticPackaging(CheckMode, hasAnswerChanged = true, usersAnswer = true)
        call mustBe returnsRoutes.ManufacturedPlasticPackagingWeightController.onPageLoad(CheckMode)
      }
    }

    "redirect to check your answers page for check mode" when {
      "answer is No and has been changed" in {
        val call = navigator.manufacturedPlasticPackaging(CheckMode, hasAnswerChanged = true, usersAnswer = false)
        call mustBe returnsRoutes.ConfirmPlasticPackagingTotalController.onPageLoad
      }

      "answer is Yes and has not been changed" in {
        val call = navigator.manufacturedPlasticPackaging(CheckMode, hasAnswerChanged = false, usersAnswer = true)
        call mustBe returnsRoutes.ConfirmPlasticPackagingTotalController.onPageLoad
      }

      "answer is No" in {
        val call = navigator.manufacturedPlasticPackaging(CheckMode, hasAnswerChanged = false, usersAnswer = false)
        call mustBe returnsRoutes.ConfirmPlasticPackagingTotalController.onPageLoad
      }
    }
  }

  "ImportedPlasticPackagingRoute" must {
    "redirect to imported weight page in  normal mode" when {

      "answer is Yes and has changed" in {
        val call = navigator.importedPlasticPackaging(NormalMode, hasAnswerChanged = true, usersAnswer = true)
        call mustBe returnsRoutes.ImportedPlasticPackagingWeightController.onPageLoad(NormalMode)
      }

      "answer is Yes and has not changed" in {
        val call = navigator.importedPlasticPackaging(NormalMode, hasAnswerChanged = false, usersAnswer = true)
        call mustBe returnsRoutes.ImportedPlasticPackagingWeightController.onPageLoad(NormalMode)
      }
    }
    "redirect to imported weight page in  normal mode" when {

      "answer is No and has changed" in {
        val call = navigator.importedPlasticPackaging(NormalMode, hasAnswerChanged = true, usersAnswer = false)
        call mustBe returnsRoutes.ConfirmPlasticPackagingTotalController.onPageLoad
      }


      "answer is No and has not changed" in {
        val call = navigator.importedPlasticPackaging(NormalMode, hasAnswerChanged = false, usersAnswer = false)
        call mustBe returnsRoutes.ConfirmPlasticPackagingTotalController.onPageLoad
      }
    }
  }

  "exportedPlasticPackagingWeightRoute" when {
    
    "all plastic is exported  in NormalMode" in {
      val call = navigator.exportedPlasticPackagingWeightRoute(true, NormalMode)
      call mustBe returnsRoutes.ReturnsCheckYourAnswersController.onPageLoad()
    }

    "only some plastic exported in NormalMode" in {
      val call = navigator.exportedPlasticPackagingWeightRoute(false, NormalMode)
      call mustBe returnsRoutes.PlasticExportedByAnotherBusinessController.onPageLoad(NormalMode)
    }

    "all plastic exported in CheckMode" in {
      val call = navigator.exportedPlasticPackagingWeightRoute(true, CheckMode)
      call mustBe returnsRoutes.PlasticExportedByAnotherBusinessController.onPageLoad(CheckMode)
    }

    "only some plastic exported in CheckMode" in {
      val call = navigator.exportedPlasticPackagingWeightRoute(false, CheckMode)
      call mustBe returnsRoutes.PlasticExportedByAnotherBusinessController.onPageLoad(CheckMode)
    }
  }

  "exportedByAnotherBusinessWeightRoute" when {
    
    "all plastic exported in NormalMode" in {
      val call = navigator.exportedByAnotherBusinessWeightRoute(true, NormalMode)
      call mustBe returnsRoutes.ReturnsCheckYourAnswersController.onPageLoad()
    }

    "only some plastic is exported in NormalMode" in {
      val call = navigator.exportedByAnotherBusinessWeightRoute(false, NormalMode)
      call mustBe returnsRoutes.NonExportedHumanMedicinesPlasticPackagingController.onPageLoad(NormalMode)
    }

    "all plastic exported in CheckMode" in {
      val call = navigator.exportedByAnotherBusinessWeightRoute(true, CheckMode)
      call mustBe returnsRoutes.ReturnsCheckYourAnswersController.onPageLoad()
    }

    "only some plastic is exported in CheckMode" in {
      val call = navigator.exportedByAnotherBusinessWeightRoute(false, CheckMode)
      call mustBe returnsRoutes.ReturnsCheckYourAnswersController.onPageLoad()
    }
  }

  "ConfirmTotalPlasticPackagingRoute" must {

    "redirect to directly exported page page" in {
      when(nonExportedAmountHelper.totalPlasticAdditions(any)).thenReturn(Some(1L))
      val call = navigator.confirmTotalPlasticPackagingRoute(userAnswers)
      call mustBe returnsRoutes.DirectlyExportedComponentsController.onPageLoad(NormalMode)
      verify(nonExportedAmountHelper).totalPlasticAdditions(userAnswers)
    }

    "redirect to CYA page" in {
      when(nonExportedAmountHelper.totalPlasticAdditions(any)).thenReturn(Some(0L))
      val call = navigator.confirmTotalPlasticPackagingRoute(userAnswers)
      call mustBe returnsRoutes.ReturnsCheckYourAnswersController.onPageLoad()
    }

    "redirect to account page" in {
      when(nonExportedAmountHelper.totalPlasticAdditions(any)).thenReturn(None)
      val call = navigator.confirmTotalPlasticPackagingRoute(userAnswers)
      call mustBe controllers.routes.IndexController.onPageLoad
    }
  }

  "for check mode" when {

    "when answer is Yes and has been changed" in {
      val call = navigator.importedPlasticPackaging(CheckMode, hasAnswerChanged = true, usersAnswer = true)
      call mustBe returnsRoutes.ImportedPlasticPackagingWeightController.onPageLoad(CheckMode)
    }

    "when answer is Yes and has not been changed" in {
      val call = navigator.importedPlasticPackaging(CheckMode, hasAnswerChanged = false, usersAnswer = true)
      call mustBe returnsRoutes.ConfirmPlasticPackagingTotalController.onPageLoad
    }

    "when answer is No and has has been changed" in {
      val call = navigator.importedPlasticPackaging(CheckMode, hasAnswerChanged = true, usersAnswer = false)
      call mustBe returnsRoutes.ConfirmPlasticPackagingTotalController.onPageLoad
    }

    "when answer is No and has has not been changed" in {
      val call = navigator.importedPlasticPackaging(CheckMode, hasAnswerChanged = false, usersAnswer = false)
      call mustBe returnsRoutes.ConfirmPlasticPackagingTotalController.onPageLoad
    }

  }

  "directlyExportedComponentsRoute" when {
    
    "answer is yes in check mode" in {
      navigator.directlyExportedComponentsRoute(true, CheckMode) mustBe
        returnsRoutes.ExportedPlasticPackagingWeightController.onPageLoad(CheckMode)
    }

    "answer is yes in normal mode" in {
      navigator.directlyExportedComponentsRoute(true, NormalMode) mustBe
        returnsRoutes.ExportedPlasticPackagingWeightController.onPageLoad(NormalMode)
    }

    "answer is no in check mode" in {
      navigator.directlyExportedComponentsRoute(false, CheckMode) mustBe
        returnsRoutes.PlasticExportedByAnotherBusinessController.onPageLoad(CheckMode)
    }

    "answer is no in normal mode" in {
      navigator.directlyExportedComponentsRoute(false, NormalMode) mustBe
        returnsRoutes.PlasticExportedByAnotherBusinessController.onPageLoad(NormalMode)
    }
    
  }
  
  "exportedByAnotherBusinessRoute" when {
    
    "answer is yes in normal mode" in {
      when(userAnswers.get(any[Gettable[Boolean]])(any)) thenReturn Some(true)
      navigator.exportedByAnotherBusinessRoute(userAnswers, NormalMode) mustBe 
        returnsRoutes.AnotherBusinessExportWeightController.onPageLoad(NormalMode)
    }
    
    "answer is yes in check mode" in {
      when(userAnswers.get(any[Gettable[Boolean]])(any)) thenReturn Some(true)
      navigator.exportedByAnotherBusinessRoute(userAnswers, CheckMode) mustBe
        returnsRoutes.AnotherBusinessExportWeightController.onPageLoad(CheckMode)
    }

    "answer is no in normal mode" in {
      when(userAnswers.get(any[Gettable[Boolean]])(any)) thenReturn Some(false)
      navigator.exportedByAnotherBusinessRoute(userAnswers, NormalMode) mustBe
        returnsRoutes.NonExportedHumanMedicinesPlasticPackagingController.onPageLoad(NormalMode)
    }

    "answer is no in check mode" in {
      when(userAnswers.get(any[Gettable[Boolean]])(any)) thenReturn Some(false)
      navigator.exportedByAnotherBusinessRoute(userAnswers, CheckMode) mustBe
        returnsRoutes.ReturnsCheckYourAnswersController.onPageLoad()
    }
  }
  
  "manufacturedPlasticPackagingWeightRoute" in {
    when(userAnswers.get(any[Gettable[Any]])(any)) thenReturn Some(1L)
    navigator.manufacturedPlasticPackagingWeight(userAnswers) mustBe 
      returnsRoutes.ConfirmPlasticPackagingTotalController.onPageLoad     
  }

  "cancelCreditRoute" should {
    "redirect to submit-return-or-claim-credit when credit is cancel" in {
      navigator.cancelCreditRoute("year-key", true) mustBe
      controllers.returns.credits.routes.WhatDoYouWantToDoController.onPageLoad(NormalMode)
    }

    "redirect to confirm-or-correct-credit page not cancelled" in {
      navigator.cancelCreditRoute("year-key", false) mustBe
        controllers.returns.credits.routes.ConfirmPackagingCreditController.onPageLoad("year-key", NormalMode)
    }
  }
}

