/*
 * Copyright 2025 HM Revenue & Customs
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

import scala.util.Success

class AnotherBusinessExportedWeightPageSpec extends PageBehaviours {

  "AnotherBusinessExportWeightPage" - {

    beRetrievable[Long](AnotherBusinessExportedWeightPage)

    beSettable[Long](AnotherBusinessExportedWeightPage)

    beRemovable[Long](AnotherBusinessExportedWeightPage)

    "cleanUp" - {
      "should reset userAnswer" - {
        "when exported amount is equal totalPlastic" in {
          val userAnswer = createUserAnswer
            .set(DirectlyExportedWeightPage, 20L).get
            .set(AnotherBusinessExportedWeightPage, 50L).get

          AnotherBusinessExportedWeightPage.cleanup(Some(50L), userAnswer) mustBe
            Success(expectedResetUserAnswer(userAnswer))
        }

        "when exported amount is greater than  totalPlastic" in {
          val userAnswer = createUserAnswer.set(AnotherBusinessExportedWeightPage, 200L).get

          AnotherBusinessExportedWeightPage.cleanup(Some(200L), userAnswer) mustBe
            Success(expectedResetUserAnswer(userAnswer))
        }
      }

      "should return userAnswer" - {
        "when exported amount is less than total plastic" in {
          val userAnswer = createUserAnswer.set(AnotherBusinessExportedWeightPage, 20L).get

          AnotherBusinessExportedWeightPage.cleanup(Some(20L), userAnswer) mustBe Success(userAnswer)
        }

        "when exported amount is less than 1" in {
          val userAnswer = createUserAnswer.set(AnotherBusinessExportedWeightPage, 0L).get

          AnotherBusinessExportedWeightPage.cleanup(Some(0L), userAnswer) mustBe Success(userAnswer)
        }

        "when exported amount is not Defined" in {
          val userAnswer = createUserAnswer.remove(AnotherBusinessExportedWeightPage).get

          AnotherBusinessExportedWeightPage.cleanup(None, userAnswer) mustBe Success(userAnswer)
        }
      }
    }
  }

  private def expectedResetUserAnswer(userAnswer: UserAnswers) = {
    userAnswer.set(DirectlyExportedPage, true, cleanup = false).get
      .set(NonExportedHumanMedicinesPlasticPackagingPage, false, cleanup = false).get
      .set(NonExportedHumanMedicinesPlasticPackagingWeightPage, 0L, cleanup = false).get
      .set(NonExportedRecycledPlasticPackagingPage, false, cleanup = false).get
      .set(NonExportedRecycledPlasticPackagingWeightPage, 0L, cleanup = false).get
      .set(AnotherBusinessExportedPage, true, false).get
  }

  private def createUserAnswer = UserAnswers("reg-number")
    .set(ManufacturedPlasticPackagingPage, false).get
    .set(ManufacturedPlasticPackagingWeightPage, 20L).get
    .set(ImportedPlasticPackagingPage, false).get
    .set(ImportedPlasticPackagingWeightPage, 50L).get
    .set(DirectlyExportedPage, true).get
    .set(DirectlyExportedWeightPage, 0L).get
    .set(AnotherBusinessExportedPage, true).get
    .set(AnotherBusinessExportedWeightPage, 0L).get
}
