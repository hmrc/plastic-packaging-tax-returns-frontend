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

import models.returns.CreditsClaimedDetails.{CreditConvertedAnswerPartialKey, CreditConvertedWeightPartialKey, CreditExportedAnswerPartialKey, CreditExportedWeightPartialKey, CreditTotalPartialKey, CreditsTotalWeightPartialKey}
import models.{CreditBalance, UserAnswers}
import pages.returns.credits.{ConvertedCreditsPage, ExportedCreditsPage, WhatDoYouWantToDoPage}
import viewmodels.{PrintBigDecimal, PrintLong}

case class CreditsClaimedDetails(
                                  exported: CreditsAnswer,
                                  converted: CreditsAnswer,
                                  override val isClaimingTaxBack: Boolean,
                                  totalWeight: Long,
                                  totalCredits: BigDecimal
) extends Credits {

  private def exportedAnswerYesNoAsString: String =
    if(exported.yesNo) "site.yes" else "site.no"


  private def convertedAnswerYesNoAsString: String =
    if(converted.yesNo) "site.yes" else "site.no"

  override def summaryList: Seq[(String, String)] = {
    Seq(
      CreditExportedAnswerPartialKey -> exportedAnswerYesNoAsString,
      CreditExportedWeightPartialKey -> exported.value.asKg,
      CreditConvertedAnswerPartialKey -> convertedAnswerYesNoAsString,
      CreditConvertedWeightPartialKey -> converted.value.asKg,
      CreditsTotalWeightPartialKey -> totalWeight.asKg,
      CreditTotalPartialKey -> totalCredits.asPounds
    ).filter(canShowWeight(_))
  }

  private def canShowWeight(keyValue: (String,String)): Boolean = {
    keyValue match {
      case (CreditExportedWeightPartialKey, _) => exportedAnswerYesNoAsString.equals("site.yes")
      case (CreditConvertedWeightPartialKey,_) => convertedAnswerYesNoAsString.equals("site.yes")
      case _ => true
    }
  }
}

object CreditsClaimedDetails {

  val CreditExportedAnswerPartialKey = "credits.exported.answer"
  val CreditExportedWeightPartialKey = "credits.exported.weight"
  val CreditConvertedAnswerPartialKey = "credits.converted.answer"
  val CreditConvertedWeightPartialKey = "credits.converted.weight"
  val CreditsTotalWeightPartialKey = "credits.total.weight"
  val CreditTotalPartialKey = "credits.total"

  def apply(userAnswer: UserAnswers, creditBalance: CreditBalance): CreditsClaimedDetails = {
    CreditsClaimedDetails(
      userAnswer.getOrFail(ExportedCreditsPage),
      userAnswer.getOrFail(ConvertedCreditsPage),
      userAnswer.getOrFail(WhatDoYouWantToDoPage),
      creditBalance.totalRequestedCreditInKilograms,
      creditBalance.totalRequestedCreditInPounds,
    )
  }
}
