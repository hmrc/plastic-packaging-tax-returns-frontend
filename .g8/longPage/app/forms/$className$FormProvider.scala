package forms

import forms.mappings.Mappings
import javax.inject.Inject
import play.api.data.Form

class $className$FormProvider @Inject() extends Mappings {

  def apply(): Form[Long] =
    Form(
      "value" -> long(
        "$className;format="decap"$.error.required",
        "$className;format="decap"$.error.wholeNumber",
        "$className;format="decap"$.error.nonNumeric")
          .verifying(inRange($minimum$L, $maximum$L, "$className;format="decap"$.error.outOfRange"))
    )
}
