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

package viewmodels.checkAnswers.returns

import controllers.returns.routes
import models.UserAnswers
import models.Mode.CheckMode
import pages.returns.ImportedPlasticPackagingPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.SummaryViewModel
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

class ImportedPlasticPackagingSummary private(key: String) extends SummaryViewModel {

  override def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(ImportedPlasticPackagingPage).map {
      answer =>
        val value = if (answer) "site.yes" else "site.no"

        SummaryListRowViewModel(key = key,
          value = ValueViewModel(value),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              routes.ImportedPlasticPackagingController.onPageLoad(CheckMode).url
            )
              .withAttribute("id" -> "confirm-pp-total-imported-plastic")
              .withVisuallyHiddenText(messages("importedPlasticPackaging.change.hidden")
              )
          )
        )
    }

}

object ImportedPlasticPackagingSummary {

  private val confirmImportedPlasticPackagingLabel = "confirmPlasticPackagingTotal.importedPlasticPackaging.label"

  val ConfirmImportedPlasticPackagingSummary = new ImportedPlasticPackagingSummary(confirmImportedPlasticPackagingLabel)
}
