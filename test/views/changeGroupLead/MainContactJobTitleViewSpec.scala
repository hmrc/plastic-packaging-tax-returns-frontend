package views.changeGroupLead

import base.ViewSpecBase
import forms.changeGroupLead.MainContactJobTitleFormProvider
import models.Mode.NormalMode
import play.api.data.Form
import play.twirl.api.Html
import support.{ViewAssertions, ViewMatchers}
import views.html.changeGroupLead.MainContactJobTitleView


class MainContactJobTitleViewSpec extends ViewSpecBase  with ViewAssertions with ViewMatchers  {
 private val page = inject[MainContactJobTitleView]

  val form: Form[String] = new MainContactJobTitleFormProvider()()

  private def createView: Html =
    page(form,"contact-name", NormalMode)(request, messages)

  "MainContactJobTitleView" should {

    "have a title" in {
      createView.select("title").text must startWith ("What is the main contact’s job title?")
    }
  }

}
