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

package pages.returns

import models.UserAnswers
import pages.behaviours.PageBehaviours

class ManufacturedPlasticPackagingPageSpec extends PageBehaviours {

  "ManufacturedPlasticPackagingPage" - {

    beRetrievable[Boolean](ManufacturedPlasticPackagingPage)

    beSettable[Boolean](ManufacturedPlasticPackagingPage)

    beRemovable[Boolean](ManufacturedPlasticPackagingPage)
  }

  "cleanUp" - {
    "must return a value of zero" - {
      "when answer is no" in {
        val answer         = UserAnswers("123").set(ManufacturedPlasticPackagingPage, false).get
        val expectedAnswer = answer.copy().set(ManufacturedPlasticPackagingWeightPage, 0L).get

        ManufacturedPlasticPackagingPage.cleanup(Some(false), answer).get mustBe expectedAnswer
      }
    }

    "must return the answer" - {
      "when is Yes" in {
        val answer = UserAnswers("123").set(ManufacturedPlasticPackagingPage, true).get

        ManufacturedPlasticPackagingPage.cleanup(Some(true), answer).get mustBe answer
      }
    }
  }
}
