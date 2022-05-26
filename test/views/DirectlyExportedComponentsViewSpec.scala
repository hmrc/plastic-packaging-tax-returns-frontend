package views

import forms.DirectlyExportedComponentsFormProvider
import models.NormalMode
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{AnyContent, Request}
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.{FakeRequest, Injecting}
import play.twirl.api.Html
import views.html.DirectlyExportedComponentsView
import views.html.helper.form

class DirectlyExportedComponentsViewSpec extends PlaySpec with GuiceOneAppPerSuite with Injecting {

  private val realMessagesApi: MessagesApi = inject[MessagesApi]

  val request: Request[AnyContent] = FakeRequest().withCSRFToken

  implicit def messages: Messages =
    realMessagesApi.preferred(request)

  val form = Form[DirectlyExportedComponentsFormProvider]
  val page = inject[DirectlyExportedComponentsView]

  private def createView: Html =
    page(form,NormalMode)(request, messages)

  "DirectlyExportedComponentsView" should {
    "have a title" in {
      val view:Html = createView

    }

  }

}
