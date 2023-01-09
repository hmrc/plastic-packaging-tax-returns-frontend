package forms

import forms.behaviours.LongFieldBehaviours
import play.api.data.FormError

class $className$FormProviderSpec extends LongFieldBehaviours {

  val form = new $className$FormProvider()()

  ".value" - {

    val fieldName = "value"

    val minimum = $minimum$L
    val maximum = $maximum$L

    val validDataGenerator = longsInRangeWithCommas(minimum, maximum)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like longField(
      form,
      fieldName,
      nonNumericError  = FormError(fieldName, "$className;format="decap"$.error.nonNumeric"),
      wholeNumberError = FormError(fieldName, "$className;format="decap"$.error.wholeNumber")
    )

    behave like longFieldWithRange(
      form,
      fieldName,
      minimum       = minimum,
      maximum       = maximum,
      expectedError = FormError(fieldName, "$className;format="decap"$.error.outOfRange", Seq(minimum, maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "$className;format="decap"$.error.required")
    )
  }
}
