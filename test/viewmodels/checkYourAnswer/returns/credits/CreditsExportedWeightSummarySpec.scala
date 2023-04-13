package viewmodels.checkYourAnswer.returns.credits

import models.Mode.CheckMode
import models.UserAnswers
import org.mockito.ArgumentMatchers
import org.mockito.MockitoSugar.{mock, when}
import org.scalatestplus.play.PlaySpec
import pages.returns.credits.ExportedCreditsWeightPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._
import viewmodels.PrintLong
import viewmodels.checkAnswers.returns.credits.CreditsExportedWeightSummary

class CreditsExportedWeightSummarySpec extends PlaySpec {

  private val messages = mock[Messages]


  "summary" should {
    "return a Summary row" when {
      "answer is yes" in {
        when(messages.apply(ArgumentMatchers.eq("confirmPackagingCredit.exported.weight"))).thenReturn("exported weight")
        when(messages.apply(ArgumentMatchers.eq("site.change"))).thenReturn("change")
        when(messages.apply(ArgumentMatchers.eq(20L.asKg))).thenReturn(20L.asKg)

        val userAnswer = UserAnswers("123").set(ExportedCreditsWeightPage, 20L).get

        CreditsExportedWeightSummary.row(userAnswer)(messages) mustBe createExpectedWeightResult
      }
    }
  }

  private def createExpectedWeightResult: Option[SummaryListRow] = {
    Some(SummaryListRow(
      key = Key(Text("exported weight")),
      value = Value(Text(20L.asKg)),
      actions = Some(Actions(items = Seq(ActionItem(
        controllers.returns.credits.routes.ExportedCreditsWeightController.onPageLoad(CheckMode).url,
        Text("change")))))
    ))
  }
}
