package viewmodels.checkAnswers.returns.credits

import models.Mode.CheckMode
import models.UserAnswers
import pages.returns.credits.ExportedCreditsWeightPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.PrintLong
import viewmodels.checkAnswers.SummaryViewModel
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object CreditsExportedWeightSummary extends SummaryViewModel {
  override def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] = {
    answers.get(ExportedCreditsWeightPage).map {
      value =>
        SummaryListRowViewModel(
          key = "confirmPackagingCredit.exported.weight",
          value = ValueViewModel(value.asKg),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              controllers.returns.credits.routes.ExportedCreditsWeightController.onPageLoad(CheckMode).url
            )
          )
        )
    }
  }
}
