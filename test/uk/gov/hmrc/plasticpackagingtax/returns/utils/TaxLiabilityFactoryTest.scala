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

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.plasticpackagingtax.returns.builders.TaxReturnBuilder

class TaxLiabilityFactoryTest extends AnyWordSpec with Matchers with TaxReturnBuilder {

  "TaxLiabilityFactory" should {

    val taxLiability = TaxLiabilityFactory.create(totalManufacturedKg = 200,
                                                  totalImportedKg = 100,
                                                  totalHumanMedicinesKg = 4,
                                                  totalDirectExportsKg = 3,
                                                  totalDirectExportsCreditPence = 204,
                                                  totalConversionCreditPence = 134,
                                                  totalRecycledKg = 20
    )

    "calculate totalKgLiable" in {
      taxLiability.totalKgLiable mustBe 300
    }

    "calculate totalKgExempt" in {
      taxLiability.totalKgExempt mustBe 27
    }

    "calculate totalCredit" in {
      taxLiability.totalCredit mustBe BigDecimal("3.38")
    }

    "calculate taxLiability" in {
      taxLiability.taxDue mustBe BigDecimal("60.00")
    }

    "returns taxLiability as zero" when {

      "all values are zero's" in {
        val taxLiability = TaxLiabilityFactory.create(totalManufacturedKg = 0,
                                                      totalImportedKg = 0,
                                                      totalHumanMedicinesKg = 0,
                                                      totalDirectExportsKg = 0,
                                                      totalDirectExportsCreditPence = 0,
                                                      totalConversionCreditPence = 0,
                                                      totalRecycledKg = 0
        )
        taxLiability.taxDue mustBe BigDecimal("0.00")
        taxLiability.totalKgExempt mustBe 0
        taxLiability.totalKgLiable mustBe 0
        taxLiability.totalCredit mustBe BigDecimal("0.00")
      }
    }
  }
}
