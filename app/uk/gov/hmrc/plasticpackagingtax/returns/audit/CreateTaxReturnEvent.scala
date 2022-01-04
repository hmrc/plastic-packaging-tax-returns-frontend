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

package uk.gov.hmrc.plasticpackagingtax.returns.audit

import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.plasticpackagingtax.returns.models.domain._

case class CreateTaxReturnEvent(
  id: String,
  manufacturedPlasticWeight: Option[ManufacturedPlasticWeight] = None,
  importedPlasticWeight: Option[ImportedPlasticWeight] = None,
  humanMedicinesPlasticWeight: Option[HumanMedicinesPlasticWeight] = None,
  exportedPlasticWeight: Option[ExportedPlasticWeight] = None,
  convertedPackagingCredit: Option[ConvertedPackagingCredit] = None,
  metaData: MetaData = MetaData()
)

object CreateTaxReturnEvent {
  implicit val format: OFormat[CreateTaxReturnEvent] = Json.format[CreateTaxReturnEvent]
  val eventType: String                              = "CREATE_PPT_TAX_RETURN"

  def apply(taxReturn: TaxReturn): CreateTaxReturnEvent =
    CreateTaxReturnEvent(id = taxReturn.id,
                         manufacturedPlasticWeight = taxReturn.manufacturedPlasticWeight,
                         importedPlasticWeight = taxReturn.importedPlasticWeight,
                         humanMedicinesPlasticWeight = taxReturn.humanMedicinesPlasticWeight,
                         exportedPlasticWeight = taxReturn.exportedPlasticWeight,
                         convertedPackagingCredit = taxReturn.convertedPackagingCredit,
                         metaData = taxReturn.metaData
    )

}
