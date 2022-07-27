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

import cacheables.ReturnDisplayApiCacheable
import controllers.amends.AmendSummaryRow
import models.UserAnswers
import models.Mode.CheckMode
import models.returns.ReturnDisplayApi
import pages.amends.AmendManufacturedPlasticPackagingPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.SummaryViewModel
import viewmodels.govuk.all.FluentActionItem
import viewmodels.govuk.summarylist.{ActionItemViewModel, SummaryListRowViewModel, ValueViewModel}
import viewmodels.implicits._

object AmendManufacturedPlasticPackagingSummary extends SummaryViewModel {

  def buildRow(answers: UserAnswers)(implicit messages: Messages): Option[AmendSummaryRow] = {

    val returnDisplayApi: ReturnDisplayApi = answers.get(ReturnDisplayApiCacheable).getOrElse(
      throw new IllegalArgumentException("Must have a return display API to do an amend")
    )

    answers.get(AmendManufacturedPlasticPackagingPage).map {
      answer =>
        val existing = returnDisplayApi.returnDetails.manufacturedWeight
        val amended  = if(existing != answer) { answer.toString } else { "" }

        AmendSummaryRow(
          messages("amendManufacturedPlasticPackaging.checkYourAnswersLabel"),
          existing.toString,
          amended,
          controllers.amends.routes.AmendManufacturedPlasticPackagingController.onPageLoad(CheckMode).url
        )
    }
  }

  override def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(AmendManufacturedPlasticPackagingPage).map {
      answer =>
        SummaryListRowViewModel(key = "amendManufacturedPlasticPackaging.checkYourAnswersLabel",
          value = ValueViewModel(answer.toString),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              controllers.amends.routes.AmendManufacturedPlasticPackagingController.onPageLoad(
                CheckMode
              ).url
            )
              .withVisuallyHiddenText(
                messages("amendManufacturedPlasticPackaging.change.hidden")
              )
          )
        )
    }

}
