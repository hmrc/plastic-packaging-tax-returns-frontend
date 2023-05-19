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

import models.returns.credits.CreditSummaryRow
import models.{CreditBalance, TaxablePlastic, UserAnswers}
import navigation.ReturnsJourneyNavigator
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import viewmodels.PrintBigDecimal
import viewmodels.govuk.summarylist._
import viewmodels.implicits._
import views.ViewUtils

import java.time.LocalDate


object CreditsClaimedListSummary {
  
  // TODO can this be made simpler?

  def createRows(userAnswer: UserAnswers, creditBalance: CreditBalance, navigator: ReturnsJourneyNavigator)
    (implicit messages: Messages): Seq[CreditSummaryRow] = {

    val isActionColumnHidden = maybeNavigator.isEmpty
    CreditsClaimedListSummary.createRows(creditBalance, maybeNavigator) ++ 
      Seq(CreditTotalSummary.createRow(creditBalance.totalRequestedCreditInPounds, isActionColumnHidden))
  }

  
  def createRows(creditBalance: CreditBalance, maybeNavigator: Option[ReturnsJourneyNavigator])
    (implicit messages: Messages): Seq[CreditSummaryRow] = {
    creditBalance.credit.toSeq.map {case (key, taxablePlastic) =>
      extractDateAndAmount(userAnswer, key, taxablePlastic)}
      .sortBy(_._1)
      .map {case (fromDate, toDate, amount) =>
        creditSummary(navigator, ViewUtils.displayDateRangeTo(fromDate, toDate), amount.asPounds)}
  }

  }

  //todo: We have two places were at the moment we getting the from and to date
  // (userAnswer and the key in CreditBalance). We may want to get this from one
  // place only. So we may want to put these in the CreditBalance -> TaxablePlastic
  private def extractDateAndAmount(
    userAnswer: UserAnswers,
    key: String,
    taxablePlastic: TaxablePlastic
  ): (LocalDate, LocalDate, BigDecimal) = {
    val fromDate = userAnswer.get[String](JsPath \ "credit" \ key \ "fromDate")
      .fold[LocalDate](throw new IllegalArgumentException)(LocalDate.parse(_))

    val toDate: LocalDate = userAnswer.get[String](JsPath \ "credit" \ key \ "endDate")
      .fold[LocalDate](throw new IllegalArgumentException)(LocalDate.parse(_))

    (fromDate, toDate, taxablePlastic.moneyInPounds)

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
