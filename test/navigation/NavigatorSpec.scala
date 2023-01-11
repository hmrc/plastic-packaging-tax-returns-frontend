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

import base.SpecBase
import base.utils.NonExportedPlasticTestHelper
import config.FrontendAppConfig
import controllers.helpers.NonExportedAmountHelper
import controllers.returns.{routes => returnsRoutes}
import models.Mode.{CheckMode, NormalMode}
import models._
import org.mockito.MockitoSugar.mock
import org.scalatest.BeforeAndAfterEach
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import pages.returns._

//todo duplicated tests? check if can be removed
class NavigatorSpec extends AnyFreeSpec with Matchers {

  private val frontendConfig = mock[FrontendAppConfig]
  private val nonExportedAmountHelper = mock[NonExportedAmountHelper]
  val navigator = new Navigator(returns = new ReturnsJourneyNavigator(frontendConfig, nonExportedAmountHelper))

  "Navigator" - {

    "in Normal mode" - {

      "for the returns journey" - {

        "for the ManufacturedPlasticPackagingWeightPage" - {

          "navigate to ImportedPlasticPackagingPage" in {
            val answers = UserAnswers("id").set(ManufacturedPlasticPackagingWeightPage, 1000L)

            navigator.nextPage(ManufacturedPlasticPackagingWeightPage,
              NormalMode,
              answers.get
            ) mustBe returnsRoutes.ImportedPlasticPackagingController.onPageLoad(NormalMode)

          }

        }

        "for the PlasticExportedByAnotherBusinessPage" - {

          "navigate to AnotherBusinessExportWeightController when DirectlyExportedComponentsPage is no and PlasticExportedByAnotherBusinessPage is yes" in {
            val answers = UserAnswers("id").set(DirectlyExportedComponentsPage, false).get
              .set(PlasticExportedByAnotherBusinessPage, true).get

            navigator.nextPage(PlasticExportedByAnotherBusinessPage,
              NormalMode,
              answers
            ) mustBe returnsRoutes.AnotherBusinessExportWeightController.onPageLoad(NormalMode)
          }

          "navigate to AnotherBusinessExportWeightController when DirectlyExportedComponentsPage is yes and PlasticExportedByAnotherBusinessPage is yes" in {
            val answers = UserAnswers("id").set(DirectlyExportedComponentsPage, true).get
              .set(PlasticExportedByAnotherBusinessPage, true).get

            navigator.nextPage(PlasticExportedByAnotherBusinessPage,
              NormalMode,
              answers
            ) mustBe returnsRoutes.AnotherBusinessExportWeightController.onPageLoad(NormalMode)
          }

          "navigate to NonExportedHumanMedicinesPlasticPackagingController when DirectlyExportedComponentsPage is yes and PlasticExportedByAnotherBusinessPage is no" in {
            val answers = UserAnswers("id").set(DirectlyExportedComponentsPage, true).get
              .set(PlasticExportedByAnotherBusinessPage, false).get

            navigator.nextPage(PlasticExportedByAnotherBusinessPage,
              NormalMode,
              answers
            ) mustBe returnsRoutes.NonExportedHumanMedicinesPlasticPackagingController.onPageLoad(NormalMode)
          }

          "navigate to NonExportedHumanMedicinesPlasticPackagingController when DirectlyExportedComponentsPage is no and PlasticExportedByAnotherBusinessPage is no" in {
            val answers = UserAnswers("id").set(DirectlyExportedComponentsPage, false).get
                .set(PlasticExportedByAnotherBusinessPage, false).get

            navigator.nextPage(PlasticExportedByAnotherBusinessPage,
              NormalMode,
              answers
            ) mustBe returnsRoutes.NonExportedHumanMedicinesPlasticPackagingController.onPageLoad(NormalMode)
          }
        }

        "for the NonExportedRecycledPlasticPackagingWeightPage" - {

          "navigate to ReturnsCheckYourAnswers" in {
            val answers = UserAnswers("id").set(NonExportedRecycledPlasticPackagingWeightPage, 1000L)

            navigator.nextPage(NonExportedRecycledPlasticPackagingWeightPage,
              NormalMode,
              answers.get
            ) mustBe returnsRoutes.ReturnsCheckYourAnswersController.onPageLoad

          }

        }
      }

    }

    "in Check mode" - {

      "for the returns journey" - {

        "for the ManufacturedPlasticPackagingWeightPage" - {

          "navigate to ConfirmPlasticPackagingTotal Page" in {
            val answers = UserAnswers("id").set(ManufacturedPlasticPackagingWeightPage, 1000L)

            navigator.nextPage(ManufacturedPlasticPackagingWeightPage,
              CheckMode,
              answers.get
            ) mustBe returnsRoutes.ConfirmPlasticPackagingTotalController.onPageLoad

          }

        }

        "for the ImportedPlasticPackagingWeightPage" - {

          "navigate to ConfirmPlasticPackagingTotal Page" in {
            val answers = UserAnswers("id").set(ImportedPlasticPackagingWeightPage, 1000L)

            navigator.nextPage(ImportedPlasticPackagingWeightPage,
              CheckMode,
              answers.get
            ) mustBe returnsRoutes.ConfirmPlasticPackagingTotalController.onPageLoad

          }

        }

        "for the PlasticExportedByAnotherBusinessPage" - {

          "navigate to CYA when answer is No" in {
            val answers = UserAnswers("id").set(PlasticExportedByAnotherBusinessPage, false).get

            navigator.nextPage(PlasticExportedByAnotherBusinessPage,
              CheckMode,
              answers
            ) mustBe returnsRoutes.ReturnsCheckYourAnswersController.onPageLoad()
          }

          "navigate to weight page when answer is Yes" in {
            val answers = UserAnswers("id").set(PlasticExportedByAnotherBusinessPage, true).get

            navigator.nextPage(PlasticExportedByAnotherBusinessPage,
              CheckMode,
              answers
            ) mustBe returnsRoutes.AnotherBusinessExportWeightController.onPageLoad(CheckMode)
          }
        }

        "for the AnotherBusinessExportWeightPage" - {
          "navigate to CYA page" in {

            val answers = NonExportedPlasticTestHelper.createUserAnswer(
              exportedAmount = 1000L,
              exportedByAnotherBusinessAmount = 100L,
              manufacturedAmount = 1100L,
              importedAmount = 50L)

            navigator.nextPage(AnotherBusinessExportWeightPage,
              CheckMode,
              answers
            ) mustBe returnsRoutes.ReturnsCheckYourAnswersController.onPageLoad()
          }
        }
      }
    }
  }
}
