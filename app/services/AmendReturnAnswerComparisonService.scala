/*
 * Copyright 2023 HM Revenue & Customs
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
import pages.amends._

class AmendReturnAnswerComparisonService {

  def hasMadeChangesOnAmend(userAnswers: UserAnswers): Boolean = {
    userAnswers.get(ReturnDisplayApiCacheable) match {
      case None => throw new Exception("Original return missing from user answers")

      case Some(original) =>

        val manufacturedHasChanged: Boolean = userAnswers.get(AmendManufacturedPlasticPackagingPage)
          .exists(_ != original.returnDetails.manufacturedWeight)
        val importedHasChanged: Boolean = userAnswers.get(AmendImportedPlasticPackagingPage)
          .exists(_ != original.returnDetails.importedWeight)
        val directExportHasChanged: Boolean = userAnswers.get(AmendDirectExportPlasticPackagingPage)
          .exists(_ != original.returnDetails.directExports)
        val recycledHasChanged: Boolean = userAnswers.get(AmendRecycledPlasticPackagingPage)
          .exists(_ != original.returnDetails.recycledPlastic)
        val humanMedicinesHasChanged: Boolean = userAnswers.get(AmendHumanMedicinePlasticPackagingPage)
          .exists(_ != original.returnDetails.humanMedicines)

        val anAmendmentHasBeenMade: Boolean =
          userAnswers.get(AmendManufacturedPlasticPackagingPage).isDefined ||
            userAnswers.get(AmendImportedPlasticPackagingPage).isDefined ||
            userAnswers.get(AmendDirectExportPlasticPackagingPage).isDefined ||
            userAnswers.get(AmendRecycledPlasticPackagingPage).isDefined ||
            userAnswers.get(AmendHumanMedicinePlasticPackagingPage).isDefined

        val aUsefulAmendHasBeenMade: Boolean =
          Seq(manufacturedHasChanged,
            importedHasChanged,
            directExportHasChanged,
            recycledHasChanged,
            humanMedicinesHasChanged)
            .contains(true)

        anAmendmentHasBeenMade && aUsefulAmendHasBeenMade

    }



  }

}
