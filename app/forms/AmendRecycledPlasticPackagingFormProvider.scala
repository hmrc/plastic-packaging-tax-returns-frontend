package forms

import forms.mappings.Mappings
import javax.inject.Inject
import play.api.data.Form

class AmendRecycledPlasticPackagingFormProvider @Inject() extends Mappings {

  def apply(): Form[Int] =
    Form(
      "value" -> int(
        "amendRecycledPlasticPackaging.error.required",
        "amendRecycledPlasticPackaging.error.wholeNumber",
        "amendRecycledPlasticPackaging.error.nonNumeric")
          .verifying(inRange(0, 99999999, "amendRecycledPlasticPackaging.error.outOfRange"))
    )
}
