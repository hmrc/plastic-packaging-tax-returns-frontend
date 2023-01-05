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

package pages.returns

import models.UserAnswers
import pages.QuestionPage
import play.api.libs.json.JsPath

import scala.util.Try

case object ConfirmPlasticPackagingTotalPage extends QuestionPage[Long] {

  override def path: JsPath = JsPath \ "confirmPlasticPackagingTotalPage"

  override def cleanup(value: Option[Long], userAnswers: UserAnswers): Try[UserAnswers] = {

    value.map(amount =>
      if (amount <= 0) {
          userAnswers.set(DirectlyExportedComponentsPage, false).get
            .set(ExportedPlasticPackagingWeightPage, 0L, cleanup = false).get
            .set(PlasticExportedByAnotherBusinessPage, false).get
            .set(AnotherBusinessExportWeightPage, 0L, cleanup = false).get
            .set(NonExportedHumanMedicinesPlasticPackagingPage, false, cleanup = false).get
            .set(NonExportedHumanMedicinesPlasticPackagingWeightPage, 0L, cleanup = false).get
            .set(NonExportedRecycledPlasticPackagingPage, false, cleanup = false).get
            .set(NonExportedRecycledPlasticPackagingWeightPage, 0L, cleanup = false).get
            .remove(ConfirmPlasticPackagingTotalPage, false)
      } else {
        super.cleanup(value, userAnswers.remove(ConfirmPlasticPackagingTotalPage, false).get)
      }
    ).getOrElse(super.cleanup(value, userAnswers.remove(ConfirmPlasticPackagingTotalPage, false).get))
  }
}
