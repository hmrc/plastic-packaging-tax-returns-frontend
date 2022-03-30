package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.HumanMedicinesPlasticPackagingWeightPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object HumanMedicinesPlasticPackagingWeightSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(HumanMedicinesPlasticPackagingWeightPage).map {
      answer =>

        SummaryListRowViewModel(
          key     = "humanMedicinesPlasticPackagingWeight.checkYourAnswersLabel",
          value   = ValueViewModel(answer.toString),
          actions = Seq(
            ActionItemViewModel("site.change", routes.HumanMedicinesPlasticPackagingWeightController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("humanMedicinesPlasticPackagingWeight.change.hidden"))
          )
        )
    }
}
