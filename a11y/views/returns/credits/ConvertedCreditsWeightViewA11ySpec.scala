package views.returns.credits

import base.ViewSpecBase
import forms.returns.credits.ConvertedCreditsWeightFormProvider
import models.returns.CreditRangeOption
import play.api.data.Form
import play.api.mvc.Call
import uk.gov.hmrc.scalatestaccessibilitylinter.AccessibilityMatchers
import views.html.returns.credits.ConvertedCreditsWeightView

import java.time.LocalDate

class ConvertedCreditsWeightViewA11ySpec extends ViewSpecBase with AccessibilityMatchers{

  val form = new ConvertedCreditsWeightFormProvider()()
  val page = inject[ConvertedCreditsWeightView]
  val creditRangeOption = CreditRangeOption(LocalDate.of(2023, 4, 1), LocalDate.of(2024, 3, 31))

  def render(form: Form[Long]): String =
    page(form, Call("method", "/submit-url"), creditRangeOption)(request, messages).toString()

  "view" should {
    "pass accessibility tests" when {
      "no error" in {
        render(form) must passAccessibilityChecks
      }

      "with error" in {
        render(form.withError("error", "error message")) must passAccessibilityChecks
      }
    }
  }

}
