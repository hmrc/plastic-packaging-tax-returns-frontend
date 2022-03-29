package forms

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class ConfirmAmendReturnFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "confirmAmendReturn.error.required"
  val invalidKey = "error.boolean"

  val form = new ConfirmAmendReturnFormProvider()()

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
