package forms

import forms.mappings.Mappings
import javax.inject.Inject
import play.api.data.Form

class ImportedPlasticPackagingWeightFormProvider @Inject() extends Mappings {

  def apply(): Form[Int] =
    Form(
      "value" -> int(
        "importedPlasticPackagingWeight.error.required",
        "importedPlasticPackagingWeight.error.wholeNumber",
        "importedPlasticPackagingWeight.error.nonNumeric")
          .verifying(inRange(0, 9999999, "importedPlasticPackagingWeight.error.outOfRange"))
    )
}
