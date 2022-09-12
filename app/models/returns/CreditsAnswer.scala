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

import play.api.libs.json.{Json, OFormat}

case class CreditsAnswer(yesNo: Boolean, weight: Option[Long]) {
  def value: Long = (yesNo, weight) match {
    case (true, Some(x)) => x
    case (true, None) => 0
    case (false, _) => 0

  }

}

object CreditsAnswer {

  implicit val formats: OFormat[CreditsAnswer] = Json.format[CreditsAnswer]
}
