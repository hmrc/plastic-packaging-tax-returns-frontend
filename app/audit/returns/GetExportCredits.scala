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

package audit.returns

import models.ExportCreditBalance
import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

case class GetExportCredits(internalId: String,
                            pptReference: String,
                            fromDate: LocalDate,
                            toDate: LocalDate,
                            result: String,
                            response: Option[ExportCreditBalance],
                            error: Option[String],
                            headers: Seq[(String, String)])

object GetExportCredits {
  implicit val format: OFormat[GetExportCredits] = Json.format[GetExportCredits]
  val eventType: String                          = "GetExportCredits"
}






