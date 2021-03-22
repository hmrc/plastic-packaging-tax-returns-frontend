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

package uk.gov.hmrc.plasticpackagingtax.returns.builders

import uk.gov.hmrc.plasticpackagingtax.returns.models.domain.{
  HumanMedicinesPlasticWeight,
  ImportedPlasticWeight,
  ManufacturedPlasticWeight,
  TaxReturn
}

//noinspection ScalaStyle
trait TaxReturnBuilder {

  private type TaxReturnModifier = TaxReturn => TaxReturn

  def aTaxReturn(modifiers: TaxReturnModifier*): TaxReturn =
    modifiers.foldLeft(modelWithDefaults)((current, modifier) => modifier(current))

  private def modelWithDefaults: TaxReturn =
    TaxReturn(id = "id", manufacturedPlasticWeight = ManufacturedPlasticWeight())

  def withId(id: String): TaxReturnModifier = _.copy(id = id)

  def withManufacturedPlasticWeight(
    totalKg: Option[Long],
    totalKgBelowThreshold: Option[Long]
  ): TaxReturnModifier =
    _.copy(manufacturedPlasticWeight = ManufacturedPlasticWeight(totalKg, totalKgBelowThreshold))

  def withImportedPlasticWeight(
    totalKg: Option[Long],
    totalKgBelowThreshold: Option[Long]
  ): TaxReturnModifier =
    _.copy(importedPlasticWeight = ImportedPlasticWeight(totalKg, totalKgBelowThreshold))

  def withHumanMedicinesPlasticWeight(totalKg: Option[Long]): TaxReturnModifier =
    _.copy(humanMedicinesPlasticWeight = HumanMedicinesPlasticWeight(totalKg))

}
