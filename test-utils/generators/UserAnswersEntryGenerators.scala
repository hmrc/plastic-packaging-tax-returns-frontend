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

package generators

import models.changeGroupLead.NewGroupLeadAddressDetails
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import pages._
import pages.amends._
import pages.returns._
import pages.returns.credits._
import pages.changeGroupLead._
import play.api.libs.json.{JsValue, Json}

trait UserAnswersEntryGenerators extends PageGenerators with ModelGenerators {

  implicit lazy val arbitraryCancelCreditsClaimUserAnswersEntry: Arbitrary[(CancelCreditsClaimPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[CancelCreditsClaimPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryAmendExportedByAnotherBusinessUserAnswersEntry: Arbitrary[(AmendExportedByAnotherBusinessPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AmendExportedByAnotherBusinessPage.type]
        value <- arbitrary[Long].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryAnotherBusinessExportWeightUserAnswersEntry: Arbitrary[(AnotherBusinessExportedWeightPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AnotherBusinessExportedWeightPage.type]
        value <- arbitrary[Long].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryPlasticExportedByAnotherBusinessUserAnswersEntry: Arbitrary[(AnotherBusinessExportedPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AnotherBusinessExportedPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

    implicit lazy val arbitraryNewGroupLeadEnterContactAddressUserAnswersEntry: Arbitrary[(NewGroupLeadEnterContactAddressPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[NewGroupLeadEnterContactAddressPage.type]
        value <- arbitrary[NewGroupLeadAddressDetails].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryMainContactNameUserAnswersEntry: Arbitrary[(MainContactNamePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[MainContactNamePage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryMainContactJobTitleUserAnswersEntry: Arbitrary[(MainContactJobTitlePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[MainContactJobTitlePage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryExportedCreditsUserAnswersEntry: Arbitrary[(ExportedCreditsPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ExportedCreditsPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryConvertedCreditsUserAnswersEntry: Arbitrary[(OldConvertedCreditsPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[OldConvertedCreditsPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryNonExportedHumanMedicinesPlasticPackagingWeightUserAnswersEntry: Arbitrary[(NonExportedHumanMedicinesPlasticPackagingWeightPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[NonExportedHumanMedicinesPlasticPackagingWeightPage.type]
        value <- arbitrary[Long].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryNonExportedHumanMedicinesPlasticPackagingUserAnswersEntry: Arbitrary[(NonExportedHumanMedicinesPlasticPackagingPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[NonExportedHumanMedicinesPlasticPackagingPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryRecycledPlasticPackagingUserAnswersEntry: Arbitrary[(NonExportedRecycledPlasticPackagingPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[NonExportedRecycledPlasticPackagingPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryDirectlyExportedComponentsUserAnswersEntry: Arbitrary[(DirectlyExportedPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[DirectlyExportedPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryManufacturedPlasticPackagingWeightUserAnswersEntry: Arbitrary[(ManufacturedPlasticPackagingWeightPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ManufacturedPlasticPackagingWeightPage.type]
        value <- arbitrary[Long].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryAgentsUserAnswersEntry: Arbitrary[(AgentsPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AgentsPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryStartYourReturnUserAnswersEntry: Arbitrary[(StartYourReturnPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[StartYourReturnPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryRecycledPlasticPackagingWeightUserAnswersEntry
    : Arbitrary[(NonExportedRecycledPlasticPackagingWeightPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[NonExportedRecycledPlasticPackagingWeightPage.type]
        value <- arbitrary[Int].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryManufacturedPlasticPackagingUserAnswersEntry
    : Arbitrary[(ManufacturedPlasticPackagingPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ManufacturedPlasticPackagingPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryImportedPlasticPackagingWeightUserAnswersEntry
    : Arbitrary[(ImportedPlasticPackagingWeightPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ImportedPlasticPackagingWeightPage.type]
        value <- arbitrary[Int].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryImportedPlasticPackagingUserAnswersEntry
    : Arbitrary[(ImportedPlasticPackagingPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ImportedPlasticPackagingPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryExportedPlasticPackagingWeightUserAnswersEntry
    : Arbitrary[(DirectlyExportedWeightPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[DirectlyExportedWeightPage.type]
        value <- arbitrary[Int].map(Json.toJson(_))
      } yield (page, value)
    }


  implicit lazy val arbitraryAmendRecycledPlasticPackagingUserAnswersEntry
    : Arbitrary[(AmendRecycledPlasticPackagingPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AmendRecycledPlasticPackagingPage.type]
        value <- arbitrary[Int].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryAmendManufacturedPlasticPackagingUserAnswersEntry
    : Arbitrary[(AmendManufacturedPlasticPackagingPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AmendManufacturedPlasticPackagingPage.type]
        value <- arbitrary[Int].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryAmendImportedPlasticPackagingUserAnswersEntry
    : Arbitrary[(AmendImportedPlasticPackagingPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AmendImportedPlasticPackagingPage.type]
        value <- arbitrary[Int].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryAmendHumanMedicinePlasticPackagingUserAnswersEntry
    : Arbitrary[(AmendHumanMedicinePlasticPackagingPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AmendHumanMedicinePlasticPackagingPage.type]
        value <- arbitrary[Int].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryAmendDirectExportPlasticPackagingUserAnswersEntry
    : Arbitrary[(AmendDirectExportPlasticPackagingPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AmendDirectExportPlasticPackagingPage.type]
        value <- arbitrary[Int].map(Json.toJson(_))
      } yield (page, value)
    }

}
