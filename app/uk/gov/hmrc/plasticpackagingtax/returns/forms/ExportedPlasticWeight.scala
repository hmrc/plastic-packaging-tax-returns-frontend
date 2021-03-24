/*
 * Copyright 2021 HM Revenue & Customs
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

import play.api.data.Forms.text
import play.api.data.{Form, Forms}
import play.api.libs.json.{Json, OFormat}

case class ExportedPlasticWeight(totalKg: String, totalValueForCredit: String)

object ExportedPlasticWeight extends CommonFormValidators {

  implicit val format: OFormat[ExportedPlasticWeight] = Json.format[ExportedPlasticWeight]

  val totalKg             = "totalKg"
  val totalValueForCredit = "totalValueForCredit"
  val emptyError          = "returns.exportedPlasticWeight.empty.error"
  val invalidFormatError  = "returns.exportedPlasticWeight.format.error"
  val weightAboveMaxError = "returns.exportedPlasticWeight.weight.aboveMax.error"
  val creditAboveMaxError = "returns.exportedPlasticWeight.credit.aboveMax.error"

  val maxTotalKg        = 99999999
  val oneHundredMillion = BigDecimal(100000000)

  private val mapping = Forms.mapping(
    totalKg ->
      text()
        .verifying(emptyError, isNonEmpty)
        .verifying(invalidFormatError, v => !isNonEmpty(v) || isDigitsOnly(v))
        .verifying(weightAboveMaxError, v => !isDigitsOnly(v) || isEqualToOrBelow(v, maxTotalKg)),
    totalValueForCredit ->
      text()
        .verifying(emptyError, isNonEmpty)
        .verifying(invalidFormatError, v => !isNonEmpty(v) || isValidDecimal(v))
        .verifying(creditAboveMaxError, v => !isNonEmpty(v) || isLowerThan(oneHundredMillion)(v))
  )(ExportedPlasticWeight.apply)(ExportedPlasticWeight.unapply)

  def form(): Form[ExportedPlasticWeight] = Form(mapping)
}
