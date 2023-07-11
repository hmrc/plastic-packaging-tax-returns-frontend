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
import models.{CreditBalance, UserAnswers}
import navigation.ReturnsJourneyNavigator
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import viewmodels.PrintBigDecimal
import viewmodels.govuk.summarylist._
import viewmodels.implicits._
import views.ViewUtils

import java.time.LocalDate


case object CreditsClaimedListSummary {

  def createCreditSummary(userAnswer: UserAnswers, creditBalance: CreditBalance, maybeNavigator: Option[ReturnsJourneyNavigator])
                         (implicit messages: Messages): Seq[CreditSummaryRow] = {

    CreditsClaimedListSummary.createRows(userAnswer, creditBalance, maybeNavigator) :+
      CreditSummaryRow(messages("creditsSummary.table.total"), creditBalance.totalRequestedCreditInPounds.asPounds, Seq.empty)
  }

  
  def createRows(userAnswer: UserAnswers, creditBalance: CreditBalance, maybeNavigator: Option[ReturnsJourneyNavigator])
    (implicit messages: Messages): Seq[CreditSummaryRow] = {
    creditBalance.credit.toSeq
      .sortBy{case (key, _) => userAnswer.getOrFail[LocalDate](JsPath \ "credit" \ key \ "fromDate")}
      .map { case (key, taxablePlastic) =>
        creditSummary(maybeNavigator, key, userAnswer, taxablePlastic.moneyInPounds.asPounds)
      }
  }
  def createRows
  (
    userAnswers: UserAnswers,
    creditBalance: CreditBalance,
    navigator: ReturnsJourneyNavigator
  )(implicit messages: Messages): Seq[CreditSummaryRow] = {
    createRows(userAnswers, creditBalance, Some(navigator))

  }

  private def creditSummary(maybeNavigator: Option[ReturnsJourneyNavigator], key: String, userAnswer: UserAnswers, value: String)
    (implicit messages: Messages): CreditSummaryRow = {

    val fromDate = LocalDate.parse(userAnswer.getOrFail[String](JsPath \ "credit" \ key \ "fromDate"))
    val toDate: LocalDate = LocalDate.parse(userAnswer.getOrFail[String](JsPath \ "credit" \ key \ "toDate"))
    val label = ViewUtils.displayDateRangeTo(fromDate, toDate)

    CreditSummaryRow(
      label,
      value,
      actions = maybeNavigator.map { navigator =>
        Seq(
          ActionItemViewModel("site.change", navigator.creditSummaryChange(key))
            .withVisuallyHiddenText(messages("creditSummary.for", label)),
          ActionItemViewModel("site.remove", navigator.creditSummaryRemove(key))
            .withVisuallyHiddenText(messages("creditSummary.for", label))
        )
      }.getOrElse(Seq())
    )
  }
}
