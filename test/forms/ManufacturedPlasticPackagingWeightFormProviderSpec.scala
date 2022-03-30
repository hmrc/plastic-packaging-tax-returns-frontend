package forms

import forms.behaviours.IntFieldBehaviours
import play.api.data.FormError

class ManufacturedPlasticPackagingWeightFormProviderSpec extends IntFieldBehaviours {

  val form = new ManufacturedPlasticPackagingWeightFormProvider()()

  ".value" - {

    val fieldName = "value"

    val minimum = 0
    val maximum = 999999

    val validDataGenerator = intsInRangeWithCommas(minimum, maximum)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like intField(
      form,
      fieldName,
      nonNumericError  = FormError(fieldName, "manufacturedPlasticPackagingWeight.error.nonNumeric"),
      wholeNumberError = FormError(fieldName, "manufacturedPlasticPackagingWeight.error.wholeNumber")
    )

    behave like intFieldWithRange(
      form,
      fieldName,
      minimum       = minimum,
      maximum       = maximum,
      expectedError = FormError(fieldName, "manufacturedPlasticPackagingWeight.error.outOfRange", Seq(minimum, maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "manufacturedPlasticPackagingWeight.error.required")
    )
  }
}
