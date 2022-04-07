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

package generators

import org.scalacheck.Arbitrary
import pages._

trait PageGenerators {

  implicit lazy val arbitraryAgentsPage: Arbitrary[AgentsPage.type] =
    Arbitrary(AgentsPage)

  implicit lazy val arbitraryConvertedPackagingCreditPage
    : Arbitrary[ConvertedPackagingCreditPage.type] =
    Arbitrary(ConvertedPackagingCreditPage)

  implicit lazy val arbitraryRecycledPlasticPackagingWeightPage
    : Arbitrary[RecycledPlasticPackagingWeightPage.type] =
    Arbitrary(RecycledPlasticPackagingWeightPage)

  implicit lazy val arbitraryManufacturedPlasticPackagingWeightPage
    : Arbitrary[ManufacturedPlasticPackagingWeightPage.type] =
    Arbitrary(ManufacturedPlasticPackagingWeightPage)

  implicit lazy val arbitraryManufacturedPlasticPackagingPage
    : Arbitrary[ManufacturedPlasticPackagingPage.type] =
    Arbitrary(ManufacturedPlasticPackagingPage)

  implicit lazy val arbitraryImportedPlasticPackagingWeightPage
    : Arbitrary[ImportedPlasticPackagingWeightPage.type] =
    Arbitrary(ImportedPlasticPackagingWeightPage)

  implicit lazy val arbitraryImportedPlasticPackagingPage
    : Arbitrary[ImportedPlasticPackagingPage.type] =
    Arbitrary(ImportedPlasticPackagingPage)

  implicit lazy val arbitraryHumanMedicinesPlasticPackagingWeightPage
    : Arbitrary[HumanMedicinesPlasticPackagingWeightPage.type] =
    Arbitrary(HumanMedicinesPlasticPackagingWeightPage)

  implicit lazy val arbitraryExportedPlasticPackagingWeightPage
    : Arbitrary[ExportedPlasticPackagingWeightPage.type] =
    Arbitrary(ExportedPlasticPackagingWeightPage)

  implicit lazy val arbitraryAmendAreYouSurePage: Arbitrary[AmendAreYouSurePage.type] =
    Arbitrary(AmendAreYouSurePage)

  implicit lazy val arbitraryAmendRecycledPlasticPackagingPage
    : Arbitrary[AmendRecycledPlasticPackagingPage.type] =
    Arbitrary(AmendRecycledPlasticPackagingPage)

  implicit lazy val arbitraryAmendManufacturedPlasticPackagingPage
    : Arbitrary[AmendManufacturedPlasticPackagingPage.type] =
    Arbitrary(AmendManufacturedPlasticPackagingPage)

  implicit lazy val arbitraryAmendImportedPlasticPackagingPage
    : Arbitrary[AmendImportedPlasticPackagingPage.type] =
    Arbitrary(AmendImportedPlasticPackagingPage)

  implicit lazy val arbitraryAmendHumanMedicinePlasticPackagingPage
    : Arbitrary[AmendHumanMedicinePlasticPackagingPage.type] =
    Arbitrary(AmendHumanMedicinePlasticPackagingPage)

  implicit lazy val arbitraryAmendDirectExportPlasticPackagingPage
    : Arbitrary[AmendDirectExportPlasticPackagingPage.type] =
    Arbitrary(AmendDirectExportPlasticPackagingPage)

}