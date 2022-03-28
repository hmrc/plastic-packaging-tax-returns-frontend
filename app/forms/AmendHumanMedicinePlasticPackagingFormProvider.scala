package forms

import forms.mappings.Mappings
import javax.inject.Inject
import play.api.data.Form

class AmendHumanMedicinePlasticPackagingFormProvider @Inject() extends Mappings {

  def apply(): Form[Int] =
    Form(
      "value" -> int(
        "amendHumanMedicinePlasticPackaging.error.required",
        "amendHumanMedicinePlasticPackaging.error.wholeNumber",
        "amendHumanMedicinePlasticPackaging.error.nonNumeric")
          .verifying(inRange(0, 99999999, "amendHumanMedicinePlasticPackaging.error.outOfRange"))
    )
}
