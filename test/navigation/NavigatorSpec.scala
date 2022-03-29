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
import controllers.routes
import pages._
import models._

class NavigatorSpec extends SpecBase {

  val navigator = new Navigator


  "Navigator" - {

    "in Normal mode" - {
      "for the AmendAreYouSurePage" - {
        "navigate to AmendManufacturedPlasticPackagingController when answer is Yes" in {
          val answers = UserAnswers("id").set(AmendAreYouSurePage, true)

          navigator.nextPage(AmendAreYouSurePage,
            NormalMode,
            answers.get
          ) mustBe routes.AmendManufacturedPlasticPackagingController.onPageLoad(NormalMode)

        }
        "navigate to Homepage when answer is No" in {

          val answers = UserAnswers("id").set(AmendAreYouSurePage, false)

          navigator.nextPage(AmendAreYouSurePage, NormalMode, answers.get
          ) mustBe (routes.IndexController.onPageLoad)
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

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage,
          CheckMode,
          UserAnswers("id")
        ) mustBe routes.CheckYourAnswersController.onPageLoad

      }
    }
  }
}
