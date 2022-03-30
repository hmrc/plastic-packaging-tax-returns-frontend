package forms

import forms.mappings.Mappings
import javax.inject.Inject
import play.api.data.Form

class RecycledPlasticPackagingWeightFormProvider @Inject() extends Mappings {

  def apply(): Form[Int] =
    Form(
      "value" -> int(
        "recycledPlasticPackagingWeight.error.required",
        "recycledPlasticPackagingWeight.error.wholeNumber",
        "recycledPlasticPackagingWeight.error.nonNumeric")
          .verifying(inRange(0, 9999999, "recycledPlasticPackagingWeight.error.outOfRange"))
    )
}
