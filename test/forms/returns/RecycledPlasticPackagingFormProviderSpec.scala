package forms.returns

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class RecycledPlasticPackagingFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "recycledPlasticPackaging.error.required"
  val invalidKey = "error.boolean"

  val form = new RecycledPlasticPackagingFormProvider()()

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
