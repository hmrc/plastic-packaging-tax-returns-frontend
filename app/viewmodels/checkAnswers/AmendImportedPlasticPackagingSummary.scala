package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.AmendImportedPlasticPackagingPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object AmendImportedPlasticPackagingSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(AmendImportedPlasticPackagingPage).map {
      answer =>

        SummaryListRowViewModel(
          key     = "amendImportedPlasticPackaging.checkYourAnswersLabel",
          value   = ValueViewModel(answer.toString),
          actions = Seq(
            ActionItemViewModel("site.change", routes.AmendImportedPlasticPackagingController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("amendImportedPlasticPackaging.change.hidden"))
          )
        )
    }
}
