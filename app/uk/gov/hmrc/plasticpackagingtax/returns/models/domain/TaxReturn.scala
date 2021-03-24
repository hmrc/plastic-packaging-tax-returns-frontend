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

package uk.gov.hmrc.plasticpackagingtax.returns.models.domain

import play.api.libs.json.{Json, OFormat}

case class TaxReturn(
  id: String,
  manufacturedPlasticWeight: Option[ManufacturedPlasticWeight] = None,
  importedPlasticWeight: Option[ImportedPlasticWeight] = None,
  humanMedicinesPlasticWeight: Option[HumanMedicinesPlasticWeight] = None,
  exportedPlasticWeight: Option[ExportedPlasticWeight] = None,
  convertedPackagingCredit: Option[ConvertedPackagingCredit] = None
) {

  def toTaxReturn: TaxReturn =
    TaxReturn(id = this.id,
              manufacturedPlasticWeight = this.manufacturedPlasticWeight,
              importedPlasticWeight = this.importedPlasticWeight,
              humanMedicinesPlasticWeight = this.humanMedicinesPlasticWeight,
              exportedPlasticWeight = this.exportedPlasticWeight,
              convertedPackagingCredit = this.convertedPackagingCredit
    )

}

object TaxReturn {
  implicit val format: OFormat[TaxReturn] = Json.format[TaxReturn]
}
