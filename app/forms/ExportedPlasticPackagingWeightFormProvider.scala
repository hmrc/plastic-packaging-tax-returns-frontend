package forms

import forms.mappings.Mappings
import javax.inject.Inject
import play.api.data.Form

class ExportedPlasticPackagingWeightFormProvider @Inject() extends Mappings {

  def apply(): Form[Int] =
    Form(
      "value" -> int(
        "exportedPlasticPackagingWeight.error.required",
        "exportedPlasticPackagingWeight.error.wholeNumber",
        "exportedPlasticPackagingWeight.error.nonNumeric")
          .verifying(inRange(0, 9999999, "exportedPlasticPackagingWeight.error.outOfRange"))
    )
}
