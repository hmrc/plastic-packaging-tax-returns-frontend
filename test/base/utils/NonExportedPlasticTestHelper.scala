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

package base.utils

import models.UserAnswers
import pages.returns._

object NonExportedPlasticTestHelper {

  def createUserAnswer(
    exportedAmount: Long,
    exportedByAnotherBusinessAmount: Long,
    manufacturedAmount: Long,
    importedAmount: Long
  ): UserAnswers = {
    UserAnswers("123")
      .set(ManufacturedPlasticPackagingPage, true, cleanup = false).get
      .set(ManufacturedPlasticPackagingWeightPage, manufacturedAmount, cleanup = false).get
      .set(ImportedPlasticPackagingPage, true, cleanup = false).get
      .set(ImportedPlasticPackagingWeightPage, importedAmount, cleanup = false).get
      .set(DirectlyExportedPage, true, cleanup = false).get
      .set(DirectlyExportedWeightPage, exportedAmount, cleanup = false).get
      .set(AnotherBusinessExportedPage, true, cleanup = false).get
      .set(AnotherBusinessExportedWeightPage, exportedByAnotherBusinessAmount, cleanup = false).get
  }
}
