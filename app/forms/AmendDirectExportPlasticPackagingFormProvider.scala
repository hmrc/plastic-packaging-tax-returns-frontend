package forms

import forms.mappings.Mappings
import javax.inject.Inject
import play.api.data.Form

class AmendDirectExportPlasticPackagingFormProvider @Inject() extends Mappings {

  def apply(): Form[Int] =
    Form(
      "value" -> int(
        "amendDirectExportPlasticPackaging.error.required",
        "amendDirectExportPlasticPackaging.error.wholeNumber",
        "amendDirectExportPlasticPackaging.error.nonNumeric")
          .verifying(inRange(0, 99999999, "amendDirectExportPlasticPackaging.error.outOfRange"))
    )
}
