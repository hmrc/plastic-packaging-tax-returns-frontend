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

import forms.behaviours.BooleanFieldBehaviours
import models.returns.CreditRangeOption
import org.mockito.MockitoSugar.mock
import play.api.data.FormError
import play.api.i18n.Messages
import views.ViewUtils

import java.time.LocalDate

class CreditsClaimedListFormProviderSpec extends BooleanFieldBehaviours {

  val invalidKey = "error.boolean"

  val mockMessages = mock[Messages]
  val cro = CreditRangeOption(LocalDate.now(), LocalDate.now())

  "form when" - {
    "one CreditRangeOption available" - {
      val onlyOneRemainingError = "creditsSummary.error.required.one-remaining"
      val form = new CreditsClaimedListFormProvider().apply(Seq.fill(1)(cro))(mockMessages)
      ".value" - {

        val fieldName = "value"

        val args = Seq(ViewUtils.displayDateRangeTo(cro.from, cro.to)(mockMessages))

        behave like booleanField(
          form,
          fieldName,
          invalidError = FormError(fieldName, invalidKey, args)
        )

        behave like mandatoryField(
          form,
          fieldName,
          requiredError = FormError(fieldName, onlyOneRemainingError, args)
        )
      }
    }

    "multiple CreditRangeOption available" - {
      val standardError = "creditsSummary.error.required"
      val form = new CreditsClaimedListFormProvider().apply(Seq.fill(2)(cro))(mockMessages)
      ".value" - {

        val fieldName = "value"

        behave like booleanField(
          form,
          fieldName,
          invalidError = FormError(fieldName, invalidKey)
        )

        behave like mandatoryField(
          form,
          fieldName,
          requiredError = FormError(fieldName, standardError)
        )
      }
    }
  }


}
