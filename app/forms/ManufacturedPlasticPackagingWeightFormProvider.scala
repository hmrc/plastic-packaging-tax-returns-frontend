package forms

import forms.mappings.Mappings
import javax.inject.Inject
import play.api.data.Form

class ManufacturedPlasticPackagingWeightFormProvider @Inject() extends Mappings {

  def apply(): Form[Int] =
    Form(
      "value" -> int(
        "manufacturedPlasticPackagingWeight.error.required",
        "manufacturedPlasticPackagingWeight.error.wholeNumber",
        "manufacturedPlasticPackagingWeight.error.nonNumeric")
          .verifying(inRange(0, 999999, "manufacturedPlasticPackagingWeight.error.outOfRange"))
    )
}
