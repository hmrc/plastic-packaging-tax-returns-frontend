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
import cacheables.AmendSelectedPeriodKey
import controllers.returns.{routes => returnsRoutes}
import controllers.amends.{routes => amendsRoutes}
import pages._
import models._
import pages.amends.{AmendAreYouSurePage, AmendDirectExportPlasticPackagingPage, AmendHumanMedicinePlasticPackagingPage, AmendImportedPlasticPackagingPage, AmendManufacturedPlasticPackagingPage, AmendRecycledPlasticPackagingPage}
import pages.returns._
/*************************************************************
Returns journey (v1)
  **************************************************************
start-date
  Yes: continue to manufactured-components (y/n)
    Yes: continue to manufactured-weight
    No: imported-components (y/n)
      Yes: imported-weight
      No: human-medicines-packaging-weight
  No: account
  human-medicines-packaging-weight
  exported-plastic-packaging-weight
  recycled-plastic-packaging-weight
  how-much-credit
  check-your-return
  *************************************************************/

class NavigatorSpec extends SpecBase {

  val navigator = new Navigator(amends = new AmendsJourneyNavigator, returns = new ReturnsJourneyNavigator)

  "Navigator" - {

    "in Normal mode" - {

      "for the returns journey" - {

        "for the StartYourReturnPage" - {

          "navigate to ManufacturedPlasticPackagingController when answer is Yes" in {

            val answers = UserAnswers("id").set(StartYourReturnPage, true)

            navigator.nextPage(StartYourReturnPage,
              NormalMode,
              answers.get
            ) mustBe returnsRoutes.ManufacturedPlasticPackagingController.onPageLoad(NormalMode)

          }

          "navigate to Homepage when answer is No" in {

            val answers = UserAnswers("id").set(StartYourReturnPage, false)

            navigator.nextPage(StartYourReturnPage,
              NormalMode,
              answers.get
            ) mustBe returnsRoutes.NotStartOtherReturnsController.onPageLoad

          }
        }

        "for the ManufacturedPlasticPackagingPage" - {

          "navigate to ManufacturedPlasticPackagingWeightPage when answer is Yes" in {
            val answers = UserAnswers("id").set(ManufacturedPlasticPackagingPage, true)

            navigator.nextPage(ManufacturedPlasticPackagingPage,
              NormalMode,
              answers.get
            ) mustBe returnsRoutes.ManufacturedPlasticPackagingWeightController.onPageLoad(NormalMode)

          }

          "navigate to ImportedPlasticPackagingPage when answer is No" in {

            val answers = UserAnswers("id").set(ManufacturedPlasticPackagingPage, false)

            navigator.nextPage(ManufacturedPlasticPackagingPage,
              NormalMode,
              answers.get
            ) mustBe returnsRoutes.ImportedPlasticPackagingController.onPageLoad(NormalMode)

          }
        }

        "for the ImportedPlasticPackagingPage" - {

          "navigate to ImportedPlasticPackagingWeightPage when answer is Yes" in {
            val answers = UserAnswers("id").set(ImportedPlasticPackagingPage, true)

            navigator.nextPage(ImportedPlasticPackagingPage,
              NormalMode,
              answers.get
            ) mustBe returnsRoutes.ImportedPlasticPackagingWeightController.onPageLoad(NormalMode)

          }

          "navigate to ConfirmPlasticPackagingTotal Page when answer is No" in {

            val answers = UserAnswers("id").set(ImportedPlasticPackagingPage, false)

            navigator.nextPage(ImportedPlasticPackagingPage,
              NormalMode,
              answers.get
            ) mustBe returnsRoutes.ConfirmPlasticPackagingTotalController.onPageLoad

          }
        }

        "for the ManufacturedPlasticPackagingWeightPage" - {

          "navigate to ImportedPlasticPackagingPage" in {
            val answers = UserAnswers("id").set(ManufacturedPlasticPackagingWeightPage, 1000L)

            navigator.nextPage(ManufacturedPlasticPackagingWeightPage,
              NormalMode,
              answers.get
            ) mustBe returnsRoutes.ImportedPlasticPackagingController.onPageLoad(NormalMode)

          }

        }

        "for the ExportedPlasticPackagingWeightPage" - {

          "navigate to HumanMedicinesPlasticPackagingController" in {
            val answers = UserAnswers("id").set(ExportedPlasticPackagingWeightPage, 1000L)

            navigator.nextPage(ExportedPlasticPackagingWeightPage,
              NormalMode,
              answers.get
            ) mustBe returnsRoutes.ExportedHumanMedicinesPlasticPackagingController.onPageLoad(NormalMode)

          }

        }

        "for the ExportedHumanMedicinesPlasticPackagingPage" - {
          "navigate to HumanMedicinesPlasticPackagingWeightPage when answer is Yes" in {
            val answers = UserAnswers("id").set(ExportedHumanMedicinesPlasticPackagingPage, true)

            navigator.nextPage(ExportedHumanMedicinesPlasticPackagingPage,
              NormalMode,
              answers.get
            ) mustBe returnsRoutes.ExportedHumanMedicinesPlasticPackagingWeightController.onPageLoad(NormalMode)
          }

          "navigate to ExportedRecycledPlasticPackagingPage when answer is No" in {

            val answers = UserAnswers("id").set(ExportedHumanMedicinesPlasticPackagingPage, false)

            navigator.nextPage(ExportedHumanMedicinesPlasticPackagingPage,
              NormalMode,
              answers.get
            ) mustBe returnsRoutes.ExportedRecycledPlasticPackagingController.onPageLoad(NormalMode)

          }

        }

        "for the ExportedHumanMedicinesPlasticPackagingWeightPage" - {

          "navigate to ExportedRecycledPlasticPackagingPage" in {
            val answers = UserAnswers("id").set(ExportedHumanMedicinesPlasticPackagingWeightPage, 1000L)

            navigator.nextPage(ExportedHumanMedicinesPlasticPackagingWeightPage,
              NormalMode,
              answers.get
            ) mustBe returnsRoutes.ExportedRecycledPlasticPackagingController.onPageLoad(NormalMode)

          }

        }


        "for the RecycledPlasticPackagingWeightPage" - {

          "navigate to ConvertedPackagingCreditPage" in {
            val answers = UserAnswers("id").set(NonExportedRecycledPlasticPackagingWeightPage, 1000L)

            navigator.nextPage(NonExportedRecycledPlasticPackagingWeightPage,
              NormalMode,
              answers.get
            ) mustBe returnsRoutes.ConvertedPackagingCreditController.onPageLoad(NormalMode)

          }

        }

        "for the ConvertedPackagingCreditPage" - {

          "navigate to ReturnsCheckYourAnswers" in {
            val answers = UserAnswers("id").set[BigDecimal](ConvertedPackagingCreditPage, 1000)

            navigator.nextPage(ConvertedPackagingCreditPage,
              NormalMode,
              answers.get
            ) mustBe returnsRoutes.ReturnsCheckYourAnswersController.onPageLoad

          }

        }
      }

      "for the amend journey" - {

        "for the AmendAreYouSurePage" - {

          "navigate to AmendManufacturedPlasticPackagingController when answer is Yes" in {
            val answers = UserAnswers("id").set(AmendAreYouSurePage, true)

            navigator.nextPage(AmendAreYouSurePage,
              NormalMode,
              answers.get
            ) mustBe amendsRoutes.AmendManufacturedPlasticPackagingController.onPageLoad(NormalMode)

          }

          "navigate to Homepage when answer is No" in {

            val answers = UserAnswers("id").set(AmendAreYouSurePage, false).get.set(AmendSelectedPeriodKey, "TEST")

            navigator.nextPage(AmendAreYouSurePage,
              NormalMode,
              answers.get
            ) mustBe (amendsRoutes.ViewReturnSummaryController.onPageLoad("TEST"))
          }

        }

        "must go from AmendManufacturedPlasticPackagingPage to AmendImportedPlasticPackagingController" in {

          navigator.nextPage(AmendManufacturedPlasticPackagingPage,
            NormalMode,
            UserAnswers("id")
          ) mustBe amendsRoutes.AmendImportedPlasticPackagingController.onPageLoad(NormalMode)

        }

        "must go from AmendImportedPlasticPackagingPage to AmendHumanMedicinePlasticPackagingController" in {

          navigator.nextPage(AmendImportedPlasticPackagingPage,
            NormalMode,
            UserAnswers("id")
          ) mustBe amendsRoutes.AmendHumanMedicinePlasticPackagingController.onPageLoad(NormalMode)

        }

        "must go from AmendHumanMedicinePlasticPackagingPage to AmendDirectExportPlasticPackagingController" in {

          navigator.nextPage(AmendHumanMedicinePlasticPackagingPage,
            NormalMode,
            UserAnswers("id")
          ) mustBe amendsRoutes.AmendDirectExportPlasticPackagingController.onPageLoad(NormalMode)

        }

        "must go from AmendDirectExportPlasticPackagingPage to AmendRecycledPlasticPackagingController" in {

          navigator.nextPage(AmendDirectExportPlasticPackagingPage,
            NormalMode,
            UserAnswers("id")
          ) mustBe amendsRoutes.AmendRecycledPlasticPackagingController.onPageLoad(NormalMode)

        }

        "must go from AmendRecycledPlasticPackagingPage to CheckYourAnswersController" in {

          navigator.nextPage(AmendRecycledPlasticPackagingPage,
            NormalMode,
            UserAnswers("id")
          ) mustBe amendsRoutes.CheckYourAnswersController.onPageLoad

        }
      }
    }

    "in Check mode" - {

      "for the returns journey" - {

        "for the ManufacturedPlasticPackagingPage" - {

          "navigate to ManufacturedPlasticPackagingWeightPage when answer is Yes" in {
            val answers = UserAnswers("id").set(ManufacturedPlasticPackagingPage, true)

            navigator.nextPage(ManufacturedPlasticPackagingPage,
              CheckMode,
              answers.get
            ) mustBe returnsRoutes.ManufacturedPlasticPackagingWeightController.onPageLoad(CheckMode)

          }

          "navigate to ConfirmPlasticPackagingTotal Page when answer is No" in {

            val answers = UserAnswers("id").set(ManufacturedPlasticPackagingPage, false)

            navigator.nextPage(ManufacturedPlasticPackagingPage,
              CheckMode,
              answers.get
            ) mustBe returnsRoutes.ConfirmPlasticPackagingTotalController.onPageLoad

          }
        }

        "for the ImportedPlasticPackagingPage" - {

          "navigate to ImportedPlasticPackagingWeightPage when answer is Yes" in {
            val answers = UserAnswers("id").set(ImportedPlasticPackagingPage, true)

            navigator.nextPage(ImportedPlasticPackagingPage,
              CheckMode,
              answers.get
            ) mustBe returnsRoutes.ImportedPlasticPackagingWeightController.onPageLoad(CheckMode)

          }

          "navigate to HumanMedicinesPlasticPackagingWeightPage when answer is No" in {

            val answers = UserAnswers("id").set(ImportedPlasticPackagingPage, false)

            navigator.nextPage(ImportedPlasticPackagingPage,
              CheckMode,
              answers.get
            ) mustBe returnsRoutes.ConfirmPlasticPackagingTotalController.onPageLoad

          }
        }

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

      "for the amend journey" - {
        // TODO - implement me!!
      }
    }
  }
}
