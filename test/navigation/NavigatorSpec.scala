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
import controllers.routes
import pages._
import models._

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

  val navigator = new Navigator

  "Navigator" - {

    "in Normal mode" - {

      "for the returns journey" - {

        "for the StartYourReturnPage" - {

          "navigate to ManufacturedPlasticPackagingController when answer is Yes" in {

            val answers = UserAnswers("id").set(StartYourReturnPage, true)

            navigator.nextPage(StartYourReturnPage,
              NormalMode,
              answers.get
            ) mustBe routes.ManufacturedPlasticPackagingController.onPageLoad(NormalMode)

          }

          "navigate to Homepage when answer is No" in {

            val answers = UserAnswers("id").set(StartYourReturnPage, false)

            navigator.nextPage(StartYourReturnPage,
              NormalMode,
              answers.get
            ) mustBe routes.IndexController.onPageLoad

          }
        }

        "for the ManufacturedPlasticPackagingPage" - {

          "navigate to ManufacturedPlasticPackagingWeightPage when answer is Yes" in {
            val answers = UserAnswers("id").set(ManufacturedPlasticPackagingPage, true)

            navigator.nextPage(ManufacturedPlasticPackagingPage,
              NormalMode,
              answers.get
            ) mustBe routes.ManufacturedPlasticPackagingWeightController.onPageLoad(NormalMode)

          }

          "navigate to ImportedPlasticPackagingPage when answer is No" in {

            val answers = UserAnswers("id").set(ManufacturedPlasticPackagingPage, false)

            navigator.nextPage(ManufacturedPlasticPackagingPage,
              NormalMode,
              answers.get
            ) mustBe routes.ImportedPlasticPackagingController.onPageLoad(NormalMode)

          }
        }

        "for the ImportedPlasticPackagingPage" - {

          "navigate to ImportedPlasticPackagingWeightPage when answer is Yes" in {
            val answers = UserAnswers("id").set(ImportedPlasticPackagingPage, true)

            navigator.nextPage(ImportedPlasticPackagingPage,
              NormalMode,
              answers.get
            ) mustBe routes.ImportedPlasticPackagingWeightController.onPageLoad(NormalMode)

          }

          "navigate to HumanMedicinesPlasticPackagingWeightPage when answer is No" in {

            val answers = UserAnswers("id").set(ImportedPlasticPackagingPage, false)

            navigator.nextPage(ImportedPlasticPackagingPage,
              NormalMode,
              answers.get
            ) mustBe routes.HumanMedicinesPlasticPackagingWeightController.onPageLoad(NormalMode)

          }
        }

        "for the ManufacturedPlasticPackagingWeightPage" - {

          "navigate to ImportedPlasticPackagingPage" in {
            val answers = UserAnswers("id").set(ManufacturedPlasticPackagingWeightPage, 1000L)

            navigator.nextPage(ManufacturedPlasticPackagingWeightPage,
              NormalMode,
              answers.get
            ) mustBe routes.ImportedPlasticPackagingController.onPageLoad(NormalMode)

          }

        }

        "for the ImportedPlasticPackagingWeightPage" - {

          "navigate to HumanMedicinesPlasticPackagingWeightPage" in {
            val answers = UserAnswers("id").set(ImportedPlasticPackagingWeightPage, 1000L)

            navigator.nextPage(ImportedPlasticPackagingWeightPage,
              NormalMode,
              answers.get
            ) mustBe routes.HumanMedicinesPlasticPackagingWeightController.onPageLoad(NormalMode)

          }

        }

        "for the HumanMedicinesPlasticPackagingWeightPage" - {

          "navigate to ExportedPlasticPackagingWeightPage" in {
            val answers = UserAnswers("id").set(HumanMedicinesPlasticPackagingWeightPage, 1000)

            navigator.nextPage(HumanMedicinesPlasticPackagingWeightPage,
              NormalMode,
              answers.get
            ) mustBe routes.ExportedPlasticPackagingWeightController.onPageLoad(NormalMode)

          }

        }

        "for the ExportedPlasticPackagingWeightPage" - {

          "navigate to RecycledPlasticPackagingWeightPage" in {
            val answers = UserAnswers("id").set(ExportedPlasticPackagingWeightPage, 1000)

            navigator.nextPage(ExportedPlasticPackagingWeightPage,
              NormalMode,
              answers.get
            ) mustBe routes.RecycledPlasticPackagingWeightController.onPageLoad(NormalMode)

          }

        }

        "for the RecycledPlasticPackagingWeightPage" - {

          "navigate to ConvertedPackagingCreditPage" in {
            val answers = UserAnswers("id").set(RecycledPlasticPackagingWeightPage, 1000)

            navigator.nextPage(RecycledPlasticPackagingWeightPage,
              NormalMode,
              answers.get
            ) mustBe routes.ConvertedPackagingCreditController.onPageLoad(NormalMode)

          }

        }

        "for the ConvertedPackagingCreditPage" - {

          "navigate to ReturnsCheckYourAnswers" in {
            val answers = UserAnswers("id").set[BigDecimal](ConvertedPackagingCreditPage, 1000)

            navigator.nextPage(ConvertedPackagingCreditPage,
              NormalMode,
              answers.get
            ) mustBe routes.ReturnsCheckYourAnswersController.onPageLoad

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
            ) mustBe routes.AmendManufacturedPlasticPackagingController.onPageLoad(NormalMode)

          }

          "navigate to Homepage when answer is No" in {

            val answers = UserAnswers("id").set(AmendAreYouSurePage, false).get.set(AmendSelectedPeriodKey, "TEST")

            navigator.nextPage(AmendAreYouSurePage,
              NormalMode,
              answers.get
            ) mustBe (routes.ViewReturnSummaryController.onPageLoad("TEST"))
          }

        }

        "must go from AmendManufacturedPlasticPackagingPage to AmendImportedPlasticPackagingController" in {

          navigator.nextPage(AmendManufacturedPlasticPackagingPage,
            NormalMode,
            UserAnswers("id")
          ) mustBe routes.AmendImportedPlasticPackagingController.onPageLoad(NormalMode)

        }

        "must go from AmendImportedPlasticPackagingPage to AmendHumanMedicinePlasticPackagingController" in {

          navigator.nextPage(AmendImportedPlasticPackagingPage,
            NormalMode,
            UserAnswers("id")
          ) mustBe routes.AmendHumanMedicinePlasticPackagingController.onPageLoad(NormalMode)

        }

        "must go from AmendHumanMedicinePlasticPackagingPage to AmendDirectExportPlasticPackagingController" in {

          navigator.nextPage(AmendHumanMedicinePlasticPackagingPage,
            NormalMode,
            UserAnswers("id")
          ) mustBe routes.AmendDirectExportPlasticPackagingController.onPageLoad(NormalMode)

        }

        "must go from AmendDirectExportPlasticPackagingPage to AmendRecycledPlasticPackagingController" in {

          navigator.nextPage(AmendDirectExportPlasticPackagingPage,
            NormalMode,
            UserAnswers("id")
          ) mustBe routes.AmendRecycledPlasticPackagingController.onPageLoad(NormalMode)

        }

        "must go from AmendRecycledPlasticPackagingPage to CheckYourAnswersController" in {

          navigator.nextPage(AmendRecycledPlasticPackagingPage,
            NormalMode,
            UserAnswers("id")
          ) mustBe routes.CheckYourAnswersController.onPageLoad

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
            ) mustBe routes.ManufacturedPlasticPackagingWeightController.onPageLoad(CheckMode)

          }

          "navigate to ImportedPlasticPackagingPage when answer is No" in {

            val answers = UserAnswers("id").set(ManufacturedPlasticPackagingPage, false)

            navigator.nextPage(ManufacturedPlasticPackagingPage,
              CheckMode,
              answers.get
            ) mustBe routes.ReturnsCheckYourAnswersController.onPageLoad

          }
        }

        "for the ImportedPlasticPackagingPage" - {

          "navigate to ImportedPlasticPackagingWeightPage when answer is Yes" in {
            val answers = UserAnswers("id").set(ImportedPlasticPackagingPage, true)

            navigator.nextPage(ImportedPlasticPackagingPage,
              CheckMode,
              answers.get
            ) mustBe routes.ImportedPlasticPackagingWeightController.onPageLoad(CheckMode)

          }

          "navigate to HumanMedicinesPlasticPackagingWeightPage when answer is No" in {

            val answers = UserAnswers("id").set(ImportedPlasticPackagingPage, false)

            navigator.nextPage(ImportedPlasticPackagingPage,
              CheckMode,
              answers.get
            ) mustBe routes.ReturnsCheckYourAnswersController.onPageLoad

          }
        }

        "for the ManufacturedPlasticPackagingWeightPage" - {

          "navigate to ImportedPlasticPackagingPage" in {
            val answers = UserAnswers("id").set(ManufacturedPlasticPackagingWeightPage, 1000L)

            navigator.nextPage(ManufacturedPlasticPackagingWeightPage,
              CheckMode,
              answers.get
            ) mustBe routes.ReturnsCheckYourAnswersController.onPageLoad

          }

        }

        "for the ImportedPlasticPackagingWeightPage" - {

          "navigate to HumanMedicinesPlasticPackagingWeightPage" in {
            val answers = UserAnswers("id").set(ImportedPlasticPackagingWeightPage, 1000L)

            navigator.nextPage(ImportedPlasticPackagingWeightPage,
              CheckMode,
              answers.get
            ) mustBe routes.ReturnsCheckYourAnswersController.onPageLoad

          }

        }
      }

      "for the amend journey" - {
        // TODO - implement me!!
      }
    }
  }
}
