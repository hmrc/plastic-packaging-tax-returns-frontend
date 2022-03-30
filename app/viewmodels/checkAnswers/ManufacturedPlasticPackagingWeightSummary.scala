package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.ManufacturedPlasticPackagingWeightPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object ManufacturedPlasticPackagingWeightSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(ManufacturedPlasticPackagingWeightPage).map {
      answer =>

        SummaryListRowViewModel(
          key     = "manufacturedPlasticPackagingWeight.checkYourAnswersLabel",
          value   = ValueViewModel(answer.toString),
          actions = Seq(
            ActionItemViewModel("site.change", routes.ManufacturedPlasticPackagingWeightController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("manufacturedPlasticPackagingWeight.change.hidden"))
          )
        )
    }
}
