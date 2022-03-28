package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.AmendDirectExportPlasticPackagingPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object AmendDirectExportPlasticPackagingSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(AmendDirectExportPlasticPackagingPage).map {
      answer =>

        SummaryListRowViewModel(
          key     = "amendDirectExportPlasticPackaging.checkYourAnswersLabel",
          value   = ValueViewModel(answer.toString),
          actions = Seq(
            ActionItemViewModel("site.change", routes.AmendDirectExportPlasticPackagingController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("amendDirectExportPlasticPackaging.change.hidden"))
          )
        )
    }
}
