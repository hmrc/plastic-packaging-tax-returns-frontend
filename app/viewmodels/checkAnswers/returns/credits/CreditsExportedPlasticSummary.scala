/*
 * Copyright 2023 HM Revenue & Customs
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

package viewmodels.checkAnswers.returns.credits

import models.Mode.CheckMode
import models.UserAnswers
import pages.returns.credits.ExportedCreditsPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Key
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.SummaryViewModel
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object CreditsExportedPlasticSummary extends SummaryViewModel {

  override def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] = {
    answers.get(ExportedCreditsPage).orElse(Some(false)).map {
      answer =>
        val value = if (answer) "site.yes" else "site.no"

        SummaryListRowViewModel(
          key = Key("confirmPackagingCredit.exported.answer", classes="govuk-!-width-one-half"),
          value = ValueViewModel(value),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              controllers.returns.credits.routes.ExportedCreditsController.onPageLoad(CheckMode).url
            ).withVisuallyHiddenText(messages("confirmPackagingCredit.exported.answer"))
          )
        )
    }
  }

}
