package forms

import forms.mappings.Mappings
import javax.inject.Inject
import play.api.data.Form

class AmendImportedPlasticPackagingFormProvider @Inject() extends Mappings {

  def apply(): Form[Int] =
    Form(
      "value" -> int(
        "amendImportedPlasticPackaging.error.required",
        "amendImportedPlasticPackaging.error.wholeNumber",
        "amendImportedPlasticPackaging.error.nonNumeric")
          .verifying(inRange(0, 99999999, "amendImportedPlasticPackaging.error.outOfRange"))
    )
}
