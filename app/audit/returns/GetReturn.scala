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

import models.returns.ReturnDisplayApi
import play.api.libs.json.{Json, OFormat}

case class GetReturn(internalId: String,
                     periodKey: String,
                     result: String,
                     response: Option[ReturnDisplayApi],
                     error: Option[String],
                     headers: Seq[(String, String)])

object GetReturn {
  implicit val format: OFormat[GetReturn] = Json.format[GetReturn]
  val eventType: String                   = "GetReturn"
}




