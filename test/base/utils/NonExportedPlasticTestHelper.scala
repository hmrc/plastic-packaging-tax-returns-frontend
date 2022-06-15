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

package base.utils

import models.UserAnswers
import pages.returns.{
  DirectlyExportedComponentsPage,
  ExportedPlasticPackagingWeightPage,
  ImportedPlasticPackagingPage,
  ImportedPlasticPackagingWeightPage,
  ManufacturedPlasticPackagingPage,
  ManufacturedPlasticPackagingWeightPage
}

object NonExportedPlasticTestHelper {

  def createUserAnswer
  (
    exportedAmount: Long,
    manufacturedAmount: Long,
    importedAmount: Long
  ): UserAnswers = {
    UserAnswers("123")
      .set(DirectlyExportedComponentsPage,true, cleanup = false).get
      .set(ExportedPlasticPackagingWeightPage,exportedAmount, cleanup = false).get
      .set(ManufacturedPlasticPackagingPage,true, cleanup = false).get
      .set(ManufacturedPlasticPackagingWeightPage,manufacturedAmount, cleanup = false).get
      .set(ImportedPlasticPackagingPage,true, cleanup = false).get
      .set(ImportedPlasticPackagingWeightPage,importedAmount, cleanup = false).get
  }
}
