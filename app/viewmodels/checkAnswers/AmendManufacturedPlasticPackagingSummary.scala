package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.AmendManufacturedPlasticPackagingPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object AmendManufacturedPlasticPackagingSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(AmendManufacturedPlasticPackagingPage).map {
      answer =>

        SummaryListRowViewModel(
          key     = "amendManufacturedPlasticPackaging.checkYourAnswersLabel",
          value   = ValueViewModel(answer.toString),
          actions = Seq(
            ActionItemViewModel("site.change", routes.AmendManufacturedPlasticPackagingController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("amendManufacturedPlasticPackaging.change.hidden"))
          )
        )
    }
}
