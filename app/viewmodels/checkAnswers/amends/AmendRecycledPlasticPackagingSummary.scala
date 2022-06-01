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

package viewmodels.checkAnswers.amends

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.amends.AmendRecycledPlasticPackagingPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.SummaryViewModel
import viewmodels.govuk.all.FluentActionItem
import viewmodels.govuk.summarylist.SummaryListRowViewModel
import viewmodels.govuk.summarylist.ActionItemViewModel
import viewmodels.govuk.summarylist.ValueViewModel
import viewmodels.implicits._

object AmendRecycledPlasticPackagingSummary extends SummaryViewModel {

  override def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(AmendRecycledPlasticPackagingPage).map {
      answer =>
        SummaryListRowViewModel(key = "amendRecycledPlasticPackaging.checkYourAnswersLabel",
          value = ValueViewModel(answer.toString),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              controllers.amends.routes.AmendRecycledPlasticPackagingController.onPageLoad(
                CheckMode
              ).url
            )
              .withVisuallyHiddenText(
                messages("amendRecycledPlasticPackaging.change.hidden")
              )
          )
        )
    }

}
