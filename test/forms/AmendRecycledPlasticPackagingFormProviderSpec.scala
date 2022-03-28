package forms

import forms.behaviours.IntFieldBehaviours
import play.api.data.FormError

class AmendRecycledPlasticPackagingFormProviderSpec extends IntFieldBehaviours {

  val form = new AmendRecycledPlasticPackagingFormProvider()()

  ".value" - {

    val fieldName = "value"

    val minimum = 0
    val maximum = 99999999

    val validDataGenerator = intsInRangeWithCommas(minimum, maximum)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like intField(
      form,
      fieldName,
      nonNumericError  = FormError(fieldName, "amendRecycledPlasticPackaging.error.nonNumeric"),
      wholeNumberError = FormError(fieldName, "amendRecycledPlasticPackaging.error.wholeNumber")
    )

    behave like intFieldWithRange(
      form,
      fieldName,
      minimum       = minimum,
      maximum       = maximum,
      expectedError = FormError(fieldName, "amendRecycledPlasticPackaging.error.outOfRange", Seq(minimum, maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "amendRecycledPlasticPackaging.error.required")
    )
  }
}
