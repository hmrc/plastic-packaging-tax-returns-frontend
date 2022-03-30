package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.ImportedPlasticPackagingWeightPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object ImportedPlasticPackagingWeightSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(ImportedPlasticPackagingWeightPage).map {
      answer =>

        SummaryListRowViewModel(
          key     = "importedPlasticPackagingWeight.checkYourAnswersLabel",
          value   = ValueViewModel(answer.toString),
          actions = Seq(
            ActionItemViewModel("site.change", routes.ImportedPlasticPackagingWeightController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("importedPlasticPackagingWeight.change.hidden"))
          )
        )
    }
}
