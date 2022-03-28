package forms

import forms.mappings.Mappings
import javax.inject.Inject
import play.api.data.Form

class AmendManufacturedPlasticPackagingFormProvider @Inject() extends Mappings {

  def apply(): Form[Int] =
    Form(
      "value" -> int(
        "amendManufacturedPlasticPackaging.error.required",
        "amendManufacturedPlasticPackaging.error.wholeNumber",
        "amendManufacturedPlasticPackaging.error.nonNumeric")
          .verifying(inRange(0, 99999999, "amendManufacturedPlasticPackaging.error.outOfRange"))
    )
}
