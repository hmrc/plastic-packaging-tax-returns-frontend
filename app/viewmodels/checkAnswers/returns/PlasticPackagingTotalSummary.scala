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

package viewmodels.checkAnswers.returns

import controllers.helpers.NonExportedAmountHelper
import models.UserAnswers
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.PrintLong
import viewmodels.checkAnswers.SummaryViewModel
import viewmodels.govuk.all.FluentValue
import viewmodels.govuk.summarylist.{SummaryListRowViewModel, ValueViewModel}
import viewmodels.implicits._

class PlasticPackagingTotalSummary(nonExportedAmountHelper: NonExportedAmountHelper) extends SummaryViewModel {
  override def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    Some(
      SummaryListRowViewModel(
        key = "confirmPlasticPackagingTotal.total.label",
        value = ValueViewModel(nonExportedAmountHelper.totalPlasticAdditions(answers).getOrElse(0L).asKg).withCssClass(
          "total-weight"
        ),
        actions = Seq.empty
      )
    )

}
