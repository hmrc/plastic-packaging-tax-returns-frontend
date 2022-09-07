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

package navigation

import base.SpecBase
import controllers.returns.{routes => returnsRoutes}
import controllers.returns.credits.{routes => creditsRoutes}
import models.Mode.{CheckMode, NormalMode}
import org.scalatestplus.play.PlaySpec

class ReturnsJourneyNavigatorSpec extends PlaySpec {

  private val returnsJourneyNavigator = new ReturnsJourneyNavigator
  val navigator = new Navigator(returns = returnsJourneyNavigator)

  //todo add in credits navigation that is currently tested in controllers

  "ExportedCreditsRoute" must {
    "redirect to ConvertedCredits page" when {
      "in normalmode" in {
        val call = returnsJourneyNavigator.ExportedCreditsRoute(NormalMode)
        call mustBe creditsRoutes.ConvertedCreditsController.onPageLoad(NormalMode)
      }
      "in checkmode" in {
        val call = returnsJourneyNavigator.ExportedCreditsRoute(CheckMode)
        call mustBe returnsRoutes.ReturnsCheckYourAnswersController.onPageLoad()
      }
    }
  }

  "ConvertedCreditsRoute" ignore {
    "in normalmode" in {
      val call = returnsJourneyNavigator.ConvertedCreditsRoute(NormalMode)
      call mustBe creditsRoutes.ConfirmPackagingCreditController.onPageLoad
    }
    "in checkmode" in {
      val call = returnsJourneyNavigator.ConvertedCreditsRoute(CheckMode)
      call mustBe returnsRoutes.ReturnsCheckYourAnswersController.onPageLoad()
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
}

