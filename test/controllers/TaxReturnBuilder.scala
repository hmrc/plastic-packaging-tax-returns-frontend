package controllers

import models.returns._

import java.util.UUID

trait TaxReturnBuilder {

  private type TaxReturnModifier = TaxReturn => TaxReturn

  def aTaxReturn(modifiers: TaxReturnModifier*): TaxReturn =
    modifiers.foldLeft(modelWithDefaults)((current, modifier) => modifier(current))

  private def modelWithDefaults: TaxReturn =
    TaxReturn(id = UUID.randomUUID().toString, periodKey = "22AC")

  def withId(id: String): TaxReturnModifier = _.copy(id = id)

  def withManufacturedPlastic(manufacturedPlastic: Boolean): TaxReturnModifier =
    _.copy(manufacturedPlastic = Some(manufacturedPlastic))

  def withManufacturedPlasticWeight(totalKg: Long): TaxReturnModifier =
    _.copy(manufacturedPlasticWeight =
      Some(ManufacturedPlasticWeight(totalKg))
    )

  def withImportedPlasticWeight(totalKg: Long): TaxReturnModifier =
    _.copy(importedPlasticWeight = Some(ImportedPlasticWeight(totalKg)))

  def withConvertedPackagingCredit(totalInPounds: BigDecimal): TaxReturnModifier =
    _.copy(convertedPackagingCredit = Some(ConvertedPackagingCredit(totalInPounds)))

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
