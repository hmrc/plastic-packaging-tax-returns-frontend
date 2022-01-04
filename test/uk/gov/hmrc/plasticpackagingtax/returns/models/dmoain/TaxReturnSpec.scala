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

package uk.gov.hmrc.plasticpackagingtax.returns.models.dmoain

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import uk.gov.hmrc.plasticpackagingtax.returns.builders.TaxReturnBuilder
import uk.gov.hmrc.plasticpackagingtax.returns.models.domain.TaxReturn

class TaxReturnSpec extends AnyWordSpecLike with TaxReturnBuilder with Matchers {

  "Tax liability" should {

    "be equal to zero" when {

      "no data present" in {

        val taxReturn = TaxReturn("id")

        taxReturn.taxLiability.totalKgLiable mustBe 0
        taxReturn.taxLiability.totalCredit mustBe 0
        taxReturn.taxLiability.totalKgExempt mustBe 0
        taxReturn.taxLiability.taxDue mustBe 0
      }
    }

    "be calculated" when {

      "all data is present" in {

        val taxReturn = aTaxReturn(withId("01"),
                                   withManufacturedPlasticWeight(5),
                                   withConvertedPackagingCredit(5),
                                   withDirectExportDetails(2),
                                   withHumanMedicinesPlasticWeight(4),
                                   withImportedPlasticWeight(2),
                                   withRecycledPlasticWeight(6)
        )

        taxReturn.taxLiability.totalKgLiable must (not be 0)
        taxReturn.taxLiability.totalCredit must (not be 0)
        taxReturn.taxLiability.totalKgExempt must (not be 0)
        taxReturn.taxLiability.taxDue must (not be 0)
      }
    }

    "be calculated" when {

      "partial data is present" in {

        val taxReturn =
          aTaxReturn(withId("01"), withManufacturedPlasticWeight(5), withImportedPlasticWeight(2))

        taxReturn.taxLiability.totalKgLiable mustBe 7
        taxReturn.taxLiability.totalCredit mustBe 0
        taxReturn.taxLiability.totalKgExempt mustBe 0
        taxReturn.taxLiability.taxDue must (not be 0)
      }
    }
  }
}
