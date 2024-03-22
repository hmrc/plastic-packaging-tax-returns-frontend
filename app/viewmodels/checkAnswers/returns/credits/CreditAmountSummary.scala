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

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow}
import viewmodels.PrintBigDecimal
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object CreditAmountSummary {

  def apply(creditAmount: BigDecimal)(implicit messages: Messages): SummaryListRow =
    SummaryListRowViewModel(
      key = Key("confirmPackagingCredit.creditAmount", classes = "govuk-!-width-one-half"),
      value = ValueViewModel(creditAmount.asPounds)
    )
}
