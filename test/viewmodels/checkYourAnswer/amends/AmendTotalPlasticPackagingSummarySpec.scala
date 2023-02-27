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

package viewmodels.checkYourAnswer.amends

import models.amends.{AmendSummaryRow, AnswerWithValue, AnswerWithoutValue}
import models.returns.{AmendsCalculations, Calculations}
import org.scalatestplus.play.PlaySpec
import viewmodels.checkAnswers.amends.AmendTotalPlasticPackagingSummary

class AmendTotalPlasticPackagingSummarySpec extends PlaySpec {

  private val calculations = AmendsCalculations(
    Calculations(1,2, 200, 100, true, 0.2),
    Calculations(1,2,100, 100, true, 0.2)
  )

  "AmendTotalPlasticPackagingSummarySpec" should {
    "return a summary row with amended value" in {

      AmendTotalPlasticPackagingSummary(calculations, true) mustEqual
        AmendSummaryRow(
          "AmendsCheckYourAnswers.packagingTotal",
          "100kg",
          AnswerWithValue(Some("100kg")),
          None
        )
    }

    "return a summary row without amended value and a hidden message" in {

      AmendTotalPlasticPackagingSummary(calculations, false) mustEqual
        AmendSummaryRow(
          "AmendsCheckYourAnswers.packagingTotal",
          "100kg",
          AnswerWithoutValue("AmendsCheckYourAnswers.hiddenCell.newAnswer.2"),
          None
        )
    }
  }
}
