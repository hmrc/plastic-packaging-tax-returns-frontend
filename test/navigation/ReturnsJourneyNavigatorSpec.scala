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
import controllers.returns.credits.{ClaimedCredits, routes => creditsRoutes}
import controllers.returns.{routes => returnsRoutes}
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
  private val mockClaimedCredits = mock[ClaimedCredits]
  private val userAnswers = mock[UserAnswers]
  private val nonExportedAmountHelper = mock[NonExportedAmountHelper]

  private val navigator = new ReturnsJourneyNavigator(frontendConfig, nonExportedAmountHelper)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockClaimedCredits, frontendConfig, nonExportedAmountHelper)
  }

  //todo add in credits navigation that is currently tested in controllers
  
  "The start your return page" must {
    "goto the what do you want to do page" in {
      when(frontendConfig.isFeatureEnabled(any)) thenReturn true
      navigator.startYourReturnRoute(true, false) mustBe 
        creditsRoutes.WhatDoYouWantToDoController.onPageLoad(NormalMode)
    }
    "except when the credits feature is disabled" in {
      when(frontendConfig.isFeatureEnabled(any)) thenReturn false
      navigator.startYourReturnRoute(true, false) mustBe
        returnsRoutes.ManufacturedPlasticPackagingController.onPageLoad(NormalMode)
    }
    "except when it is the users first return" in {
      when(frontendConfig.isFeatureEnabled(any)) thenReturn true
      navigator.startYourReturnRoute(true, isFirstReturn = true) mustBe
        returnsRoutes.ManufacturedPlasticPackagingController.onPageLoad(NormalMode)
    }
    "go back to the account home page" in {
      when(frontendConfig.isFeatureEnabled(any)) thenReturn true
      navigator.startYourReturnRoute(doesUserWantToStartReturn = false, false) mustBe
        returnsRoutes.NotStartOtherReturnsController.onPageLoad()
    }
  }

  "exportedCreditsYesNo" must {

    "redirect to weight page when user answers 'yes'" when {
      "in normal mode" in {
        val call = navigator.exportedCreditsYesNo(NormalMode, true)
        call mustBe creditsRoutes.ExportedCreditsWeightController.onPageLoad(NormalMode)
      }

      "in check mode" in {
        val call = navigator.exportedCreditsYesNo(CheckMode, true)
        call mustBe creditsRoutes.ExportedCreditsWeightController.onPageLoad(CheckMode)
      }
    }

    "redirect to converted yes-no page when user answers 'no'" when {
      "in normal mode" in {
        val call = navigator.exportedCreditsYesNo(NormalMode, false)
        call mustBe creditsRoutes.ConvertedCreditsController.onPageLoad(NormalMode)
      }

      "in check mode" in {
        val call = navigator.exportedCreditsYesNo(CheckMode, false)
        call mustBe creditsRoutes.ConvertedCreditsController.onPageLoad(CheckMode)
      }
    }
    
  }
  "exportedCreditsWeight" must {

    "redirect to converted yes-no page" when {
      "in normal mode" in {
        val call = navigator.exportedCreditsWeight(NormalMode)
        call mustBe creditsRoutes.ConvertedCreditsController.onPageLoad(NormalMode)
      }

      "in check mode" in {
        val call = navigator.exportedCreditsWeight(CheckMode)
        call mustBe creditsRoutes.ConvertedCreditsController.onPageLoad(CheckMode)
      }
    }
  }

  "convertedCreditsYesNo" must {
    
    "redirect to weight page when user answers 'yes'" when {
      "normal mode" in {
        val call = navigator.convertedCreditsYesNo(NormalMode, true)
        call mustBe creditsRoutes.ConvertedCreditsWeightController.onPageLoad(NormalMode)
      }
      "check mode" in {
        val call = navigator.convertedCreditsYesNo(CheckMode, true)
        call mustBe creditsRoutes.ConvertedCreditsWeightController.onPageLoad(CheckMode)
      }
    }
    
    "skip weight page when user answers 'no'" when {
      "normal mode" in {
        val call = navigator.convertedCreditsYesNo(NormalMode, false)
        call mustBe creditsRoutes.ConfirmPackagingCreditController.onPageLoad(NormalMode)
      }
      "check mode" in {
        val call = navigator.convertedCreditsYesNo(CheckMode, false)
        call mustBe creditsRoutes.ConfirmPackagingCreditController.onPageLoad(CheckMode)
      }
    }
    
  }

  "confirmCreditRoute" must {

    "redirect to start return page in Normal Mode" in {
      val call = navigator.confirmCreditRoute(NormalMode)
      call mustBe controllers.returns.routes.NowStartYourReturnController.onPageLoad
    }

    "redirect to CYA page in CheckMode Mode" in {
      val call = navigator.confirmCreditRoute(CheckMode)
      call mustBe controllers.returns.routes.ReturnsCheckYourAnswersController.onPageLoad
    }
  }

  "manufacturedPlasticPackagingRoute" must {
    
    "redirect to manufacturedWeight page in normal mode" when {
      "answer is Yes" in {
        val call = navigator.manufacturedPlasticPackagingRoute(NormalMode, hasAnswerChanged = true, usersAnswer = true)
        call mustBe returnsRoutes.ManufacturedPlasticPackagingWeightController.onPageLoad(NormalMode)
      }
      "answer is Yes and not been changed" in {
        val call = navigator.manufacturedPlasticPackagingRoute(NormalMode, hasAnswerChanged = false, usersAnswer = true)
        call mustBe returnsRoutes.ManufacturedPlasticPackagingWeightController.onPageLoad(NormalMode)
      }
    }

    "redirect to to Imported yes/no page in normal mode" when {
      "answer is No" in {
        val call = navigator.manufacturedPlasticPackagingRoute(NormalMode, hasAnswerChanged = true, usersAnswer = false)
        call mustBe returnsRoutes.ImportedPlasticPackagingController.onPageLoad(NormalMode)
      }

      "answer is No and not been changed" in {
        val call = navigator.manufacturedPlasticPackagingRoute(NormalMode, hasAnswerChanged = false, usersAnswer = false)
        call mustBe returnsRoutes.ImportedPlasticPackagingController.onPageLoad(NormalMode)
      }
    }

    "redirect to manufacturedWeight for check mode" when {
      "answer is Yes and has been changed" in {
        val call = navigator.manufacturedPlasticPackagingRoute(CheckMode, hasAnswerChanged = true, usersAnswer = true)
        call mustBe returnsRoutes.ManufacturedPlasticPackagingWeightController.onPageLoad(CheckMode)
      }
    }

    "redirect to check your answers page for check mode" when {
      "answer is No and has been changed" in {
        val call = navigator.manufacturedPlasticPackagingRoute(CheckMode, hasAnswerChanged = true, usersAnswer = false)
        call mustBe returnsRoutes.ConfirmPlasticPackagingTotalController.onPageLoad
      }

      "answer is Yes and has not been changed" in {
        val call = navigator.manufacturedPlasticPackagingRoute(CheckMode, hasAnswerChanged = false, usersAnswer = true)
        call mustBe returnsRoutes.ConfirmPlasticPackagingTotalController.onPageLoad
      }

      "answer is No" in {
        val call = navigator.manufacturedPlasticPackagingRoute(CheckMode, hasAnswerChanged = false, usersAnswer = false)
        call mustBe returnsRoutes.ConfirmPlasticPackagingTotalController.onPageLoad
      }
    }
  }

  "ImportedPlasticPackagingRoute" must {
    "redirect to imported weight page in  normal mode" when {

      "answer is Yes and has changed" in {
        val call = navigator.importedPlasticPackagingRoute(NormalMode, hasAnswerChanged = true, usersAnswer = true)
        call mustBe returnsRoutes.ImportedPlasticPackagingWeightController.onPageLoad(NormalMode)
      }

      "answer is Yes and has not changed" in {
        val call = navigator.importedPlasticPackagingRoute(NormalMode, hasAnswerChanged = false, usersAnswer = true)
        call mustBe returnsRoutes.ImportedPlasticPackagingWeightController.onPageLoad(NormalMode)
      }
    }
    "redirect to imported weight page in  normal mode" when {

      "answer is No and has changed" in {
        val call = navigator.importedPlasticPackagingRoute(NormalMode, hasAnswerChanged = true, usersAnswer = false)
        call mustBe returnsRoutes.ConfirmPlasticPackagingTotalController.onPageLoad
      }


      "answer is No and has not changed" in {
        val call = navigator.importedPlasticPackagingRoute(NormalMode, hasAnswerChanged = false, usersAnswer = false)
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
      call mustBe returnsRoutes.ReturnsCheckYourAnswersController.onPageLoad
    }

    "only some plastic is exported in CheckMode" in {
      val call = navigator.exportedByAnotherBusinessWeightRoute(false, CheckMode)
      call mustBe returnsRoutes.ReturnsCheckYourAnswersController.onPageLoad
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
      call mustBe returnsRoutes.ReturnsCheckYourAnswersController.onPageLoad
    }

    "redirect to account page" in {
      when(nonExportedAmountHelper.totalPlasticAdditions(any)).thenReturn(None)
      val call = navigator.confirmTotalPlasticPackagingRoute(userAnswers)
      call mustBe controllers.routes.IndexController.onPageLoad
    }
  }

  "for check mode" when {

    "when answer is Yes and has been changed" in {
      val call = navigator.importedPlasticPackagingRoute(CheckMode, hasAnswerChanged = true, usersAnswer = true)
      call mustBe returnsRoutes.ImportedPlasticPackagingWeightController.onPageLoad(CheckMode)
    }

    "when answer is Yes and has not been changed" in {
      val call = navigator.importedPlasticPackagingRoute(CheckMode, hasAnswerChanged = false, usersAnswer = true)
      call mustBe returnsRoutes.ConfirmPlasticPackagingTotalController.onPageLoad
    }

    "when answer is No and has has been changed" in {
      val call = navigator.importedPlasticPackagingRoute(CheckMode, hasAnswerChanged = true, usersAnswer = false)
      call mustBe returnsRoutes.ConfirmPlasticPackagingTotalController.onPageLoad
    }

    "when answer is No and has has not been changed" in {
      val call = navigator.importedPlasticPackagingRoute(CheckMode, hasAnswerChanged = false, usersAnswer = false)
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
    navigator.manufacturedPlasticPackagingWeightRoute(userAnswers) mustBe 
      returnsRoutes.ConfirmPlasticPackagingTotalController.onPageLoad     
  }
}

