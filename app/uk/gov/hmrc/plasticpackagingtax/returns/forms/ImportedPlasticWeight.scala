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

import play.api.data.{Form, Forms}
import play.api.data.Forms.{mapping, text}
import play.api.libs.json.{Json, OFormat}

case class ImportedPlasticWeight(totalKg: Option[String], totalKgBelowThreshold: Option[String])

object ImportedPlasticWeight extends CommonFormValidators {

  implicit val format: OFormat[ImportedPlasticWeight] = Json.format[ImportedPlasticWeight]

  val maxTotalKg            = 99999999
  val totalKg               = "totalKg"
  val totalKgBelowThreshold = "totalKgBelowThreshold"
  val weightEmptyError      = "returns.importedPlasticWeight.empty.error"
  val invalidFormatError    = "returns.importedPlasticWeight.format.error"
  val aboveMaxError         = "returns.importedPlasticWeight.aboveMax.error"
  val invalidValueError     = "returns.importedPlasticWeight.invalidValue.error"

  val isValid: ImportedPlasticWeight => Boolean = value =>
    value.totalKgBelowThreshold.map(_.trim.toLong).getOrElse(0L) <= value.totalKg.map(
      _.trim.toLong
    ).getOrElse(0L)

  def form(): Form[ImportedPlasticWeight] =
    Form(
      mapping(
        totalKg -> Forms.optional(text())
          .verifying(weightEmptyError, isNotEmpty)
          .verifying(invalidFormatError, v => !isNotEmpty(v) || isDigitsOnly(v))
          .verifying(aboveMaxError, v => !isDigitsOnly(v) || isEqualToOrBelow(v, maxTotalKg)),
        totalKgBelowThreshold -> Forms.optional(text())
          .verifying(weightEmptyError, isNotEmpty)
          .verifying(invalidFormatError, v => !isNotEmpty(v) || isDigitsOnly(v))
          .verifying(aboveMaxError, v => !isDigitsOnly(v) || isEqualToOrBelow(v, maxTotalKg))
      )(ImportedPlasticWeight.apply)(ImportedPlasticWeight.unapply)
        .verifying(invalidValueError, isValid(_))
    )

}
