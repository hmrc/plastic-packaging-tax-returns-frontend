package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.AmendExportedWeightPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object AmendExportedWeightSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(AmendExportedWeightPage).map {
      answer =>

        SummaryListRowViewModel(
          key     = "amendExportedWeight.checkYourAnswersLabel",
          value   = ValueViewModel(answer.toString),
          actions = Seq(
            ActionItemViewModel("site.change", routes.AmendExportedWeightController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("amendExportedWeight.change.hidden"))
          )
        )
    }
}
