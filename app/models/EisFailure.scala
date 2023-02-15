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

package models

import play.api.libs.json.{Json, OFormat}

case class EisError(code: String, reason: String)

object EisError {
  implicit val format: OFormat[EisError] = Json.format[EisError]
}

case class EisFailure(failures: Option[Seq[EisError]]) {

  /**
    * @return true when response can be inferred to mean ppt account has been de-registered
    * @note only correct for responses from the view subscription api
    */
  def isDeregistered: Boolean =
    failures.exists(_.exists(_.code == "NO_DATA_FOUND"))

  /**
    * @return true if a reason matches that expected from a bad-gateway or service-unavailable failure 
    * @note intended for use on response from view subscription api, but may also work for others - check the specs
    */
  def isDependentSystemsNotResponding: Boolean =
    failures.exists(_.exists(_.reason == "Dependent systems are currently not responding."))
}

object EisFailure {
  implicit val format: OFormat[EisFailure] = Json.format[EisFailure]
}
