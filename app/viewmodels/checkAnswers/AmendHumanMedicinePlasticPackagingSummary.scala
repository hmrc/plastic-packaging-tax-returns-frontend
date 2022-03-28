package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.AmendHumanMedicinePlasticPackagingPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object AmendHumanMedicinePlasticPackagingSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(AmendHumanMedicinePlasticPackagingPage).map {
      answer =>

        SummaryListRowViewModel(
          key     = "amendHumanMedicinePlasticPackaging.checkYourAnswersLabel",
          value   = ValueViewModel(answer.toString),
          actions = Seq(
            ActionItemViewModel("site.change", routes.AmendHumanMedicinePlasticPackagingController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("amendHumanMedicinePlasticPackaging.change.hidden"))
          )
        )
    }
}
