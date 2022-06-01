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

import models.UserAnswers
import models.returns.{ImportedPlasticWeight, ManufacturedPlasticWeight}
import pages.returns.{ImportedPlasticPackagingWeightPage, ManufacturedPlasticPackagingWeightPage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.PrintLong
import viewmodels.checkAnswers.SummaryViewModel
import viewmodels.govuk.all.FluentValue
import viewmodels.govuk.summarylist.{SummaryListRowViewModel, ValueViewModel}
import viewmodels.implicits._

object PlasticPackagingTotalSummary extends SummaryViewModel {
  override def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    Some(SummaryListRowViewModel(
      key = "confirmPlasticPackagingTotal.total.label",
      value = ValueViewModel(calculateTotal(answers).asKgs).withCssClass("total-weight"),
      actions = Seq.empty
    ))

  //todo if these exceptions get thrown we should redirect the user to the questions to input them
  def calculateTotal(answers: UserAnswers): Long = {
    answers.get(ManufacturedPlasticPackagingWeightPage).map(
      value => ManufacturedPlasticWeight(value).totalKg
    ).getOrElse(throw new IllegalStateException("Manufactured Plastic Weight not found.")) +
      answers.get(ImportedPlasticPackagingWeightPage).map(
        value => ImportedPlasticWeight(value).totalKg
      ).getOrElse(throw new IllegalStateException("Imported Plastic Weight not found."))
  }
}
