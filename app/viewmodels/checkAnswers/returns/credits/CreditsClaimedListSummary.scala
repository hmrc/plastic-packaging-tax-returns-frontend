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


object CreditsClaimedListSummary {

  def createRows(userAnswer: UserAnswers, creditBalance: CreditBalance, navigator: ReturnsJourneyNavigator)
    (implicit messages: Messages): Seq[CreditSummaryRow] = {

    creditBalance.credit.map { 
      case (key, taxablePlastic) =>
        creditSummary(navigator, displayDates(userAnswer, key), taxablePlastic.moneyInPounds.asPounds)
    }
      .toSeq
  }

  //todo: We have two places were at the moment we getting the from and to date
  // (userAnswer and the key in CreditBalance). We may want to get this from one
  // place only. So we may want to put these in the CreditBalance -> TaxablePlastic
  private def displayDates(userAnswer: UserAnswers, key: String)(implicit message: Messages): String = {
    val fromDate = userAnswer.get[String](JsPath \ "credit" \ key \ "fromDate")
      .fold[LocalDate](throw new IllegalArgumentException)(LocalDate.parse(_))

    val toDate: LocalDate = userAnswer.get[String](JsPath \ "credit" \ key \ "endDate")
      .fold[LocalDate](throw new IllegalArgumentException)(LocalDate.parse(_))

    ViewUtils.displayDateRangeTo(fromDate, toDate)
  }

  private def creditSummary(navigator: ReturnsJourneyNavigator, key: String, value: String) 
    (implicit messages: Messages): CreditSummaryRow = {
    
    CreditSummaryRow(
      key,
      value,
      actions = Seq(
        ActionItemViewModel("site.change", navigator.creditSummaryChange(key)),
        ActionItemViewModel("site.remove", navigator.creditSummaryRemove(key))
      )
    )
  }
}
