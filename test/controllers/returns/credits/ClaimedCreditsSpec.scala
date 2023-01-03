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

package controllers.returns.credits

import models.UserAnswers
import models.returns.CreditsAnswer
import org.scalatestplus.play.PlaySpec
import pages.returns.credits.{ConvertedCreditsPage, ExportedCreditsPage}

class ClaimedCreditsSpec extends PlaySpec {


  "Check" must {

    "be true" when {

      "user claims exportedCredit but not convertedCredit" in {
        val ans = UserAnswers("ppt321")
          .set(ExportedCreditsPage, CreditsAnswer(true, Some(20))).get
          .set(ConvertedCreditsPage, CreditsAnswer(false, None)).get

        ClaimedCredits(ans).hasMadeClaim mustBe true
      }

      "user claims convertedCredit but not exportedCredit" in {
        val ans = UserAnswers("ppt321")
          .set(ExportedCreditsPage, CreditsAnswer(false, None)).get
          .set(ConvertedCreditsPage, CreditsAnswer(true, Some(30))).get

        ClaimedCredits(ans).hasMadeClaim mustBe true
      }

      "user claims convertedCredit and exportedCredit" in {
        val ans = UserAnswers("ppt321")
          .set(ExportedCreditsPage, CreditsAnswer(true, Some(30))).get
          .set(ConvertedCreditsPage, CreditsAnswer(true, Some(30))).get

        ClaimedCredits(ans).hasMadeClaim mustBe true

      }
    }

    "be false" when {

      "user claims no convertedCredit or exportedCredit" in {
        val ans = UserAnswers("ppt321")
          .set(ExportedCreditsPage, CreditsAnswer(false, None)).get
          .set(ConvertedCreditsPage, CreditsAnswer(false, None)).get

        ClaimedCredits(ans).hasMadeClaim mustBe false
      }
    }
  }
}
