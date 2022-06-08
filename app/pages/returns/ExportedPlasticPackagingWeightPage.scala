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

package pages.returns

import models.UserAnswers
import pages.{ExportedRecycledPlasticPackagingPage, ExportedRecycledPlasticPackagingWeightPage, QuestionPage}
import play.api.libs.json.JsPath

import scala.util.Try

case object ExportedPlasticPackagingWeightPage extends QuestionPage[Long] {
  private def exportSplit(answers: UserAnswers): Boolean = {
    val total = answers.get(ManufacturedPlasticPackagingWeightPage).getOrElse(0L)
    val exported = answers.get(ExportedPlasticPackagingWeightPage).getOrElse(0L)

    exported < total
  }

  override def path: JsPath = JsPath \ toString

  override def toString: String = "exportedPlasticPackagingWeight"

  override def cleanup(value: Option[Long], userAnswers: UserAnswers): Try[UserAnswers] =
    value.map(amount =>
      if (amount > 0) {
        if(!exportSplit(userAnswers)) {
          userAnswers.set(DirectlyExportedComponentsPage, true, cleanup = false).get
            .remove(NonExportedHumanMedicinesPlasticPackagingPage).get
            .remove(NonExportedHumanMedicinesPlasticPackagingWeightPage).get
            .remove(RecycledPlasticPackagingPage).get
            .remove(RecycledPlasticPackagingWeightPage)
        }
        else {
          userAnswers.set(DirectlyExportedComponentsPage, true, cleanup = false)
        }
      } else {
        userAnswers.remove(HumanMedicinesPlasticPackagingPage).get
          .remove(ExportedRecycledPlasticPackagingPage)
      }
    ).getOrElse(super.cleanup(value, userAnswers))
}
