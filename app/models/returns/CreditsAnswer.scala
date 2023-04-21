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

import scala.language.implicitConversions

case class CreditsAnswer(yesNo: Boolean, private val _weight: Option[Long]) {

  def yesNoMsgKey: String = if (yesNo) "site.yes" else "site.no"

  def weight: Long = (yesNo, _weight) match {
    case (true, Some(x)) => x
    case (true, None) => 0
    case (false, _) => 0
  }

  def changeYesNoTo(isYes: Boolean) = 
    if (isYes)
      CreditsAnswer(isYes, _weight)
    else
      CreditsAnswer(false, Some(0))

  def asTuple: (Boolean, Long) = yesNo -> weight

}

object CreditsAnswer {

  def changeYesNoTo(isYes: Boolean) (previousAnswer: Option[CreditsAnswer]): CreditsAnswer = { // Curry for the win
    previousAnswer match {
      case None => CreditsAnswer(isYes, None)
      case Some(previous) => previous.changeYesNoTo(isYes)
    }
  }

  implicit val formats: OFormat[CreditsAnswer] = Json.format[CreditsAnswer]

  def noClaim: CreditsAnswer = CreditsAnswer(false, None)
  
  def answerWeightWith(weight: Long): CreditsAnswer = CreditsAnswer(true, Some(weight))

  def fillFormYesNo(userAnswers: UserAnswers, page: Gettable[CreditsAnswer], form: Form[Boolean]): Form[Boolean] = {
    userAnswers.get(page) match {
      case None => form
      case Some(value) => form.fill(value.yesNo)
    }
  }

  def fillFormWeight(userAnswers: UserAnswers, page: Gettable[CreditsAnswer], form: Form[Long]): Form[Long] = {
    userAnswers.get(page) match {
      case None => form
      case Some(value) => form.fill(value.weight)
    }
  }
}
