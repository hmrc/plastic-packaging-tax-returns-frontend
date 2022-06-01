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

package pages.returns

import models.UserAnswers
import pages.behaviours.PageBehaviours

class ImportedPlasticPackagingPageSpec extends PageBehaviours {

  "ImportedPlasticPackagingPage" - {

    beRetrievable[Boolean](ImportedPlasticPackagingPage)

    beSettable[Boolean](ImportedPlasticPackagingPage)

    beRemovable[Boolean](ImportedPlasticPackagingPage)
  }

  "clean up" - {
    "must return the same answer" - {
      "when answer was Yes" in {
        val answer = UserAnswers("123").set(ImportedPlasticPackagingPage, true).get

        ImportedPlasticPackagingPage.cleanup(Some(true), answer).get mustBe answer
      }
    }

    "must return a value of zero" - {
      "when answer is no" in {
        val answer = UserAnswers("123").set(ImportedPlasticPackagingPage, false).get
        val expectedAnswer = answer.copy().set(ImportedPlasticPackagingWeightPage, 0L).get

        ImportedPlasticPackagingPage.cleanup(Some(false), answer).get mustBe expectedAnswer
      }
    }
  }
}
