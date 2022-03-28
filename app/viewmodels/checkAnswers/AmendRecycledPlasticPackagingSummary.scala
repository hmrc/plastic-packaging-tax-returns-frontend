package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.AmendRecycledPlasticPackagingPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object AmendRecycledPlasticPackagingSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(AmendRecycledPlasticPackagingPage).map {
      answer =>

        SummaryListRowViewModel(
          key     = "amendRecycledPlasticPackaging.checkYourAnswersLabel",
          value   = ValueViewModel(answer.toString),
          actions = Seq(
            ActionItemViewModel("site.change", routes.AmendRecycledPlasticPackagingController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("amendRecycledPlasticPackaging.change.hidden"))
          )
        )
    }
}
