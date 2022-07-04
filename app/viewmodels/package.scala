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

import java.text.DecimalFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

package object viewmodels {

  private val poundsFormat = new DecimalFormat("£#,##0.00")
  private val kgsFormat = new DecimalFormat("#,###kg")

  implicit class PrintBigDecimal(val amount: BigDecimal) extends AnyVal {
    def asPounds: String = poundsFormat.format(amount)

    def asKg: String = kgsFormat.format(amount)
  }

  implicit class PrintLong(val amount: Long) extends AnyVal {
    def asPounds: String = poundsFormat.format(amount)

    def asKg: String = kgsFormat.format(amount)
    def asKgs: String = asKg
  }

  implicit class PrintDate(val date: String) {
    val datetime = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"))
      .format(DateTimeFormatter.ofPattern("dd MMMM yyyy")) //TODO: Translate month to Welsh

    def asDate: String = datetime
  }
}
