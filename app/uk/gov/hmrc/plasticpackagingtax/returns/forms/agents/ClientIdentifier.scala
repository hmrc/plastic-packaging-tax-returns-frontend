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

package uk.gov.hmrc.plasticpackagingtax.returns.forms.agents

import play.api.data.{Form, Mapping}
import play.api.data.Forms.{mapping, text}
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.plasticpackagingtax.returns.forms.CommonFormValidators

case class ClientIdentifier(identifier: String)

object ClientIdentifier extends CommonFormValidators {

  implicit val format: OFormat[ClientIdentifier] = Json.format[ClientIdentifier]

  val identifier           = "identifier"
  val identifierEmptyError = "agents.client.identifier.empty.error"

  def form(): Form[ClientIdentifier] = {
    val m: Mapping[ClientIdentifier] = mapping(
      identifier -> text()
        .verifying(identifierEmptyError, isNonEmpty)
    )(ClientIdentifier.apply)(ClientIdentifier.unapply)

    Form(mapping = m)
  }

}
