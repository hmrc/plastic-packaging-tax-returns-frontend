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

import play.api.libs.json.{Json, OFormat}

case class CreditsAnswer(yesNo: Boolean, private val weight: Option[Long]) {

  def yesNoMsgKey: String = if (yesNo) "site.yes" else "site.no"

  def weightValue: Long = (yesNo, weight) match {
    case (true, Some(x)) => x
    case _ => 0
  }

  def changeYesNoTo(isYes: Boolean): CreditsAnswer = CreditsAnswer(isYes, weightForForm)
  def asTuple: (Boolean, Long) = yesNo -> weightValue

  def weightForForm: Option[Long] = (yesNo, weight) match {
    case (_, None) => None
    case (false, _) => Some(0L)
    case (true, x) => x 
  }
  
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
  def fillFormYesNo(creditsAnswer: CreditsAnswer): Option[Boolean] = Some(creditsAnswer.yesNo)
  def fillFormWeight(creditsAnswer: CreditsAnswer): Option[Long] = creditsAnswer.weightForForm
    
}
