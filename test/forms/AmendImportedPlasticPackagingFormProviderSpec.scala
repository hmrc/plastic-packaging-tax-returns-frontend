package forms

import forms.behaviours.IntFieldBehaviours
import play.api.data.FormError

class AmendImportedPlasticPackagingFormProviderSpec extends IntFieldBehaviours {

  val form = new AmendImportedPlasticPackagingFormProvider()()

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
      nonNumericError  = FormError(fieldName, "amendImportedPlasticPackaging.error.nonNumeric"),
      wholeNumberError = FormError(fieldName, "amendImportedPlasticPackaging.error.wholeNumber")
    )

    behave like intFieldWithRange(
      form,
      fieldName,
      minimum       = minimum,
      maximum       = maximum,
      expectedError = FormError(fieldName, "amendImportedPlasticPackaging.error.outOfRange", Seq(minimum, maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "amendImportedPlasticPackaging.error.required")
    )
  }
}
