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

import models.UserAnswers
import models.returns.credits.CreditSummaryRow
import navigation.ReturnsJourneyNavigator
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, JsPath}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object CreditsClaimedListSummary {

  def createRows(answers: UserAnswers, navigator: ReturnsJourneyNavigator) 
    (implicit messages: Messages): Seq[SummaryListRow] = {

    answers.get[Map[String,JsObject]](JsPath \ "credit").map {
      answer: Map[String, JsObject] =>

        answer.map(item => {
          CreditSummaryRow(
            key = o._1,
            "0",
            change = ActionItemViewModel("site.change", navigator.creditSummaryChange(item._1)),
            remove = ActionItemViewModel("site.remove", navigator.creditSummaryRemove(item._1))
          )
        })
    }.getOrElse(Seq.empty).toSeq
  }

}
