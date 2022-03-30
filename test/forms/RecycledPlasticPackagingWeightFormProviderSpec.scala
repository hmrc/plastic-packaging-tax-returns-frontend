package forms

import forms.behaviours.IntFieldBehaviours
import play.api.data.FormError

class RecycledPlasticPackagingWeightFormProviderSpec extends IntFieldBehaviours {

  val form = new RecycledPlasticPackagingWeightFormProvider()()

  ".value" - {

    val fieldName = "value"

    val minimum = 0
    val maximum = 9999999

    val validDataGenerator = intsInRangeWithCommas(minimum, maximum)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like intField(
      form,
      fieldName,
      nonNumericError  = FormError(fieldName, "recycledPlasticPackagingWeight.error.nonNumeric"),
      wholeNumberError = FormError(fieldName, "recycledPlasticPackagingWeight.error.wholeNumber")
    )

    behave like intFieldWithRange(
      form,
      fieldName,
      minimum       = minimum,
      maximum       = maximum,
      expectedError = FormError(fieldName, "recycledPlasticPackagingWeight.error.outOfRange", Seq(minimum, maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "recycledPlasticPackagingWeight.error.required")
    )
  }
}
