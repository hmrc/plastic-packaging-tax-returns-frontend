/*
 * Copyright 2025 HM Revenue & Customs
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

package viewmodels.checkAnswers.changeGroupLead

import controllers.changeGroupLead.routes
import models.Mode.CheckMode
import models.UserAnswers
import pages.changeGroupLead.MainContactJobTitlePage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.SummaryViewModel
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object MainContactJobTitleSummary extends SummaryViewModel {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(MainContactJobTitlePage).map { answer =>
      SummaryListRowViewModel(
        key = "newGroupLeadCheckYourAnswers.job.title.key",
        value = ValueViewModel(HtmlFormat.escape(answer).toString),
        actions = Seq(
          ActionItemViewModel("site.change", routes.MainContactJobTitleController.onPageLoad(CheckMode).url)
            .withVisuallyHiddenText(messages("newGroupLeadCheckYourAnswers.job.title.key"))
        )
      )
    }
}
