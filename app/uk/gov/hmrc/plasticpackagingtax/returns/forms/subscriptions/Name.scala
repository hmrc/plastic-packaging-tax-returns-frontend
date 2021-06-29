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

package uk.gov.hmrc.plasticpackagingtax.returns.forms.subscriptions

import play.api.data.Forms.text
import play.api.data.{Form, Forms, Mapping}
import uk.gov.hmrc.plasticpackagingtax.returns.forms.CommonFormValidators

case class Name(value: String)

object Name extends CommonFormValidators {

  val emptyError  = "subscription.primaryContactDetails.name.error.empty"
  val lengthError = "subscription.primaryContactDetails.name.error.length"
  val formatError = "subscription.primaryContactDetails.name.error.format"

  val value = "value"

  private val mapping: Mapping[Name] = Forms.mapping(
    value ->
      text()
        .verifying(emptyError, isNonEmpty)
        .verifying(lengthError, isNotExceedingMaxLength(_, 160))
        .verifying(formatError, isValidName)
  )(Name.apply)(Name.unapply)

  def form(): Form[Name] = Form(mapping)
}
