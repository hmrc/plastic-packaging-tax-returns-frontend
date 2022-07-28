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
import models.UserAnswers
import models.amends.AmendSummaryRow
import models.returns.ReturnDisplayApi
import pages.amends.AmendRecycledPlasticPackagingPage
import play.api.i18n.Messages

object AmendRecycledPlasticPackagingSummary {

  def apply(answers: UserAnswers)(implicit messages: Messages): AmendSummaryRow = {

    val returnDisplayApi: ReturnDisplayApi = answers.get(ReturnDisplayApiCacheable).getOrElse(
      throw new IllegalArgumentException("Must have a return display API to do an amend")
    )

    val maybeAnswer: Option[Int] = answers.get(AmendRecycledPlasticPackagingPage)
    val existing: BigDecimal     = returnDisplayApi.returnDetails.recycledPlastic

    val amended: Option[String] = maybeAnswer.map(_.toString)


    AmendSummaryRow(
      messages("amendRecycledPlasticPackaging.checkYourAnswersLabel"),
      existing.toString,
      amended,
      Some(controllers.amends.routes.AmendRecycledPlasticPackagingController.onPageLoad().url)
    )
  }

}
