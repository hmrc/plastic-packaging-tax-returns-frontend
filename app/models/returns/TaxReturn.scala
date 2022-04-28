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

package models.returns

import models.returns.ReturnType.ReturnType
import org.joda.time.{DateTime, DateTimeZone}
import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

final case class TaxReturnObligation(
  fromDate: LocalDate,
  toDate: LocalDate,
  dueDate: LocalDate,
  periodKey: String
)

object TaxReturnObligation {
  implicit val format: OFormat[TaxReturnObligation] = Json.format[TaxReturnObligation]
}

case class TaxReturn(
  id: String,
  returnType: Option[ReturnType] = Some(ReturnType.NEW),
  periodKey: String,
  manufacturedPlastic: Option[Boolean] = None,
  manufacturedPlasticWeight: Option[ManufacturedPlasticWeight] = None,
  importedPlastic: Option[Boolean] = None,
  importedPlasticWeight: Option[ImportedPlasticWeight] = None,
  humanMedicinesPlasticWeight: Option[HumanMedicinesPlasticWeight] = None,
  exportedPlasticWeight: Option[ExportedPlasticWeight] = None,
  convertedPackagingCredit: Option[ConvertedPackagingCredit] = None,
  recycledPlasticWeight: Option[RecycledPlasticWeight] = None,
  lastModifiedDateTime: Option[DateTime] = None
) {

  def updateLastModified(): TaxReturn =
    this.copy(lastModifiedDateTime = Some(DateTime.now(DateTimeZone.UTC)))

}

object TaxReturn {

  import play.api.libs.json._

  implicit val dateFormatDefault: Format[DateTime] = new Format[DateTime] {

    override def reads(json: JsValue): JsResult[DateTime] =
      JodaReads.DefaultJodaDateTimeReads.reads(json)

    override def writes(o: DateTime): JsValue = JodaWrites.JodaDateTimeNumberWrites.writes(o)
  }

  implicit val format: OFormat[TaxReturn] = Json.format[TaxReturn]
}
