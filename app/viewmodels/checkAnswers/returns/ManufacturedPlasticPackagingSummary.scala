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

import controllers.returns.routes
import models.{CheckMode, UserAnswers}
import pages.returns.ManufacturedPlasticPackagingPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._
import viewmodels.checkAnswers.SummaryViewModel
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

class ManufacturedPlasticPackagingSummary (key: String) extends SummaryViewModel {
  override def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(ManufacturedPlasticPackagingPage).map {
      answer =>
        val value = if (answer) "site.yes" else "site.no"

        SummaryListRow(key = Key(key, classes="govuk-!-font-weight-regular"),
          value = Value(value),
          actions = Some(Actions(items = Seq(
            ActionItem(
              routes.ManufacturedPlasticPackagingController.onPageLoad(CheckMode).url,
              messages("site.change"),

            ).withAttribute("id"-> "confirm-pp-total-manufactured-plastic")
              .withVisuallyHiddenText(messages("manufacturedPlasticPackaging.change.hidden"))
          )))
        )
    }
}
object ManufacturedPlasticPackagingSummary {

  private val manufacturedPlasticPageLabel = "manufacturedPlasticPackaging.checkYourAnswersLabel"
  private val confirmPlasticPackagingTotalLabel = "confirmPlasticPackagingTotal.manufacturedPlasticPackaging.label"

  val CheckYourAnswerManufacturedPlasticPackaging = new ManufacturedPlasticPackagingSummary(manufacturedPlasticPageLabel)
  val ConfirmManufacturedPlasticPackaging = new ManufacturedPlasticPackagingSummary(confirmPlasticPackagingTotalLabel)
}
