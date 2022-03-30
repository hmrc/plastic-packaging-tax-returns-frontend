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

import models._
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import pages._
import play.api.libs.json.{JsValue, Json}

trait UserAnswersEntryGenerators extends PageGenerators with ModelGenerators {

  implicit lazy val arbitraryConvertedPackagingCreditUserAnswersEntry
    : Arbitrary[(ConvertedPackagingCreditPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ConvertedPackagingCreditPage.type]
        value <- arbitrary[Int].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryRecycledPlasticPackagingWeightUserAnswersEntry
    : Arbitrary[(RecycledPlasticPackagingWeightPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[RecycledPlasticPackagingWeightPage.type]
        value <- arbitrary[Int].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryManufacturedPlasticPackagingWeightUserAnswersEntry
    : Arbitrary[(ManufacturedPlasticPackagingWeightPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ManufacturedPlasticPackagingWeightPage.type]
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

  implicit lazy val arbitraryHumanMedicinesPlasticPackagingWeightUserAnswersEntry
    : Arbitrary[(HumanMedicinesPlasticPackagingWeightPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[HumanMedicinesPlasticPackagingWeightPage.type]
        value <- arbitrary[Int].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryExportedPlasticPackagingWeightUserAnswersEntry
    : Arbitrary[(ExportedPlasticPackagingWeightPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ExportedPlasticPackagingWeightPage.type]
        value <- arbitrary[Int].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryAmendAreYouSureUserAnswersEntry
    : Arbitrary[(AmendAreYouSurePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AmendAreYouSurePage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
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
