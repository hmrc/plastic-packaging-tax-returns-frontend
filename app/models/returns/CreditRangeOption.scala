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

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

final case class CreditRangeOption(from: LocalDate, to: LocalDate) {
  def key: String = from.toString + "-" + to.toString
}

object CreditRangeOption {
  implicit val format: OFormat[CreditRangeOption] = Json.format[CreditRangeOption]
  def createFromString(key: String): CreditRangeOption = {
    val first    = key.substring(0, 10)
    val second   = key.substring(11, 21)
    val fromDate = LocalDate.parse(first)
    val toDate   = LocalDate.parse(second)
    CreditRangeOption(fromDate, toDate)
  }
}
