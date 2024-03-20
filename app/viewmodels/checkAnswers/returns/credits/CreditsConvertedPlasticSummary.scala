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
import pages.returns.credits.ConvertedCreditsPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Key
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.SummaryViewModel
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

case class CreditsConvertedPlasticSummary(key: String) extends SummaryViewModel {
  override def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] = {
    answers.get(ConvertedCreditsPage(key)).orElse(Some(CreditsAnswer.noClaim)).map { creditsAnswer =>
      SummaryListRowViewModel(
        key = Key("confirmPackagingCredit.converted.answer", classes = "govuk-!-width-one-half"),
        value = ValueViewModel(creditsAnswer.yesNoMsgKey),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            controllers.returns.credits.routes.ConvertedCreditsController.onPageLoad(key, CheckMode).url
          ).withVisuallyHiddenText(messages("confirmPackagingCredit.converted.answer"))
        )
      )
    }
  }
}
