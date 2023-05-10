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
import models.returns.CreditsAnswer
import pages.returns.credits.CreditsClaimedListPage
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

import java.time.LocalDate


case class Credits(credits: Seq[Credit])


case class Credit
(
  endDate: LocalDate,
  exportedCredit: CreditsAnswer,
  convertedCredit: CreditsAnswer
)


object CreditsClaimedListSummary {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] = {

    val t=  answers.get((JsPath \ "credit"))
    answers.get(CreditsClaimedListPage).map {
      answer =>
        val value = if (answer) "site.yes" else "site.no"

        SummaryListRowViewModel(
          key = "creditsSummary.checkYourAnswersLabel",
          value = ValueViewModel(value),
          actions = Seq(
            ActionItemViewModel("site.change", controllers.returns.credits.routes.CreditsClaimedListController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("creditsSummary.change.hidden"))
          )
        )
    }
  }

}
