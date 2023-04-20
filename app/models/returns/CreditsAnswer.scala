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
import models.returns.CreditsAnswer.noClaim
import play.api.data.Form
import play.api.libs.json.{JsPath, Json, OFormat}
import queries.Gettable

case class CreditsAnswer(yesNo: Boolean, weight: Option[Long]) {

  def yesNoMsgKey: String = if (yesNo) "site.yes" else "site.no"

  def value: Long = (yesNo, weight) match {
    case (true, Some(x)) => x
    case (true, None) => 0
    case (false, _) => 0
  }

  def changeYesNo(isYes: Boolean) = 
    if (isYes)
      CreditsAnswer(isYes, weight)
    else
      CreditsAnswer(false, Some(0))

}

object CreditsAnswer {

  def changeYesNo(isYes: Boolean, path: JsPath, userAnswers: UserAnswers): UserAnswers = {
    val newCreditAnswer = userAnswers
      .get[CreditsAnswer](path)
      .fold(CreditsAnswer.noClaim) (_.changeYesNo(isYes))
    userAnswers.setOrFail[CreditsAnswer](path, newCreditAnswer)
  }

  implicit val formats: OFormat[CreditsAnswer] = Json.format[CreditsAnswer]

  def noClaim: CreditsAnswer = CreditsAnswer(false, None)
  
  def answerYesNoWith(isYes: Boolean): CreditsAnswer = 
    if (isYes) CreditsAnswer(false, Some(0L)) else CreditsAnswer(false, Some(0L))
  
  def answerWeightWith(weight: Long): CreditsAnswer = CreditsAnswer(true, Some(weight))

  def fillFormYesNo(userAnswers: UserAnswers, page: Gettable[CreditsAnswer], form: Form[Boolean]) = {
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
