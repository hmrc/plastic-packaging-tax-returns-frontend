package views.amends

import base.ViewSpecBase
import forms.amends.CancelAmendFormProvider
import models.returns.TaxReturnObligation
import play.twirl.api.Html
import uk.gov.hmrc.scalatestaccessibilitylinter.AccessibilityMatchers
import views.html.amends.CancelAmendView

import java.time.LocalDate

class CancelAmendViewA11ySpec extends ViewSpecBase with AccessibilityMatchers {

  val aTaxObligation: TaxReturnObligation = TaxReturnObligation(
    LocalDate.now(),
    LocalDate.now().plusWeeks(12),
    LocalDate.now().plusWeeks(16),
    "PK1")

  "CancelAmendView" should {
    "pass accessibility checks" in {
      val form = new CancelAmendFormProvider()()
      val page = inject[CancelAmendView]

      def render: Html =
        page(form, aTaxObligation)(request, messages)

      render.toString() must passAccessibilityChecks
    }
  }

}
