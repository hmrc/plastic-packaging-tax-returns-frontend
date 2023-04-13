package viewmodels.checkAnswers.returns.credits

import models.Mode.CheckMode
import models.UserAnswers
import pages.returns.credits.{ConvertedCreditsPage, ExportedCreditsPage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.SummaryViewModel
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object CreditsConvertedPlasticSummary extends SummaryViewModel {
  override def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] = {
    answers.get(ConvertedCreditsPage).map {
      answer =>
        val value = if (answer) "site.yes" else "site.no"

        SummaryListRowViewModel(
          key = "confirmPackagingCredit.converted.answer",
          value = ValueViewModel(value),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              controllers.returns.credits.routes.ConvertedCreditsController.onPageLoad(CheckMode).url
            )
          )
        )
    }
  }
}
