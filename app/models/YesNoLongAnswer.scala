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

package models

import pages.QuestionPage
import play.api.libs.json.{Json, OFormat}

case class YesNoLongAnswer(userSaidYes: Option[Boolean], usersValue: Option[Long]) {

  /**
    * @return true if either:
    *         - user answered 'no'
    *         - user answered 'yes' and provided a value
    */
  def areQuestionsAnswered: Boolean = this match {
    case YesNoLongAnswer(Some(false), _)      => true
    case YesNoLongAnswer(Some(true), Some(_)) => true
    case _                                    => false
  }

  /**
    * @return returns the effective value of the users answers, ie the value given by the user or zero if user 
    *         answered 'no'
    * @throws IllegalStateException if both parts / questions have not been answered 
    */
  def value: Long = this match {
    case YesNoLongAnswer(Some(false), _)          => 0L
    case YesNoLongAnswer(Some(true), Some(value)) => value
    case _                                        => throw new IllegalStateException(s"Question only partially answered ($this)")
  }

  /**
    * @return true if the user answered 'no'
    */
  def isUsersAnswerNo: Boolean = userSaidYes.contains(false)

  /**
    * @return true if the user answered 'yes'
    */
  def isUsersAnswerYes: Boolean = userSaidYes.contains(true)

  /**
    * Updates the user's answer to 'yes', leaves value as is
    *
    * @return an updated UserAnswers object
    */
  def changeAnswerToYes: YesNoLongAnswer =
    YesNoLongAnswer(Some(true), usersValue)

  /**
    * Updates the user's answer to 'no', leaves value as is
    *
    * @return an updated UserAnswers object
    */
  def changeAnswerToNo: YesNoLongAnswer =
    YesNoLongAnswer(Some(false), usersValue)

  /**
    * Updates the user's answer to given value, and 'yes' (as is implied by providing a value)
    *
    * @return an updated UserAnswers object
    * @param newValue - value given by user
    */
  def changeAnswerToValue(newValue: Long): YesNoLongAnswer =
    YesNoLongAnswer(Some(true), Some(newValue))

}

object YesNoLongAnswer {
  
  def fromUserAnswers(userAnswers: UserAnswers, pageOne: QuestionPage[Boolean], pageTwo: QuestionPage[Long]): YesNoLongAnswer = {
    val maybeUserAnsweredYes: Option[Boolean] = userAnswers.get(pageOne)
    val maybeUsersValueWas: Option[Long] = userAnswers.get(pageTwo)
    YesNoLongAnswer(maybeUserAnsweredYes, maybeUsersValueWas)
  }

  def apply(): YesNoLongAnswer = new YesNoLongAnswer(None, None)
  implicit val formats: OFormat[YesNoLongAnswer] = Json.format[YesNoLongAnswer]
}
