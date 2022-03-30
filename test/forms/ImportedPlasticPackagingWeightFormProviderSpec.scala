package forms

import forms.behaviours.IntFieldBehaviours
import play.api.data.FormError

class ImportedPlasticPackagingWeightFormProviderSpec extends IntFieldBehaviours {

  val form = new ImportedPlasticPackagingWeightFormProvider()()

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
      nonNumericError  = FormError(fieldName, "importedPlasticPackagingWeight.error.nonNumeric"),
      wholeNumberError = FormError(fieldName, "importedPlasticPackagingWeight.error.wholeNumber")
    )

    behave like intFieldWithRange(
      form,
      fieldName,
      minimum       = minimum,
      maximum       = maximum,
      expectedError = FormError(fieldName, "importedPlasticPackagingWeight.error.outOfRange", Seq(minimum, maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "importedPlasticPackagingWeight.error.required")
    )
  }
}
