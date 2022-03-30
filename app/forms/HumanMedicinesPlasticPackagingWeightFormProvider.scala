package forms

import forms.mappings.Mappings
import javax.inject.Inject
import play.api.data.Form

class HumanMedicinesPlasticPackagingWeightFormProvider @Inject() extends Mappings {

  def apply(): Form[Int] =
    Form(
      "value" -> int(
        "humanMedicinesPlasticPackagingWeight.error.required",
        "humanMedicinesPlasticPackagingWeight.error.wholeNumber",
        "humanMedicinesPlasticPackagingWeight.error.nonNumeric")
          .verifying(inRange(0, 9999999, "humanMedicinesPlasticPackagingWeight.error.outOfRange"))
    )
}
