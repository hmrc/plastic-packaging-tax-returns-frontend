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

import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}
import util.EdgeOfSystem
import views.ViewUtils

import java.time.LocalDate

final case class TaxReturnObligation(
  fromDate: LocalDate,
  toDate: LocalDate,
  dueDate: LocalDate,
  periodKey: String
) {
  def toReturnQuarter(implicit messages: Messages): String =
    ViewUtils.displayReturnQuarter(fromDate, toDate)

  def tooOldToAmend(implicit edgeOfSystem: EdgeOfSystem): Boolean = {
    val today = edgeOfSystem.localDateTimeNow.toLocalDate
    dueDate.isBefore(today.minusYears(4))
  }
}

object TaxReturnObligation {
  implicit val format: OFormat[TaxReturnObligation] = Json.format[TaxReturnObligation]
}
