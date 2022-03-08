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

package uk.gov.hmrc.plasticpackagingtax.returns.builders

import uk.gov.hmrc.plasticpackagingtax.returns.models.domain._
import uk.gov.hmrc.plasticpackagingtax.returns.models.obligations.Obligation

import java.time.LocalDate
import java.util.UUID

//noinspection ScalaStyle
trait TaxReturnBuilder {

  private type TaxReturnModifier = TaxReturn => TaxReturn

  def aTaxReturn(modifiers: TaxReturnModifier*): TaxReturn =
    modifiers.foldLeft(modelWithDefaults)((current, modifier) => modifier(current))

  private def modelWithDefaults: TaxReturn =
    TaxReturn(id = UUID.randomUUID().toString, obligation = Some(defaultObligation))

  val defaultObligation = Obligation(fromDate = LocalDate.parse("2022-04-01"),
                                     toDate = LocalDate.parse("2022-06-30"),
                                     dueDate = LocalDate.parse("2022-09-30"),
                                     periodKey = "22AC"
  )

  def withId(id: String): TaxReturnModifier = _.copy(id = id)

  def withManufacturedPlastic(manufacturedPlastic: Boolean): TaxReturnModifier =
    _.copy(manufacturedPlastic = Some(manufacturedPlastic))

  def withManufacturedPlasticWeight(totalKg: Long): TaxReturnModifier =
    _.copy(manufacturedPlasticWeight =
      Some(ManufacturedPlasticWeight(totalKg))
    )

  def withImportedPlasticWeight(totalKg: Long): TaxReturnModifier =
    _.copy(importedPlasticWeight = Some(ImportedPlasticWeight(totalKg)))

  def withConvertedPackagingCredit(totalInPence: Long): TaxReturnModifier =
    _.copy(convertedPackagingCredit = Some(ConvertedPackagingCredit(totalInPence)))

  def withHumanMedicinesPlasticWeight(totalKg: Long): TaxReturnModifier =
    _.copy(humanMedicinesPlasticWeight = Some(HumanMedicinesPlasticWeight(totalKg)))

  def withDirectExportDetails(totalKg: Long): TaxReturnModifier =
    _.copy(exportedPlasticWeight =
      Some(ExportedPlasticWeight(totalKg = totalKg))
    )

  def withRecycledPlasticWeight(totalKg: Long): TaxReturnModifier =
    _.copy(recycledPlasticWeight =
      Some(RecycledPlasticWeight(totalKg))
    )

}
