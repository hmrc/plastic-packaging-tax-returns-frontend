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
import models.UserAnswers
import models.Mode.CheckMode
import pages.returns.ManufacturedPlasticPackagingWeightPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.PrintLong
import viewmodels.checkAnswers.SummaryViewModel
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

class ManufacturedPlasticPackagingWeightSummary private(key: String) extends SummaryViewModel {

  override def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answer(answers).map {
      answer => createSummaryListView(answer.asKg)
    }

  override def answer(answers: UserAnswers): Option[Long] = {
    answers.get(ManufacturedPlasticPackagingWeightPage)
  }

  private def createSummaryListView(value: String)(implicit messages: Messages): SummaryListRow = {
    SummaryListRowViewModel(key = key,
      value = ValueViewModel(value),
      actions = Seq(
        ActionItemViewModel(
          "site.change",
          routes.ManufacturedPlasticPackagingWeightController.onPageLoad(
            CheckMode
          ).url
        ).withAttribute(("id", "confirm-pp-total-weight-manufactured"))
      )
    )

  }

}

object ManufacturedPlasticPackagingWeightSummary {

  private val confirmPlasticPackagingTotalLabel = "confirmPlasticPackagingTotal.weightManufacturedPlasticPackaging.label"

  val ConfirmManufacturedPlasticPackagingSummary = new ManufacturedPlasticPackagingWeightSummary(confirmPlasticPackagingTotalLabel)

}
