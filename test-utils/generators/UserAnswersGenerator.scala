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

import models.UserAnswers
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.TryValues
import pages._
import pages.amends._
import pages.changeGroupLead._
import pages.returns._
import play.api.libs.json.{JsValue, Json}

trait UserAnswersGenerator extends TryValues {
  self: Generators =>

  val generators: Seq[Gen[(QuestionPage[_], JsValue)]] =
    arbitrary[(AmendExportedByAnotherBusinessPage.type, JsValue)] ::
      arbitrary[(AnotherBusinessExportedWeightPage.type, JsValue)] ::
      arbitrary[(AnotherBusinessExportedPage.type, JsValue)] ::
      arbitrary[(NewGroupLeadEnterContactAddressPage.type, JsValue)] ::
      arbitrary[(MainContactNamePage.type, JsValue)] ::
      arbitrary[(MainContactJobTitlePage.type, JsValue)] ::
      arbitrary[(NonExportedHumanMedicinesPlasticPackagingWeightPage.type, JsValue)] ::
      arbitrary[(NonExportedHumanMedicinesPlasticPackagingPage.type, JsValue)] ::
      arbitrary[(NonExportedRecycledPlasticPackagingPage.type, JsValue)] ::
      arbitrary[(DirectlyExportedWeightPage.type, JsValue)] ::
      arbitrary[(ManufacturedPlasticPackagingWeightPage.type, JsValue)] ::
      arbitrary[(DirectlyExportedPage.type, JsValue)] ::
      arbitrary[(AgentsPage.type, JsValue)] ::
      arbitrary[(StartYourReturnPage.type, JsValue)] ::
      arbitrary[(NonExportedRecycledPlasticPackagingWeightPage.type, JsValue)] ::
      arbitrary[(ManufacturedPlasticPackagingWeightPage.type, JsValue)] ::
      arbitrary[(ManufacturedPlasticPackagingPage.type, JsValue)] ::
      arbitrary[(ImportedPlasticPackagingWeightPage.type, JsValue)] ::
      arbitrary[(ImportedPlasticPackagingPage.type, JsValue)] ::
      arbitrary[(DirectlyExportedWeightPage.type, JsValue)] ::
      arbitrary[(AmendRecycledPlasticPackagingPage.type, JsValue)] ::
      arbitrary[(AmendManufacturedPlasticPackagingPage.type, JsValue)] ::
      arbitrary[(AmendImportedPlasticPackagingPage.type, JsValue)] ::
      arbitrary[(AmendHumanMedicinePlasticPackagingPage.type, JsValue)] ::
      arbitrary[(AmendDirectExportPlasticPackagingPage.type, JsValue)] ::
      Nil

  implicit lazy val arbitraryUserData: Arbitrary[UserAnswers] = {

    import models._

    Arbitrary {
      for {
        id <- nonEmptyString
        data <- generators match {
          case Nil => Gen.const(Map[QuestionPage[_], JsValue]())
          case _   => Gen.mapOf(oneOf(generators))
        }
      } yield UserAnswers(
        id = id,
        data = data.foldLeft(Json.obj()) { case (obj, (path, value)) =>
          obj.setObject(path.path, value).get
        }
      )
    }
  }

}
