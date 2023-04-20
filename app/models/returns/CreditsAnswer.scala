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

package models.returns

import models.UserAnswers
import play.api.data.Form
import play.api.libs.json.{Json, OFormat}
import queries.Gettable

//todo: This would need to relooked as now we had two page for credit answer and weight
case class CreditsAnswer(yesNo: Boolean, weight: Option[Long]) {
  def yesNoMsgKey: String = if(yesNo) "site.yes" else "site.no"
  def value: Long = (yesNo, weight) match {
    case (true, Some(x)) => x
    case (true, None) => 0
    case (false, _) => 0
  }
}

object CreditsAnswer {
  def setUserAnswer(userAnswers: UserAnswers) = ???

  def isYesNo(value: Boolean): CreditsAnswer = CreditsAnswer(value, None)

  def noClaim: CreditsAnswer = CreditsAnswer(false, None)
  implicit val formats: OFormat[CreditsAnswer] = Json.format[CreditsAnswer]

  def fillForm(userAnswers: UserAnswers, page: Gettable[CreditsAnswer], form: Form[Boolean]) = {
    userAnswers.get(page) match {
      case None => form
      case Some(value) => form.fill(value.yesNo)
    }
  }

  def fillFormWeight(userAnswers: UserAnswers, page: Gettable[CreditsAnswer], form: Form[Long]) = {
    userAnswers.get(page) match {
      case None => form
      case Some(value) => form.fill(value.value)
    }
  }
}
