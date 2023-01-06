package forms

import forms.behaviours.IntFieldBehaviours
import play.api.data.FormError

class AmendExportedWeightFormProviderSpec extends IntFieldBehaviours {

  val form = new AmendExportedWeightFormProvider()()

  ".value" - {

    val fieldName = "value"

    val minimum = 0
    val maximum = 99999999999

    val validDataGenerator = intsInRangeWithCommas(minimum, maximum)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like intField(
      form,
      fieldName,
      nonNumericError  = FormError(fieldName, "amendExportedWeight.error.nonNumeric"),
      wholeNumberError = FormError(fieldName, "amendExportedWeight.error.wholeNumber")
    )

    behave like intFieldWithRange(
      form,
      fieldName,
      minimum       = minimum,
      maximum       = maximum,
      expectedError = FormError(fieldName, "amendExportedWeight.error.outOfRange", Seq(minimum, maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "amendExportedWeight.error.required")
    )
  }
}
