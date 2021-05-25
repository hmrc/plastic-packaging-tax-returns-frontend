/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.plasticpackagingtax.returns.audit

import play.api.libs.json.{Format, Json, OFormat, Reads, Writes}
import uk.gov.hmrc.plasticpackagingtax.returns.audit.UserType.UserType

object UserType extends Enumeration {
  type UserType = Value
  val NEW, RETURNING = Value

  implicit val format: Format[UserType] =
    Format(Reads.enumNameReads(UserType), Writes.enumNameWrites)

}

case class StartTaxReturnEvent(userType: UserType) {}

object StartTaxReturnEvent {
  implicit val format: OFormat[StartTaxReturnEvent] = Json.format[StartTaxReturnEvent]
  val eventType: String                             = "START_PPT_TAX_RETURN"
}
