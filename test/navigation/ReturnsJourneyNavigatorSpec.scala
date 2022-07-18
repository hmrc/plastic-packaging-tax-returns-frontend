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
import models.{CheckMode, NormalMode}

class ReturnsJourneyNavigatorSpec extends SpecBase {

  private val returnsJourneyNavigator = new ReturnsJourneyNavigator
  val navigator = new Navigator(amends = new AmendsJourneyNavigator, returns = returnsJourneyNavigator)

  "Manufacturing yes / no page" - {
    "for normal mode" - {

      "navigate to ManufacturedPlasticPackagingWeightPage when answer is Yes" in {
        val call = returnsJourneyNavigator.manufacturedPlasticPackagingRoute(NormalMode, hasAnswerChanged = true, usersAnswer = true)
        call mustBe returnsRoutes.ManufacturedPlasticPackagingWeightController.onPageLoad(NormalMode)
      }

      "navigate to ImportedPlasticPackagingPage when answer is No" in {
        val call = returnsJourneyNavigator.manufacturedPlasticPackagingRoute(NormalMode, hasAnswerChanged = true, usersAnswer = false)
        call mustBe returnsRoutes.ImportedPlasticPackagingController.onPageLoad(NormalMode)
      }
    }

    "for check mode" - {

      "navigate to ManufacturedPlasticPackagingWeightPage when answer is Yes and has been changed" in {
        val call = returnsJourneyNavigator.manufacturedPlasticPackagingRoute(CheckMode, hasAnswerChanged = true, usersAnswer = true)
        call mustBe returnsRoutes.ManufacturedPlasticPackagingWeightController.onPageLoad(CheckMode)
      }

      "navigate to ConfirmPlasticPackagingTotal when answer is Yes and has not been changed" in {
        val call = returnsJourneyNavigator.manufacturedPlasticPackagingRoute(NormalMode, hasAnswerChanged = false, usersAnswer = true)
        call mustBe returnsRoutes.ConfirmPlasticPackagingTotalController.onPageLoad
      }

      "navigate to ConfirmPlasticPackagingTotal Page when answer is No" in {
        val call = returnsJourneyNavigator.manufacturedPlasticPackagingRoute(NormalMode, hasAnswerChanged = false, usersAnswer = false)
        call mustBe returnsRoutes.ConfirmPlasticPackagingTotalController.onPageLoad
      }
    }
  }
}
