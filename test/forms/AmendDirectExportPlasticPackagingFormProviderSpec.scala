package forms

import forms.behaviours.IntFieldBehaviours
import play.api.data.FormError

class AmendDirectExportPlasticPackagingFormProviderSpec extends IntFieldBehaviours {

  val form = new AmendDirectExportPlasticPackagingFormProvider()()

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
      nonNumericError  = FormError(fieldName, "amendDirectExportPlasticPackaging.error.nonNumeric"),
      wholeNumberError = FormError(fieldName, "amendDirectExportPlasticPackaging.error.wholeNumber")
    )

    behave like intFieldWithRange(
      form,
      fieldName,
      minimum       = minimum,
      maximum       = maximum,
      expectedError = FormError(fieldName, "amendDirectExportPlasticPackaging.error.outOfRange", Seq(minimum, maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "amendDirectExportPlasticPackaging.error.required")
    )
  }
}
