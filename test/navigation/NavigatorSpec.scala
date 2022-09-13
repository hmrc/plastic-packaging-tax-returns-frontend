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
import base.utils.NonExportedPlasticTestHelper
import config.FrontendAppConfig
import controllers.returns.{routes => returnsRoutes}
import models.Mode.{CheckMode, NormalMode}
import models._
import org.mockito.MockitoSugar.mock
import pages.returns._

/** ***********************************************************
 * Returns journey (v1)
 * *************************************************************
 * start-date
 * Yes: continue to manufactured-components (y/n)
 * Yes: continue to manufactured-weight
 * No: imported-components (y/n)
 * Yes: imported-weight
 * No: human-medicines-packaging-weight
 * No: account
 * human-medicines-packaging-weight
 * exported-plastic-packaging-weight
 * recycled-plastic-packaging-weight
 * how-much-credit
 * check-your-return
 * *********************************************************** */
//todo duplicated tests? check if can be removed
class NavigatorSpec extends SpecBase {

  private val frontendConfig = mock[FrontendAppConfig]
  val navigator = new Navigator(returns = new ReturnsJourneyNavigator(frontendConfig))

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

        "for the DirectlyExportedPlasticPackagingPage" - {

          "navigate to ExportedPlasticPackagingWeightPage when yes" in {
            val answers = UserAnswers("id").set(DirectlyExportedComponentsPage, true)

            navigator.nextPage(DirectlyExportedComponentsPage,
              NormalMode,
              answers.get
            ) mustBe returnsRoutes.ExportedPlasticPackagingWeightController.onPageLoad(NormalMode)


          }

          "navigate to NonExportedHumanMedicinesPlasticPackagingController when no" in {
            val answers = UserAnswers("id").set(DirectlyExportedComponentsPage, false)

            navigator.nextPage(DirectlyExportedComponentsPage,
              NormalMode,
              answers.get
            ) mustBe returnsRoutes.NonExportedHumanMedicinesPlasticPackagingController.onPageLoad(NormalMode)
          }
        }

        "for the ExportedPlasticPackagingWeightPage" - {

          "navigate to NonExportedHumanMedicinesPlasticPackagingController" - {
            "when exported amount is less then the total plastic package" in {
              val answers = NonExportedPlasticTestHelper.createUserAnswer(
                exportedAmount = 1000L,
                manufacturedAmount = 1000L,
                importedAmount = 50L)

              navigator.nextPage(ExportedPlasticPackagingWeightPage,
                NormalMode,
                answers
              ) mustBe returnsRoutes.NonExportedHumanMedicinesPlasticPackagingController.onPageLoad(NormalMode)

            }
          }

          "navigate to the check your answer page when" - {
            "exported amount greater the the total plastic package" in {
              val answers = UserAnswers("id").set(ExportedPlasticPackagingWeightPage, 1000L)

              navigator.nextPage(ExportedPlasticPackagingWeightPage,
                NormalMode,
                answers.get
              ) mustBe returnsRoutes.ReturnsCheckYourAnswersController.onPageLoad
            }
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
      }
    }
  }
}
