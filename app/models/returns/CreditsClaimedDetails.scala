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

package models.returns

import models.returns.CreditsClaimedDetails._
import models.{CreditBalance, UserAnswers}
import pages.returns.credits.{ConvertedCreditsPage, ConvertedCreditsWeightPage, ExportedCreditsPage, ExportedCreditsWeightPage}
import viewmodels.{PrintBigDecimal, PrintLong}

case class CreditsClaimedDetails(
                                  isExported: Boolean,
                                  exportedCreditsWeight: Long,
                                  isCreditsConverted: Boolean,
                                  convertedCreditsWeight: Long,
                                  totalWeight: Long,
                                  totalCredits: BigDecimal
) extends Credits {

  override def summaryList: Seq[(String, String)] = {
    val convertedCreditYesNoMsgKey: String = if (isCreditsConverted) "site.yes" else "site.no"
    val exportedCreditYesNoMsgKey: String = if(isExported) "site.yes" else "site.no"

    Seq(
      CreditExportedAnswerPartialKey -> exportedCreditYesNoMsgKey -> true,
      CreditExportedWeightPartialKey -> getExportedValue -> true,
      CreditConvertedAnswerPartialKey -> convertedCreditYesNoMsgKey -> true,
      CreditConvertedWeightPartialKey -> getConvertedValue -> true,
      CreditsTotalWeightPartialKey -> totalWeight.asKg -> true,
      CreditTotalPartialKey -> totalCredits.asPounds -> true
    ).collect { case (tuple, show) if show => tuple }
  }

  private def getExportedValue = {
    if(isExported)
      exportedCreditsWeight.asKg
    else 0L.asKg
  }

  
  private def getConvertedValue = {
    if(isCreditsConverted)
      convertedCreditsWeight.asKg
    else 0L.asKg
  }
}

object CreditsClaimedDetails {

  val CreditExportedAnswerPartialKey = "submit-return.check-your-answers.credits.exported.answer"
  val CreditExportedWeightPartialKey = "submit-return.check-your-answers.credits.exported.weight"
  val CreditConvertedAnswerPartialKey = "submit-return.check-your-answers.credits.converted.answer"
  val CreditConvertedWeightPartialKey = "submit-return.check-your-answers.credits.converted.weight"
  val CreditsTotalWeightPartialKey = "submit-return.check-your-answers.credits.total.weight"
  val CreditTotalPartialKey = "submit-return.check-your-answers.credits.total"

  def apply(userAnswer: UserAnswers, creditBalance: CreditBalance): CreditsClaimedDetails = {
    CreditsClaimedDetails(
      userAnswer.getOrFail(ExportedCreditsPage),
      userAnswer.getOrFail(ExportedCreditsWeightPage),
      userAnswer.getOrFail(ConvertedCreditsPage),
      userAnswer.getOrFail(ConvertedCreditsWeightPage),
      creditBalance.totalRequestedCreditInKilograms,
      creditBalance.totalRequestedCreditInPounds,
    )
  }
}
