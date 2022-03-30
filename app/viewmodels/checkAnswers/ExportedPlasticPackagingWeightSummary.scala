package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.ExportedPlasticPackagingWeightPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object ExportedPlasticPackagingWeightSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(ExportedPlasticPackagingWeightPage).map {
      answer =>

        SummaryListRowViewModel(
          key     = "exportedPlasticPackagingWeight.checkYourAnswersLabel",
          value   = ValueViewModel(answer.toString),
          actions = Seq(
            ActionItemViewModel("site.change", routes.ExportedPlasticPackagingWeightController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("exportedPlasticPackagingWeight.change.hidden"))
          )
        )
    }
}
