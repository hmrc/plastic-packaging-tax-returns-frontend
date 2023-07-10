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

package forms.returns.credits

import forms.mappings.Mappings
import models.returns.CreditRangeOption
import play.api.data.Form
import play.api.i18n.Messages
import views.ViewUtils

class CreditsClaimedListFormProvider extends Mappings {

  val standardError = "creditsSummary.error.required"
  val onlyOneRemainingError = "creditsSummary.error.required.one-remaining"

  def apply(options: Seq[CreditRangeOption])(implicit messages: Messages): Form[Boolean] = {
    Form(
      options match {
        case Seq(onlyOption) => "value" -> boolean(onlyOneRemainingError, args = Seq(ViewUtils.displayDateRangeTo(onlyOption.from, onlyOption.to)))
        case _ => "value" -> boolean(standardError)
      }
    )
  }
}