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

package forms.agents

import forms.CommonFormValidators
import play.api.data.Form
import play.api.data.Forms.{mapping, text}
import play.api.libs.json.{Json, OFormat}

import java.util.regex.Pattern

case class ClientIdentifier(identifier: String)

object ClientIdentifier extends CommonFormValidators {

  implicit val format: OFormat[ClientIdentifier] = Json.format[ClientIdentifier]

  val identifier           = "identifier"
  val identifierEmptyError = "agents.client.identifier.empty.error"
  val formatError          = "agents.client.identifier.format.error"
  val lengthError          = "agents.client.identifier.length.error"

  private val validFormatPattern = Pattern.compile("^X[A-Z]PPT000[0-9]{7}$")

  def form(): Form[ClientIdentifier] =
    Form(mapping =
      mapping(
        identifier -> text()
          .verifying(identifierEmptyError, isNonEmpty)
          .verifying(lengthError, isLength(_, 15))
          .verifying(formatError, isMatchingPattern(_, validFormatPattern))
      )(ClientIdentifier.apply)(ClientIdentifier.unapply)
    )

}
