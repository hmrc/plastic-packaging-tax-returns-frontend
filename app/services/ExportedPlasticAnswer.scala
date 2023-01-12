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

import controllers.helpers.InjectableNonExportedAmountHelper
import models.UserAnswers
import pages.returns._

import scala.util.Try

// Todo: This may be rename better.
class ExportedPlasticAnswer(userAnswers: UserAnswers) {

  def resetExportedByYouIfAllExportedPlastic: Try[UserAnswers] = {
    if(isAllPlasticExported) {
      userAnswers.set(DirectlyExportedComponentsPage, true, cleanup = false).get
        .set(PlasticExportedByAnotherBusinessPage, false).get
        .set(AnotherBusinessExportWeightPage, 0L, cleanup = false).get
        .set(NonExportedHumanMedicinesPlasticPackagingPage, false, cleanup = false).get
        .set(NonExportedHumanMedicinesPlasticPackagingWeightPage, 0L, cleanup = false).get
        .set(NonExportedRecycledPlasticPackagingPage, false, cleanup = false).get
        .set(NonExportedRecycledPlasticPackagingWeightPage, 0L, cleanup = false)
    }
    else {
      userAnswers.set(DirectlyExportedComponentsPage, true, cleanup = false)
    }
  }

  def resetAnotherBusinessIfAllExportedPlastic: Try[UserAnswers] = {
    if(isAllPlasticExported) {
      userAnswers
        .set(DirectlyExportedComponentsPage, true, cleanup = false).get
        .set(NonExportedHumanMedicinesPlasticPackagingPage, false, cleanup = false).get
        .set(NonExportedHumanMedicinesPlasticPackagingWeightPage, 0L, cleanup = false).get
        .set(NonExportedRecycledPlasticPackagingPage, false, cleanup = false).get
        .set(NonExportedRecycledPlasticPackagingWeightPage, 0L, cleanup = false)
    }
    else {
      userAnswers.set(PlasticExportedByAnotherBusinessPage, true, cleanup = false)
    }
  }

  def resetAllIfNoTotalPlastic(helper: InjectableNonExportedAmountHelper): UserAnswers = {
    if(helper.totalPlastic(userAnswers).exists(_ > 0L)) {
      userAnswers
    } else {
      userAnswers.set(DirectlyExportedComponentsPage, false).get
        .set(ExportedPlasticPackagingWeightPage, 0L, cleanup = false).get
        .set(PlasticExportedByAnotherBusinessPage, false).get
        .set(AnotherBusinessExportWeightPage, 0L, cleanup = false).get
        .set(NonExportedHumanMedicinesPlasticPackagingPage, false, cleanup = false).get
        .set(NonExportedHumanMedicinesPlasticPackagingWeightPage, 0L, cleanup = false).get
        .set(NonExportedRecycledPlasticPackagingPage, false, cleanup = false).get
        .set(NonExportedRecycledPlasticPackagingWeightPage, 0L, cleanup = false).get
    }
  }

  def isAllPlasticExported: Boolean = {
    val manufactured = userAnswers.get(ManufacturedPlasticPackagingWeightPage).getOrElse(0L)
    val imported = userAnswers.get(ImportedPlasticPackagingWeightPage).getOrElse(0L)
    val exported = userAnswers.get(ExportedPlasticPackagingWeightPage).getOrElse(0L)
    val exportedByOther = userAnswers.get(AnotherBusinessExportWeightPage).getOrElse(0L)

    exported + exportedByOther >= (manufactured + imported)
  }
}

object ExportedPlasticAnswer {

  def apply(userAnswer: UserAnswers): ExportedPlasticAnswer = {
    new ExportedPlasticAnswer(userAnswer)
  }
}
