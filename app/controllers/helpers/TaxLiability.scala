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

package controllers.helpers

import scala.math.BigDecimal.RoundingMode

case class TaxLiability(
                         totalKgLiable: Long = 0,
                         totalKgExempt: Long = 0,
                         totalCredit: BigDecimal = 0,
                         taxDue: BigDecimal = 0
                       )

object TaxLiabilityFactory {

  private val taxValueInPencePerKg = BigDecimal("0.20")

  def create(
              totalManufacturedKg: Long,
              totalImportedKg: Long,
              totalHumanMedicinesKg: Long,
              totalDirectExportsKg: Long,
              totalConversionCreditPounds: BigDecimal,
              totalRecycledKg: Long
            ): TaxLiability = {

    val totalKgLiable = totalManufacturedKg + totalImportedKg
    val totalKgExempt = totalHumanMedicinesKg + totalDirectExportsKg + totalRecycledKg

    val taxDue = taxValueInPencePerKg * BigDecimal(
      scala.math.max(totalKgLiable - totalKgExempt, 0)
    ).setScale(2, RoundingMode.HALF_EVEN)

    TaxLiability(totalKgLiable = totalKgLiable,
      totalKgExempt = totalKgExempt,
      totalCredit = totalConversionCreditPounds,
      taxDue = taxDue
    )
  }

}
