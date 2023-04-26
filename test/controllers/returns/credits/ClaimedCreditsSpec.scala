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
import pages.returns.credits.{OldConvertedCreditsPage, OldExportedCreditsPage}

class ClaimedCreditsSpec extends PlaySpec {


  "Check" must {

    "be true" when {

      "user claims exportedCredit but not convertedCredit" in {
        ClaimedCredits(createUserAnswerWithNoConvertedCredits).hasMadeClaim mustBe true
      }

      "user claims convertedCredit but not exportedCredit" in {
        ClaimedCredits(createUserAnswerWithNoExportedCredits).hasMadeClaim mustBe true
      }

      "user claims convertedCredit and exportedCredit" in {
        ClaimedCredits(createUserAnswerWithCredits).hasMadeClaim mustBe true
      }
    }

    "be false" when {
      "user claims no convertedCredit or exportedCredit" in {
        ClaimedCredits(createUserAnswerWithNoExportedOrConvertedCredits).hasMadeClaim mustBe false
      }
    }
  }

  private def createUserAnswerWithCredits: UserAnswers = {
    UserAnswers("ppt321")
      .set(OldExportedCreditsPage, CreditsAnswer.answerWeightWith(20L)).get
      .set(OldConvertedCreditsPage, CreditsAnswer.answerWeightWith(30L)).get
  }

  private def createUserAnswerWithNoExportedOrConvertedCredits: UserAnswers = {
    UserAnswers("ppt321")
      .set(OldExportedCreditsPage, CreditsAnswer.noClaim).get
      .set(OldConvertedCreditsPage, CreditsAnswer.noClaim).get
  }

  private def createUserAnswerWithNoConvertedCredits: UserAnswers = {
    UserAnswers("ppt321")
      .set(OldExportedCreditsPage, CreditsAnswer.answerWeightWith(20L)).get
      .set(OldConvertedCreditsPage, CreditsAnswer.noClaim).get
  }

  private def createUserAnswerWithNoExportedCredits: UserAnswers = {
    UserAnswers("ppt321")
      .set(OldExportedCreditsPage, CreditsAnswer.noClaim).get
      .set(OldConvertedCreditsPage, CreditsAnswer.answerWeightWith(30L)).get
  }
}
