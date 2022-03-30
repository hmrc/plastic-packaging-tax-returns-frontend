package forms

import forms.behaviours.IntFieldBehaviours
import play.api.data.FormError

class HumanMedicinesPlasticPackagingWeightFormProviderSpec extends IntFieldBehaviours {

  val form = new HumanMedicinesPlasticPackagingWeightFormProvider()()

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
      nonNumericError  = FormError(fieldName, "humanMedicinesPlasticPackagingWeight.error.nonNumeric"),
      wholeNumberError = FormError(fieldName, "humanMedicinesPlasticPackagingWeight.error.wholeNumber")
    )

    behave like intFieldWithRange(
      form,
      fieldName,
      minimum       = minimum,
      maximum       = maximum,
      expectedError = FormError(fieldName, "humanMedicinesPlasticPackagingWeight.error.outOfRange", Seq(minimum, maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "humanMedicinesPlasticPackagingWeight.error.required")
    )
  }
}
