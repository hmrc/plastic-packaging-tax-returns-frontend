package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.RecycledPlasticPackagingWeightPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object RecycledPlasticPackagingWeightSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(RecycledPlasticPackagingWeightPage).map {
      answer =>

        SummaryListRowViewModel(
          key     = "recycledPlasticPackagingWeight.checkYourAnswersLabel",
          value   = ValueViewModel(answer.toString),
          actions = Seq(
            ActionItemViewModel("site.change", routes.RecycledPlasticPackagingWeightController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("recycledPlasticPackagingWeight.change.hidden"))
          )
        )
    }
}
