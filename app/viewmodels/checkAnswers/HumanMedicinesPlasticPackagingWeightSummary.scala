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

package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.HumanMedicinesPlasticPackagingWeightPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.PrintLong
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object HumanMedicinesPlasticPackagingWeightSummary extends SummaryViewModel {

  override def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(HumanMedicinesPlasticPackagingWeightPage).map {
      answer =>
        SummaryListRowViewModel(key = "humanMedicinesPlasticPackagingWeight.checkYourAnswersLabel",
                                value = ValueViewModel(answer.asKgs),
                                actions = Seq(
                                  ActionItemViewModel(
                                    "site.change",
                                    routes.HumanMedicinesPlasticPackagingWeightController.onPageLoad(
                                      CheckMode
                                    ).url
                                  )
                                    .withVisuallyHiddenText(
                                      messages("humanMedicinesPlasticPackagingWeight.change.hidden")
                                    )
                                )
        )
    }

}
