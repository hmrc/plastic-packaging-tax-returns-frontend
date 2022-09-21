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

package models.returns

import models.returns.CreditsClaimedDetails._
import models.{CreditBalance, UserAnswers}
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import pages.returns.credits.{ConvertedCreditsPage, ExportedCreditsPage, WhatDoYouWantToDoPage}
import viewmodels.PrintLong

class ClaimedCreditsDetailsSpec extends PlaySpec {

  val userAnswer = UserAnswers("123")
    .set(ExportedCreditsPage, CreditsAnswer(true, Some(100L))).get
    .set(ConvertedCreditsPage, CreditsAnswer(true, Some(200L))).get
    .set(WhatDoYouWantToDoPage, true).get

  "isClaimingCredit" should {

    "be true" when {
      "there is something to claim" in {
        val credits = CreditsClaimedDetails(userAnswer, CreditBalance(10, 4, 200, true))

        credits.isClaimingTaxBack mustBe true
      }
      "be false" when {
        "nothing is claimed" in {
          val credits = CreditsClaimedDetails(userAnswer.set(WhatDoYouWantToDoPage, false).get, CreditBalance(20, 0, 0, true))

          credits.isClaimingTaxBack mustBe false
        }
      }
    }
  }

  "summaryList" should {
    val table = Table(
      ("description", "exported", "converted", "exportedWeight", "convertedWeight"),
      ("populate both weights when both answers are yes", true, true, Some(100L), Some(200L)),
      ("remove converted weight when converted is no", true, false, Some(100L), None),
      ("remove exported weight when exported is no", false, true, None, Some(200L)),
      ("remove both exported and converted weight", false, false, None, None)
    )

    forAll(table) {
      (description, exported, converted, exportedWeight, convertedWeight) =>
        s"$description" in {
          val newAns = userAnswer
            .set(ExportedCreditsPage, CreditsAnswer(exported, exportedWeight)).get
            .set(ConvertedCreditsPage, CreditsAnswer(converted, convertedWeight)).get

          val credits = CreditsClaimedDetails(newAns, CreditBalance(10, 4, 200, true))

          credits.summaryList mustBe Seq(
            CreditExportedAnswerPartialKey -> (if (exported) "site.yes" else "site.no"),
            exportedWeight.fold("N/A" -> "N/A")(o => CreditExportedWeightPartialKey -> o.asKg),
            CreditConvertedAnswerPartialKey -> (if (converted) "site.yes" else "site.no"),
            convertedWeight.fold("N/A" -> "N/A")(o => CreditConvertedWeightPartialKey -> o.asKg),
            CreditsTotalWeightPartialKey -> "200kg",
            CreditTotalPartialKey        -> "£4.00"
          ).filter(!_._1.equals("N/A"))
        }
    }
  }

}