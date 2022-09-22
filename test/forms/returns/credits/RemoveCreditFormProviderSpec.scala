package forms.returns.credits

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class RemoveCreditFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "removeCredit.error.required"
  val invalidKey = "error.boolean"

  val form = new RemoveCreditFormProvider()()

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
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
