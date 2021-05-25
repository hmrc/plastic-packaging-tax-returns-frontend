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

package uk.gov.hmrc.plasticpackagingtax.returns.utils

case class TaxLiability(
  totalKgLiable: Long = 0,
  totalKgExempt: Long = 0,
  totalCredit: BigDecimal = 0,
  taxDue: BigDecimal = 0
)

object TaxLiabilityFactory extends PriceConverter {

  private val taxValueInPencePerKg = BigDecimal("0.20")

  def create(
    totalManufacturedKg: Long,
    totalImportedKg: Long,
    totalHumanMedicinesKg: Long,
    totalDirectExportsKg: Long,
    totalConversionCreditPence: Long,
    totalRecycledKg: Long
  ): TaxLiability = {

    val totalKgLiable = totalManufacturedKg + totalImportedKg

    val totalKgExempt = totalHumanMedicinesKg + totalDirectExportsKg + totalRecycledKg

    val taxDue = taxValueInPencePerKg * totalKgLiable

    TaxLiability(totalKgLiable = totalKgLiable,
                 totalKgExempt = totalKgExempt,
                 totalCredit = toBigDecimal(totalConversionCreditPence),
                 taxDue = format(taxDue)
    )
  }

}
