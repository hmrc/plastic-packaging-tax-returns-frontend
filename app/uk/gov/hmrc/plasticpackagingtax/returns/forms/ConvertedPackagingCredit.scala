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

package uk.gov.hmrc.plasticpackagingtax.returns.forms

import play.api.data.Form
import play.api.data.Forms.{mapping, text}
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.plasticpackagingtax.returns.forms.ExportedPlasticWeight.{
  isLowerOrEqualTo,
  isNonEmpty,
  isValidDecimal
}
import uk.gov.hmrc.plasticpackagingtax.returns.utils.PriceConverter

case class ConvertedPackagingCredit(totalInPence: String) extends PriceConverter {
  def totalInPenceAsLong() = convertDecimalRepresentationToPences(this.totalInPence)
}

object ConvertedPackagingCredit {

  implicit val format: OFormat[ConvertedPackagingCredit] =
    Json.format[ConvertedPackagingCredit]

  val maxTotalCredit     = BigDecimal("99999999.99")
  val totalInPence       = "totalInPence"
  val creditEmptyError   = "returns.convertedPackagingCredit.empty.error"
  val invalidFormatError = "returns.convertedPackagingCredit.format.error"
  val aboveMaxError      = "returns.convertedPackagingCredit.aboveMax.error"

  val toPence: Option[String] => Long = value => value.map(BigDecimal(_).toLong).getOrElse(0)

  def form(): Form[ConvertedPackagingCredit] =
    Form(
      mapping(
        totalInPence -> text()
          .verifying(creditEmptyError, isNonEmpty)
          .verifying(invalidFormatError, v => !isNonEmpty(v) || isValidDecimal(v))
          .verifying(aboveMaxError, v => !isValidDecimal(v) || isLowerOrEqualTo(maxTotalCredit)(v))
      )(ConvertedPackagingCredit.apply)(ConvertedPackagingCredit.unapply)
    )

}
