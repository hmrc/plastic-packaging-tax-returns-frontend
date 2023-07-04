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

package models.returns.credits

import models.UserAnswers
import models.returns.{CreditRangeOption, CreditsAnswer}
import play.api.i18n.Messages
import play.api.libs.json.{JsPath, Json, OFormat}
import views.ViewUtils

import java.time.LocalDate


case class SingleYearClaim(
  fromDate: LocalDate,
  toDate: LocalDate,
  exportedCredits: Option[CreditsAnswer],
  convertedCredits: Option[CreditsAnswer]
) {
  def toDateRangeString(implicit messages: Messages) = ViewUtils.displayDateRangeTo(fromDate, toDate)
  def createCreditRangeOption(): CreditRangeOption = CreditRangeOption(fromDate, toDate)
}

object SingleYearClaim {

  def readFrom(userAnswers: UserAnswers, key: String): SingleYearClaim = {
    // TODO possible de-dupes elsewhere?
    userAnswers.getOrFail[SingleYearClaim](JsPath \ "credit" \ key)
  }

  def maybeReadFrom(userAnswers: UserAnswers, key: String): Option[SingleYearClaim] = {
    userAnswers.get[SingleYearClaim](JsPath \ "credit" \ key)
  }
  
  implicit val formats: OFormat[SingleYearClaim] = Json.format[SingleYearClaim]
}
