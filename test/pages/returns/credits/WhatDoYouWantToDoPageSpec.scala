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

package pages.returns.credits

import models.UserAnswers
import models.returns.CreditsAnswer
import pages.behaviours.PageBehaviours

import java.time.Instant
import scala.util.Success

class WhatDoYouWantToDoPageSpec extends PageBehaviours {

  val instant = Instant.now()
  val userAnswers = UserAnswers("123", lastUpdated = instant)
    .setOrFail(ConvertedCreditsPage, CreditsAnswer(yesNo = true, Some(10)))
    .setOrFail(ExportedCreditsPage, CreditsAnswer(yesNo = true, Some(10)))

    "clean up should" - {
      "do nothing when" - {
        "answer is empty" in {
          WhatDoYouWantToDoPage.cleanup(None, userAnswers) mustBe Success(userAnswers)

        }
        "answer is true" in {
          WhatDoYouWantToDoPage.cleanup(Some(true), userAnswers) mustBe Success(userAnswers)
        }
      }
      "set all credit answers to no claim when" - {
        "answer is false" in {
          val updatedUserAnswers = UserAnswers("123", lastUpdated = instant)
            .setOrFail(ConvertedCreditsPage, CreditsAnswer.noClaim)
            .setOrFail(ExportedCreditsPage, CreditsAnswer.noClaim)

          WhatDoYouWantToDoPage.cleanup(Some(false), userAnswers) mustBe Success(updatedUserAnswers)
        }
      }
    }

}

