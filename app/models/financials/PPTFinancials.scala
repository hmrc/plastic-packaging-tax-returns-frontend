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

package models.financials

import models.financials.CurrencyFormatters.formatCurrencyAmount
import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}

import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale

final case class Charge(amount: BigDecimal, date: LocalDate)

object Charge {
  implicit val writes: OFormat[Charge] = Json.format[Charge]
}

final case class PPTFinancials(
  creditAmount: Option[BigDecimal],
  debitAmount: Option[Charge],
  overdueAmount: Option[BigDecimal]
) {

  def amountToPayInPence: Int = debitAmount.map(_.amount).orElse(overdueAmount).map(a => (100 * a).toInt).getOrElse(0)

  private def getMonth(date: LocalDate)(implicit messages: Messages): String =
    messages(s"month.${date.getMonthValue}")

  def paymentStatement()(implicit messages: Messages): String =
    (creditAmount, debitAmount, overdueAmount) match {
      case (None, None, None) => messages("account.homePage.card.payments.nothingOutstanding")
      case (Some(amount), None, None) =>
        messages("account.homePage.card.payments.inCredit", formatCurrencyAmount(amount))
      case (None, Some(Charge(amount, date)), None) =>
        messages(
          "account.homePage.card.payments.debitDue",
          formatCurrencyAmount(amount),
          s"${date.getDayOfMonth} ${getMonth(date)} ${date.getYear}"
        )
      case (None, None, Some(amount)) =>
        messages("account.homePage.card.payments.overDue", formatCurrencyAmount(amount))
      case (None, Some(Charge(debit, _)), Some(overdue)) =>
        messages(
          "account.homePage.card.payments.debitAndOverDue",
          formatCurrencyAmount(debit),
          formatCurrencyAmount(overdue)
        )
      case _ => messages("account.homePage.card.payments.error")
    }

}

object PPTFinancials {
  implicit val format: OFormat[PPTFinancials] = Json.format[PPTFinancials]
}

object CurrencyFormatters {

  def formatCurrencyAmount(amount: BigDecimal): String = {
    val maxDecimalPlaces: Int      = 2
    val numberFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.UK)
    numberFormat.setMaximumFractionDigits(maxDecimalPlaces)
    numberFormat.setMinimumFractionDigits(maxDecimalPlaces)
    numberFormat.format(amount)
  }

}
