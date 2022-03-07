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

case class ManufacturedPlasticWeight(totalKg: String) {}

object ManufacturedPlasticWeight extends CommonFormValidators {

  implicit val format: OFormat[ManufacturedPlasticWeight] = Json.format[ManufacturedPlasticWeight]

  val maxTotalKg         = 99999999999L
  val totalKg            = "totalKg"
  val weightEmptyError   = "returns.manufacturedPlasticWeight.empty.error"
  val invalidFormatError = "returns.manufacturedPlasticWeight.format.error"
  val aboveMaxError      = "returns.manufacturedPlasticWeight.aboveMax.error"

  def form(): Form[ManufacturedPlasticWeight] =
    Form(
      mapping(
        totalKg -> text()
          .verifying(weightEmptyError, isNonEmpty)
          .verifying(invalidFormatError, v => !isNonEmpty(v) || isDigitsOnly(v))
          .verifying(aboveMaxError, v => !isDigitsOnly(v) || isEqualToOrBelow(v, maxTotalKg))
      )(ManufacturedPlasticWeight.apply)(ManufacturedPlasticWeight.unapply)
    )

}
