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

package models.returns

import models.returns.credits.CreditSummaryRow
import models.{CreditBalance, UserAnswers}
import play.api.i18n.Messages
import play.twirl.api.Html
import viewmodels.checkAnswers.returns.credits.CreditsClaimedListSummary

case class CreditsClaimedDetails(
  override val summaryList: Seq[CreditSummaryRow],
  totalClaimAmount: BigDecimal
) extends Credits {

  def ifClaiming(claimAmountToHtml: BigDecimal => Html): Html =
    if (totalClaimAmount > 0)
      claimAmountToHtml(totalClaimAmount)
    else
      Html(None)

}

object CreditsClaimedDetails {

  def apply(userAnswer: UserAnswers, creditBalance: CreditBalance)(implicit messages: Messages): CreditsClaimedDetails =
    CreditsClaimedDetails(
      CreditsClaimedListSummary.createCreditSummary(userAnswer, creditBalance, None),
      totalClaimAmount = creditBalance.totalRequestedCreditInPounds
    )
}
