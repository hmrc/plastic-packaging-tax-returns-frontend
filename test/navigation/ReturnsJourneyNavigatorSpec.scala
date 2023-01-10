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
import controllers.returns.credits.{ClaimedCredits, routes => creditsRoutes}
import controllers.returns.{routes => returnsRoutes}
import models.Mode.{CheckMode, NormalMode}
import models.UserAnswers
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar.{mock, reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import pages.returns.{ImportedPlasticPackagingPage, ImportedPlasticPackagingWeightPage, ManufacturedPlasticPackagingPage, ManufacturedPlasticPackagingWeightPage}
import queries.Gettable

class ReturnsJourneyNavigatorSpec extends PlaySpec with BeforeAndAfterEach {

  private val frontendConfig = mock[FrontendAppConfig]
  private val mockClaimedCredits = mock[ClaimedCredits]
  private val userAnswers = mock[UserAnswers]

  private val returnsJourneyNavigator = new ReturnsJourneyNavigator(frontendConfig)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockClaimedCredits, frontendConfig)
  }

  //todo add in credits navigation that is currently tested in controllers
  
  "The start your return page" must {
    "goto the what do you want to do page" in {
      when(frontendConfig.isFeatureEnabled(any)) thenReturn true
      returnsJourneyNavigator.startYourReturnRoute(true, false) mustBe 
        creditsRoutes.WhatDoYouWantToDoController.onPageLoad(NormalMode)
    }
    "except when the credits feature is disabled" in {
      when(frontendConfig.isFeatureEnabled(any)) thenReturn false
      returnsJourneyNavigator.startYourReturnRoute(true, false) mustBe
        returnsRoutes.ManufacturedPlasticPackagingController.onPageLoad(NormalMode)
    }
    "except when it is the users first return" in {
      when(frontendConfig.isFeatureEnabled(any)) thenReturn true
      returnsJourneyNavigator.startYourReturnRoute(true, isFirstReturn = true) mustBe
        returnsRoutes.ManufacturedPlasticPackagingController.onPageLoad(NormalMode)
    }
    "go back to the account home page" in {
      when(frontendConfig.isFeatureEnabled(any)) thenReturn true
      returnsJourneyNavigator.startYourReturnRoute(doesUserWantToStartReturn = false, false) mustBe
        returnsRoutes.NotStartOtherReturnsController.onPageLoad()
    }
  }

  "ExportedCreditsRoute" must {
    "redirect to ConvertedCredits page" when {
      "in normalmode" in {
        val call = returnsJourneyNavigator.exportedCreditsRoute(NormalMode)
        call mustBe creditsRoutes.ConvertedCreditsController.onPageLoad(NormalMode)
      }
    }
    "redirect to checkYourAnswers page" when {
      "in checkmode" in {
        val call = returnsJourneyNavigator.exportedCreditsRoute(CheckMode)
        call mustBe creditsRoutes.ConvertedCreditsController.onPageLoad(CheckMode)
      }
    }
  }

  "ConvertedCreditsRoute" must {
    "redirect to NowStartYourReturn page" when {
      "the user does not claim any credits" in {
        when(mockClaimedCredits.hasMadeClaim).thenReturn(false)
        val call = returnsJourneyNavigator.convertedCreditsRoute(NormalMode, mockClaimedCredits)
        call mustBe returnsRoutes.NowStartYourReturnController.onPageLoad
      }
      "in checkmode" in {
        when(mockClaimedCredits.hasMadeClaim).thenReturn(false)
        val call = returnsJourneyNavigator.convertedCreditsRoute(CheckMode, mockClaimedCredits)
        call mustBe returnsRoutes.ReturnsCheckYourAnswersController.onPageLoad()
      }
    }
    "redirect to confirmCredit page" when {
      "the user claims credits" in {
        when(mockClaimedCredits.hasMadeClaim).thenReturn(true)
        val call = returnsJourneyNavigator.convertedCreditsRoute(NormalMode, mockClaimedCredits)
        call mustBe creditsRoutes.ConfirmPackagingCreditController.onPageLoad(NormalMode)
      }
      "in checkmode" in {
        when(mockClaimedCredits.hasMadeClaim).thenReturn(true)
        val call = returnsJourneyNavigator.convertedCreditsRoute(CheckMode, mockClaimedCredits)
        call mustBe creditsRoutes.ConfirmPackagingCreditController.onPageLoad(CheckMode)
      }
    }
  }

  "manufacturedPlasticPackagingRoute" must {
    "redirect to manufacturedWeight page in normalmode" when {

      "answer is Yes" in {
        val call = returnsJourneyNavigator.manufacturedPlasticPackagingRoute(NormalMode, hasAnswerChanged = true, usersAnswer = true)
        call mustBe returnsRoutes.ManufacturedPlasticPackagingWeightController.onPageLoad(NormalMode)
      }
      "answer is Yes and not been changed" in {
        val call = returnsJourneyNavigator.manufacturedPlasticPackagingRoute(NormalMode, hasAnswerChanged = false, usersAnswer = true)
        call mustBe returnsRoutes.ManufacturedPlasticPackagingWeightController.onPageLoad(NormalMode)
      }
    }
    "redirect to to Imported yes/no page in normalmode" when {
      "answer is No" in {
        val call = returnsJourneyNavigator.manufacturedPlasticPackagingRoute(NormalMode, hasAnswerChanged = true, usersAnswer = false)
        call mustBe returnsRoutes.ImportedPlasticPackagingController.onPageLoad(NormalMode)
      }


      "answer is No and not been changed" in {
        val call = returnsJourneyNavigator.manufacturedPlasticPackagingRoute(NormalMode, hasAnswerChanged = false, usersAnswer = false)
        call mustBe returnsRoutes.ImportedPlasticPackagingController.onPageLoad(NormalMode)
      }
    }

    "redirect to manufacturedWeight for check mode" when {

      "answer is Yes and has been changed" in {
        val call = returnsJourneyNavigator.manufacturedPlasticPackagingRoute(CheckMode, hasAnswerChanged = true, usersAnswer = true)
        call mustBe returnsRoutes.ManufacturedPlasticPackagingWeightController.onPageLoad(CheckMode)
      }
    }
    "redirect to check your answers page for check mode" when {
      "answer is No and has been changed" in {
        val call = returnsJourneyNavigator.manufacturedPlasticPackagingRoute(CheckMode, hasAnswerChanged = true, usersAnswer = false)
        call mustBe returnsRoutes.ConfirmPlasticPackagingTotalController.onPageLoad
      }

      "answer is Yes and has not been changed" in {
        val call = returnsJourneyNavigator.manufacturedPlasticPackagingRoute(CheckMode, hasAnswerChanged = false, usersAnswer = true)
        call mustBe returnsRoutes.ConfirmPlasticPackagingTotalController.onPageLoad
      }

      "answer is No" in {
        val call = returnsJourneyNavigator.manufacturedPlasticPackagingRoute(CheckMode, hasAnswerChanged = false, usersAnswer = false)
        call mustBe returnsRoutes.ConfirmPlasticPackagingTotalController.onPageLoad
      }
    }
  }

  "ImportedPlasticPackagingRoute" must {
    "redirect to imported weight page in  normal mode" when {

      "answer is Yes and has changed" in {
        val call = returnsJourneyNavigator.importedPlasticPackagingRoute(NormalMode, hasAnswerChanged = true, usersAnswer = true)
        call mustBe returnsRoutes.ImportedPlasticPackagingWeightController.onPageLoad(NormalMode)
      }

      "answer is Yes and has not changed" in {
        val call = returnsJourneyNavigator.importedPlasticPackagingRoute(NormalMode, hasAnswerChanged = false, usersAnswer = true)
        call mustBe returnsRoutes.ImportedPlasticPackagingWeightController.onPageLoad(NormalMode)
      }
    }
    "redirect to imported weight page in  normal mode" when {

      "answer is No and has changed" in {
        val call = returnsJourneyNavigator.importedPlasticPackagingRoute(NormalMode, hasAnswerChanged = true, usersAnswer = false)
        call mustBe returnsRoutes.ConfirmPlasticPackagingTotalController.onPageLoad
      }


      "answer is No and has not changed" in {
        val call = returnsJourneyNavigator.importedPlasticPackagingRoute(NormalMode, hasAnswerChanged = false, usersAnswer = false)
        call mustBe returnsRoutes.ConfirmPlasticPackagingTotalController.onPageLoad
      }
    }
  }

  "exportedPlasticPackagingWeightRoute" must {
    "redirect to ReturnsCheckYourAnswersController in NormalMode" in {
      val call = returnsJourneyNavigator.exportedPlasticPackagingWeightRoute(true, NormalMode)
      call mustBe returnsRoutes.ReturnsCheckYourAnswersController.onPageLoad()
    }

    "redirect to PlasticExportedByAnotherBusinessController in NormalMode" in {
      val call = returnsJourneyNavigator.exportedPlasticPackagingWeightRoute(false, NormalMode)
      call mustBe returnsRoutes.PlasticExportedByAnotherBusinessController.onPageLoad(NormalMode)
    }

    "redirect to PlasticExportedByAnotherBusinessController in CheckMode" in {
      val call = returnsJourneyNavigator.exportedPlasticPackagingWeightRoute(false, CheckMode)
      call mustBe returnsRoutes.PlasticExportedByAnotherBusinessController.onPageLoad(CheckMode)
    }
  }

  "exportedByAnotherBusinessWeightRoute" must {
    "redirect to ReturnsCheckYourAnswersController in NormalMode" in {
      val call = returnsJourneyNavigator.exportedByAnotherBusinessWeightRoute(true, NormalMode)
      call mustBe returnsRoutes.ReturnsCheckYourAnswersController.onPageLoad()
    }

    "redirect to NonExportedHumanMedicinesPlasticPackagingController in NormalMode" in {
      val call = returnsJourneyNavigator.exportedByAnotherBusinessWeightRoute(false, NormalMode)
      call mustBe returnsRoutes.NonExportedHumanMedicinesPlasticPackagingController.onPageLoad(NormalMode)
    }

    "redirect to PlasticExportedByAnotherBusinessController in CheckMode" in {
      val call = returnsJourneyNavigator.exportedByAnotherBusinessWeightRoute(false, CheckMode)
      call mustBe returnsRoutes.ReturnsCheckYourAnswersController.onPageLoad
    }
  }

  "ConfirmTotalPlasticPackagingRoute" must {

    "redirect to directly exported page page" in {
      val answer = UserAnswers("123")
          .set(ManufacturedPlasticPackagingPage, true).get
          .set(ManufacturedPlasticPackagingWeightPage, 10L).get
          .set(ImportedPlasticPackagingPage, true).get
          .set(ImportedPlasticPackagingWeightPage, 1L).get


      val call = returnsJourneyNavigator.confirmTotalPlasticPackagingRoute(answer)
      call mustBe returnsRoutes.DirectlyExportedComponentsController.onPageLoad(NormalMode)
    }

    "redirect to CYA page" in {
      val answer = UserAnswers("123")
        .set(ManufacturedPlasticPackagingPage, true).get
        .set(ManufacturedPlasticPackagingWeightPage, 0L).get
        .set(ImportedPlasticPackagingPage, true).get
        .set(ImportedPlasticPackagingWeightPage, 0L).get


      val call = returnsJourneyNavigator.confirmTotalPlasticPackagingRoute(answer)
      call mustBe returnsRoutes.ReturnsCheckYourAnswersController.onPageLoad
    }

    "redirect to account page" in {
      val answer = UserAnswers("123")
        .set(ImportedPlasticPackagingPage, true).get
        .set(ImportedPlasticPackagingWeightPage, 0L).get


      val call = returnsJourneyNavigator.confirmTotalPlasticPackagingRoute(answer)
      call mustBe controllers.routes.IndexController.onPageLoad
    }
  }

  "for check mode" when {

    "when answer is Yes and has been changed" in {
      val call = returnsJourneyNavigator.importedPlasticPackagingRoute(CheckMode, hasAnswerChanged = true, usersAnswer = true)
      call mustBe returnsRoutes.ImportedPlasticPackagingWeightController.onPageLoad(CheckMode)
    }

    "when answer is Yes and has not been changed" in {
      val call = returnsJourneyNavigator.importedPlasticPackagingRoute(CheckMode, hasAnswerChanged = false, usersAnswer = true)
      call mustBe returnsRoutes.ConfirmPlasticPackagingTotalController.onPageLoad
    }

    "when answer is No and has has been changed" in {
      val call = returnsJourneyNavigator.importedPlasticPackagingRoute(CheckMode, hasAnswerChanged = true, usersAnswer = false)
      call mustBe returnsRoutes.ConfirmPlasticPackagingTotalController.onPageLoad
    }

    "when answer is No and has has not been changed" in {
      val call = returnsJourneyNavigator.importedPlasticPackagingRoute(CheckMode, hasAnswerChanged = false, usersAnswer = false)
      call mustBe returnsRoutes.ConfirmPlasticPackagingTotalController.onPageLoad
    }

  }

  "directlyExportedComponentsRoute" when {
    
    "answer is yes in check mode" in {
      when(userAnswers.get(any[Gettable[Any]])(any)) thenReturn Some(true)
      returnsJourneyNavigator.directlyExportedComponentsRoute(userAnswers, CheckMode) mustBe
        returnsRoutes.ExportedPlasticPackagingWeightController.onPageLoad(CheckMode)
    }

    "answer is yes in normal mode" in {
      when(userAnswers.get(any[Gettable[Any]])(any)) thenReturn Some(true)
      returnsJourneyNavigator.directlyExportedComponentsRoute(userAnswers, NormalMode) mustBe
        returnsRoutes.ExportedPlasticPackagingWeightController.onPageLoad(NormalMode)
    }

    "answer is no in check mode" in {
      when(userAnswers.get(any[Gettable[Any]])(any)) thenReturn Some(false)
      returnsJourneyNavigator.directlyExportedComponentsRoute(userAnswers, CheckMode) mustBe
        returnsRoutes.PlasticExportedByAnotherBusinessController.onPageLoad(CheckMode)
    }

    "answer is no in normal mode" in {
      when(userAnswers.get(any[Gettable[Any]])(any)) thenReturn Some(false)
      returnsJourneyNavigator.directlyExportedComponentsRoute(userAnswers, NormalMode) mustBe
        returnsRoutes.PlasticExportedByAnotherBusinessController.onPageLoad(NormalMode)
    }
    
  }
}

