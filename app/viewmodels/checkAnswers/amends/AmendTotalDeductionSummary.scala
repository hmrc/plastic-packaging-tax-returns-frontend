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

package viewmodels.checkAnswers.amends

import models.UserAnswers
import models.amends.{AmendNewAnswerType, AmendSummaryRow}
import models.returns.AmendsCalculations
import pages.amends.{AmendDirectExportPlasticPackagingPage, AmendExportedByAnotherBusinessPage, AmendHumanMedicinePlasticPackagingPage, AmendRecycledPlasticPackagingPage}
import viewmodels.PrintLong

object AmendTotalDeductionSummary {

  def apply(calculations: AmendsCalculations, userAnswer: UserAnswers): AmendSummaryRow = {

    val answer = AmendNewAnswerType(
      calculations.amend.deductionsTotal.asKg,
      "AmendsCheckYourAnswers.hiddenCell.newAnswer.2",
      isAmending(userAnswer)
    )

    AmendSummaryRow(
      "AmendsCheckYourAnswers.deductionsTotal",
      calculations.original.deductionsTotal.asKg,
      answer,
      None
    )
  }

  private def isAmending(userAnswer: UserAnswers) =
    userAnswer.get(AmendDirectExportPlasticPackagingPage).isDefined ||
      userAnswer.get(AmendExportedByAnotherBusinessPage).isDefined ||
      userAnswer.get(AmendHumanMedicinePlasticPackagingPage).isDefined ||
      userAnswer.get(AmendRecycledPlasticPackagingPage).isDefined
}
