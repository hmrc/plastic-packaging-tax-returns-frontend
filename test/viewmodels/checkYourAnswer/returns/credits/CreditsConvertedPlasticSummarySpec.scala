package viewmodels.checkYourAnswer.returns.credits

import models.Mode.CheckMode
import models.UserAnswers
import org.mockito.ArgumentMatchers
import org.mockito.MockitoSugar.{mock, reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import pages.returns.credits.ConvertedCreditsPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Key, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Actions, SummaryListRow, Value}
import viewmodels.checkAnswers.returns.credits.CreditsConvertedPlasticSummary

class CreditsConvertedPlasticSummarySpec extends PlaySpec with BeforeAndAfterEach{

  private val messages = mock[Messages]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(messages)
    when(messages.apply(ArgumentMatchers.eq("confirmPackagingCredit.converted.answer"))).thenReturn("answer")
    when(messages.apply(ArgumentMatchers.eq("site.change"))).thenReturn("change")
  }



  "Row" should {
    "return a summary row" when {
      "answer is yes" in {
        when(messages.apply(ArgumentMatchers.eq("site.yes"))).thenReturn("yes")
        val userAnswer = UserAnswers("123").set(ConvertedCreditsPage, true).get

        val result = CreditsConvertedPlasticSummary.row(userAnswer)(messages)

        result mustBe createExpectedResult("yes")
      }

      "answer is no" in {
        when(messages.apply(ArgumentMatchers.eq("site.no"))).thenReturn("no")
        val userAnswer = UserAnswers("123").set(ConvertedCreditsPage, false).get

        val result = CreditsConvertedPlasticSummary.row(userAnswer)(messages)

        result mustBe createExpectedResult("no")
      }
    }
  }

  private def createExpectedResult(answerValue: String): Option[SummaryListRow] = {
    Some(SummaryListRow(
      key = Key(Text("answer")),
      value = Value(Text(answerValue)),
      actions = Some(Actions(items = Seq(ActionItem(
        controllers.returns.credits.routes.ConvertedCreditsController.onPageLoad(CheckMode).url,
        Text("change")))))
    ))
  }
}
