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

import play.api.data.Forms.{mapping, text}
import play.api.data.{Form, Forms}
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.plasticpackagingtax.returns.forms.ImportedPlasticWeight.{
  isDigitsOnly,
  isEqualToOrBelow,
  isNotEmpty
}

case class HumanMedicinesPlasticWeight(totalKg: Option[String])

object HumanMedicinesPlasticWeight {

  implicit val format: OFormat[HumanMedicinesPlasticWeight] =
    Json.format[HumanMedicinesPlasticWeight]

  val maxTotalKg         = 99999999
  val totalKg            = "totalKg"
  val weightEmptyError   = "returns.humanMedicinesPlasticWeight.empty.error"
  val invalidFormatError = "returns.humanMedicinesPlasticWeight.format.error"
  val aboveMaxError      = "returns.humanMedicinesPlasticWeight.aboveMax.error"
  val invalidValueError  = "returns.humanMedicinesPlasticWeight.invalidValue.error"

  def form(): Form[HumanMedicinesPlasticWeight] =
    Form(
      mapping(
        totalKg -> Forms.optional(text())
          .verifying(weightEmptyError, isNotEmpty)
          .verifying(invalidFormatError, v => !isNotEmpty(v) || isDigitsOnly(v))
          .verifying(aboveMaxError, v => !isDigitsOnly(v) || isEqualToOrBelow(v, maxTotalKg))
      )(HumanMedicinesPlasticWeight.apply)(HumanMedicinesPlasticWeight.unapply)
    )

}
