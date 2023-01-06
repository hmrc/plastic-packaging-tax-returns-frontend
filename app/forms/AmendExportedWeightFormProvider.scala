package forms

import forms.mappings.Mappings
import javax.inject.Inject
import play.api.data.Form

class AmendExportedWeightFormProvider @Inject() extends Mappings {

  def apply(): Form[Long] =
    Form(
      "value" -> long(
        "amendExportedWeight.error.required",
        "amendExportedWeight.error.wholeNumber",
        "amendExportedWeight.error.nonNumeric")
          .verifying(inRange(0, 99999999999, "amendExportedWeight.error.outOfRange"""))
    )
}
