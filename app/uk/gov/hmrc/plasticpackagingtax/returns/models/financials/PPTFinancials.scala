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

package uk.gov.hmrc.plasticpackagingtax.returns.models.financials

import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

final case class PPTFinancials(
  creditAmount: Option[BigDecimal],
  debitAmount: Option[(BigDecimal, LocalDate)],
  overdueAmount: Option[BigDecimal]
) {

  private def getMonth(date: LocalDate)(implicit messages: Messages) =
    messages(s"month.${date.getMonthValue}")

  def paymentStatement()(implicit messages: Messages): String =
    (creditAmount, debitAmount, overdueAmount) match {
      case (None, None, None)         => messages("account.homePage.card.payments.nothingOutstanding")
      case (Some(amount), None, None) => messages("account.homePage.card.payments.inCredit", amount)
      case (None, Some((amount, date)), None) =>
        messages("account.homePage.card.payments.debitDue",
                 amount,
                 s"${date.getDayOfMonth} ${getMonth(date)} ${date.getYear}"
        )
      case (None, None, Some(amount)) => messages("account.homePage.card.payments.overDue", amount)
      case (None, Some((debit, _)), Some(overdue)) =>
        messages("account.homePage.card.payments.debitAndOverDue", debit, overdue)
      case _ => messages("account.homePage.card.payments.error")
    }

}

object PPTFinancials {
  implicit val format: OFormat[PPTFinancials] = Json.format[PPTFinancials]
}
