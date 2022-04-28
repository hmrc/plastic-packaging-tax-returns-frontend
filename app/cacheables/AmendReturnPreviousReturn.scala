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

package cacheables

import models.returns.ReturnDisplayApi
import pages.{
  AmendDirectExportPlasticPackagingPage,
  AmendHumanMedicinePlasticPackagingPage,
  AmendImportedPlasticPackagingPage,
  AmendManufacturedPlasticPackagingPage,
  AmendRecycledPlasticPackagingPage
}
import play.api.libs.json.{JsObject, JsPath, Json, Writes}
import queries.{Gettable, Settable}

case object AmendReturnPreviousReturn
    extends Gettable[ReturnDisplayApi] with Settable[ReturnDisplayApi] {
  override def path: JsPath = JsPath \ "amend"

  override def toString: String = "amendReturnPreviousReturn"

  val returnDisplayApiWrites: Writes[ReturnDisplayApi] = new Writes[ReturnDisplayApi] {

    def writes(display: ReturnDisplayApi): JsObject =
      Json.obj(
        AmendManufacturedPlasticPackagingPage.toString  -> display.returnDetails.manufacturedWeight,
        AmendImportedPlasticPackagingPage.toString      -> display.returnDetails.importedWeight,
        AmendHumanMedicinePlasticPackagingPage.toString -> display.returnDetails.humanMedicines,
        AmendDirectExportPlasticPackagingPage.toString  -> display.returnDetails.directExports,
        AmendRecycledPlasticPackagingPage.toString      -> display.returnDetails.recycledPlastic
      )

  }

}
