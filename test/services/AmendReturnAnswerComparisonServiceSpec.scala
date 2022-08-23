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

package services

import cacheables.ReturnDisplayApiCacheable
import models.UserAnswers
import models.returns.{IdDetails, ReturnDisplayApi, ReturnDisplayDetails}
import org.scalatestplus.play.PlaySpec
import pages.amends.{AmendDirectExportPlasticPackagingPage, AmendHumanMedicinePlasticPackagingPage, AmendImportedPlasticPackagingPage, AmendManufacturedPlasticPackagingPage, AmendRecycledPlasticPackagingPage}

class AmendReturnAnswerComparisonServiceSpec extends PlaySpec {

  val service: AmendReturnAnswerComparisonService = new AmendReturnAnswerComparisonService
  val retDisApi: ReturnDisplayApi = ReturnDisplayApi(
    "",
    IdDetails("", ""),
    None,
    ReturnDisplayDetails(500, 400, 660, 210, 220, 230, 350, 350, 900, 1000)
  )

  val userAnswersNoChanges = UserAnswers("userAnswersId").set(ReturnDisplayApiCacheable, retDisApi).get


  "hasMadeChangesOnAmend" must {

    "return false" when {

      "no changes have been made" in {
        service.hasMadeChangesOnAmend(userAnswersNoChanges) mustBe false
      }

      "a field has been amended to the same value" in {
        val userAnswersWithPointlessChange: UserAnswers = userAnswersNoChanges
          .set(AmendManufacturedPlasticPackagingPage, 500L).get

        service.hasMadeChangesOnAmend(userAnswersWithPointlessChange) mustBe false
      }
      "multiple fields have pointless amendments" in {
        val userAnswersWithPointlessChange: UserAnswers = userAnswersNoChanges
          .set(AmendManufacturedPlasticPackagingPage, 500L).get
          .set(AmendImportedPlasticPackagingPage, 400L).get

        service.hasMadeChangesOnAmend(userAnswersWithPointlessChange) mustBe false
      }
    }

    "return true" when {

      "manufacturedPlastic has been amended" in {
        val userAnswersWithOneChange = userAnswersNoChanges.set(AmendManufacturedPlasticPackagingPage, 333L)
        service.hasMadeChangesOnAmend(userAnswersWithOneChange.get) mustBe true
      }

      "importedPlastic has been amended" in {
        val userAnswersWithOneChange = userAnswersNoChanges.set(AmendImportedPlasticPackagingPage, 333L)
        service.hasMadeChangesOnAmend(userAnswersWithOneChange.get) mustBe true
      }
      "humanMedicines has been amended" in {
        val userAnswersWithOneChange = userAnswersNoChanges.set(AmendHumanMedicinePlasticPackagingPage, 333L)
        service.hasMadeChangesOnAmend(userAnswersWithOneChange.get) mustBe true
      }
      "recycled has been amended" in {
        val userAnswersWithOneChange = userAnswersNoChanges.set(AmendRecycledPlasticPackagingPage, 333L)
        service.hasMadeChangesOnAmend(userAnswersWithOneChange.get) mustBe true
      }
      "directExports has been amended" in {
        val userAnswersWithOneChange = userAnswersNoChanges.set(AmendDirectExportPlasticPackagingPage, 333L)
        service.hasMadeChangesOnAmend(userAnswersWithOneChange.get) mustBe true
      }
    }
    "throws exception" when {
      "when returnDisplayAPI is missing" in {
        val userAnswersNoRDA = UserAnswers("userAnswersId")
        val exception = intercept[Exception](service.hasMadeChangesOnAmend(userAnswersNoRDA))

        exception.getMessage mustBe "Original return missing from user answers"

      }

    }
  }

}
