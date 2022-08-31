package views.returns.credits

import base.ViewSpecBase
import forms.returns.credits.ExportedCreditsFormProvider
import models.Mode.NormalMode
import play.api.data.Form
import play.twirl.api.Html
import views.html.returns.credits.ExportedCreditsView

class ExportedCreditsViewSpec extends ViewSpecBase {

  val page: ExportedCreditsView = inject[ExportedCreditsView]
  val form = new ExportedCreditsFormProvider()

  private def createView(form: Form[ExportedCreditsFormProvider]): Html =
    page(form, NormalMode)(request, messages)

}
