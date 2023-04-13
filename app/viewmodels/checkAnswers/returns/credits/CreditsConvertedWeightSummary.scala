package viewmodels.checkAnswers.returns.credits

import models.Mode.CheckMode
import models.UserAnswers
import pages.returns.credits.ConvertedCreditsWeightPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.PrintLong
import viewmodels.checkAnswers.SummaryViewModel
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object CreditsConvertedWeightSummary extends SummaryViewModel{
  override def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] = {
    answers.get(ConvertedCreditsWeightPage).map {
      value =>
        SummaryListRowViewModel(
          key = "confirmPackagingCredit.converted.weight",
          value = ValueViewModel(value.asKg),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              controllers.returns.credits.routes.ConvertedCreditsWeightController.onPageLoad(CheckMode).url
            )
          )
        )
    }
  }
}
