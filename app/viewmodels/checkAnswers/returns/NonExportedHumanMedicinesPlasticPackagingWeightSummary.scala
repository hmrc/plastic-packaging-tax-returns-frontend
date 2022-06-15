/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package viewmodels.checkAnswers.returns

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.returns.NonExportedHumanMedicinesPlasticPackagingWeightPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.SummaryViewModel
import viewmodels.govuk.all.FluentActionItem
import viewmodels.govuk.summarylist.{ActionItemViewModel, SummaryListRowViewModel, ValueViewModel}
import viewmodels.implicits._
import viewmodels.PrintLong

class NonExportedHumanMedicinesPlasticPackagingWeightSummary(key: String = "nonExportedHumanMedicinesPlasticPackagingWeight.checkYourAnswersLabel")
  extends SummaryViewModel {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answer(answers).map {
      answer: Long =>

        SummaryListRowViewModel(
          key = key,
          value = ValueViewModel(answer.asKg),
          actions = Seq(
            ActionItemViewModel("site.change", controllers.returns.routes.NonExportedHumanMedicinesPlasticPackagingWeightController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("nonExportedHumanMedicinesPlasticPackagingWeight.change.hidden"))
          )
        )
    }

  override def answer(answers: UserAnswers): Option[Long] = {
    answers.get(NonExportedHumanMedicinesPlasticPackagingWeightPage)
  }
}
