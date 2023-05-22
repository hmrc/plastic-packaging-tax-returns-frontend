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

package viewmodels.checkAnswers.returns.credits

import models.CreditBalance
import models.returns.credits.CreditSummaryRow
import navigation.ReturnsJourneyNavigator
import play.api.i18n.Messages
import viewmodels.PrintBigDecimal
import viewmodels.govuk.summarylist._
import viewmodels.implicits._


object CreditsClaimedListSummary {
  
  // TODO can this be made simpler?

  def createCreditSummary(creditBalance: CreditBalance, maybeNavigator: Option[ReturnsJourneyNavigator]) 
    (implicit messages: Messages): Seq[CreditSummaryRow] = {

    val isActionColumnHidden = maybeNavigator.isEmpty
    CreditsClaimedListSummary.createRows(creditBalance, maybeNavigator) ++ 
      Seq(CreditTotalSummary.createRow(creditBalance.totalRequestedCreditInPounds, isActionColumnHidden))
  }

  def createRows(creditBalance: CreditBalance, navigator: ReturnsJourneyNavigator)
    (implicit messages: Messages): Seq[CreditSummaryRow] = createRows(creditBalance, Some(navigator))
  
  def createRows(creditBalance: CreditBalance, maybeNavigator: Option[ReturnsJourneyNavigator])
    (implicit messages: Messages): Seq[CreditSummaryRow] = {

    creditBalance.credit.map { 
      case (key, taxablePlastic) => creditSummary(maybeNavigator, key, taxablePlastic.moneyInPounds.asPounds)
    }.toSeq
  }

  private def creditSummary(maybeNavigator: Option[ReturnsJourneyNavigator], key: String, value: String) 
    (implicit messages: Messages): CreditSummaryRow = {
    
    CreditSummaryRow(
      key,
      value,
      actions = maybeNavigator.map { navigator =>
        Seq(
          ActionItemViewModel("site.change", navigator.creditSummaryChange(key)),
          ActionItemViewModel("site.remove", navigator.creditSummaryRemove(key))
        )
      }.getOrElse(Seq()), 
      isActionColumnHidden = maybeNavigator.isEmpty
    )
  }
}
