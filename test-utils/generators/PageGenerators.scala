/*
 * Copyright 2025 HM Revenue & Customs
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

import cacheables._
import org.scalacheck.Arbitrary
import pages._
import pages.amends._
import pages.changeGroupLead._
import pages.returns._
import pages.returns.credits._

trait PageGenerators {

  implicit lazy val arbitraryReturnObligationCacheable: Arbitrary[ReturnObligationCacheable.type] =
    Arbitrary(ReturnObligationCacheable)

  implicit lazy val arbitraryReturnDisplayApiCacheable: Arbitrary[ReturnDisplayApiCacheable.type] =
    Arbitrary(ReturnDisplayApiCacheable)

  implicit lazy val arbitraryIsFirstReturnCacheable: Arbitrary[IsFirstReturnCacheable.type] =
    Arbitrary(IsFirstReturnCacheable)

  implicit lazy val arbitraryAmendSelectedPeriodKey: Arbitrary[AmendSelectedPeriodKey.type] =
    Arbitrary(AmendSelectedPeriodKey)

  implicit lazy val arbitraryAmendObligationCacheable: Arbitrary[AmendObligationCacheable.type] =
    Arbitrary(AmendObligationCacheable)

  implicit lazy val arbitraryAmendExportedByAnotherBusinessPage: Arbitrary[AmendExportedByAnotherBusinessPage.type] =
    Arbitrary(AmendExportedByAnotherBusinessPage)

  implicit lazy val arbitraryAnotherBusinessExportWeightPage: Arbitrary[AnotherBusinessExportedWeightPage.type] =
    Arbitrary(AnotherBusinessExportedWeightPage)

  implicit lazy val arbitraryPlasticExportedByAnotherBusinessPage: Arbitrary[AnotherBusinessExportedPage.type] =
    Arbitrary(AnotherBusinessExportedPage)

  implicit lazy val arbitraryMainContactNamePage: Arbitrary[MainContactNamePage.type] =
    Arbitrary(MainContactNamePage)

  implicit lazy val arbitraryNewGroupLeadEnterContactAddressPage: Arbitrary[NewGroupLeadEnterContactAddressPage.type] =
    Arbitrary(NewGroupLeadEnterContactAddressPage)

  implicit lazy val arbitraryMainContactJobTitlePage: Arbitrary[MainContactJobTitlePage.type] =
    Arbitrary(MainContactJobTitlePage)

  implicit lazy val arbitraryExportedCreditsPage: Arbitrary[ExportedCreditsPage.type] =
    Arbitrary(ExportedCreditsPage)

  implicit lazy val arbitraryConvertedCreditsPage: Arbitrary[ConvertedCreditsPage.type] =
    Arbitrary(ConvertedCreditsPage)

  implicit lazy val arbitraryNonExportedHumanMedicinesPlasticPackagingWeightPage
    : Arbitrary[NonExportedHumanMedicinesPlasticPackagingWeightPage.type] =
    Arbitrary(NonExportedHumanMedicinesPlasticPackagingWeightPage)

  implicit lazy val arbitraryNonExportedHumanMedicinesPlasticPackagingPage
    : Arbitrary[NonExportedHumanMedicinesPlasticPackagingPage.type] =
    Arbitrary(NonExportedHumanMedicinesPlasticPackagingPage)

  implicit lazy val arbitraryRecycledPlasticPackagingPage: Arbitrary[NonExportedRecycledPlasticPackagingPage.type] =
    Arbitrary(NonExportedRecycledPlasticPackagingPage)

  implicit lazy val arbitraryDirectlyExportedComponentsPage: Arbitrary[DirectlyExportedPage.type] =
    Arbitrary(DirectlyExportedPage)

  implicit lazy val arbitraryAgentsPage: Arbitrary[AgentsPage.type] =
    Arbitrary(AgentsPage)

  implicit lazy val arbitraryStartYourReturnPage: Arbitrary[StartYourReturnPage.type] =
    Arbitrary(StartYourReturnPage)

  implicit lazy val arbitraryRecycledPlasticPackagingWeightPage
    : Arbitrary[NonExportedRecycledPlasticPackagingWeightPage.type] =
    Arbitrary(NonExportedRecycledPlasticPackagingWeightPage)

  implicit lazy val arbitraryManufacturedPlasticPackagingWeightPage
    : Arbitrary[ManufacturedPlasticPackagingWeightPage.type] =
    Arbitrary(ManufacturedPlasticPackagingWeightPage)

  implicit lazy val arbitraryManufacturedPlasticPackagingPage: Arbitrary[ManufacturedPlasticPackagingPage.type] =
    Arbitrary(ManufacturedPlasticPackagingPage)

  implicit lazy val arbitraryImportedPlasticPackagingWeightPage: Arbitrary[ImportedPlasticPackagingWeightPage.type] =
    Arbitrary(ImportedPlasticPackagingWeightPage)

  implicit lazy val arbitraryImportedPlasticPackagingPage: Arbitrary[ImportedPlasticPackagingPage.type] =
    Arbitrary(ImportedPlasticPackagingPage)

  implicit lazy val arbitraryExportedPlasticPackagingWeightPage: Arbitrary[DirectlyExportedWeightPage.type] =
    Arbitrary(DirectlyExportedWeightPage)

  implicit lazy val arbitraryAmendRecycledPlasticPackagingPage: Arbitrary[AmendRecycledPlasticPackagingPage.type] =
    Arbitrary(AmendRecycledPlasticPackagingPage)

  implicit lazy val arbitraryAmendManufacturedPlasticPackagingPage
    : Arbitrary[AmendManufacturedPlasticPackagingPage.type] =
    Arbitrary(AmendManufacturedPlasticPackagingPage)

  implicit lazy val arbitraryAmendImportedPlasticPackagingPage: Arbitrary[AmendImportedPlasticPackagingPage.type] =
    Arbitrary(AmendImportedPlasticPackagingPage)

  implicit lazy val arbitraryAmendHumanMedicinePlasticPackagingPage
    : Arbitrary[AmendHumanMedicinePlasticPackagingPage.type] =
    Arbitrary(AmendHumanMedicinePlasticPackagingPage)

  implicit lazy val arbitraryAmendDirectExportPlasticPackagingPage
    : Arbitrary[AmendDirectExportPlasticPackagingPage.type] =
    Arbitrary(AmendDirectExportPlasticPackagingPage)

}
