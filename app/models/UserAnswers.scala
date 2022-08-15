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

package models

import models.UserAnswers.SaveUserAnswerFunc
import pages.QuestionPage
import play.api.data.Form
import play.api.libs.json._
import queries.{Gettable, Settable}
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

case class UserAnswers(
  id: String,
  data: JsObject = Json.obj(),
  lastUpdated: Instant = Instant.now
) {

  def fill[A](gettable: Gettable[A], form: Form[A])(implicit rds: Reads[A]): Form[A] =
    get(gettable).fold(form)(form.fill)

  def get[A](page: Gettable[A])(implicit rds: Reads[A]): Option[A] =
    Reads.optionNoError(Reads.at(page.path)).reads(data).getOrElse(None)

  def set[A](page: Settable[A], value: A, cleanup: Boolean = true)(implicit writes: Writes[A]): Try[UserAnswers] = {

    val updatedData = data.setObject(page.path, Json.toJson(value)) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(errors) =>
        Failure(JsResultException(errors))
    }

    updatedData.flatMap {
      d =>
        val updatedAnswers = copy(data = d)
        if(cleanup) { page.cleanup(Some(value), updatedAnswers) } else { Try { updatedAnswers } }
    }
  }

    /**
    * Sets the answer (value) to the given question (settable), and returns a failed future  if something goes 
    * wrong - unlike set() which returns a failed Try
    * @param settable - question we're setting the answer / data for
    * @param value - value to set
    * @param cleanup - true to call the question's cleanup method
    * @tparam A - type of 'value'
    * @return Future of updated UserAnswers
    */
  def setUnsafe[A](settable: Settable[A], value: A, cleanup: Boolean = true) 
    (implicit writes: Writes[A]): UserAnswers = 
    set(settable, value, cleanup).get

  /**
    * Saves this UserAnswers using the given function
    * @param saveUserAnswerFunc - function called to save user answers
    * @return Future of updated UserAnswers
    */
  def save(saveUserAnswerFunc: SaveUserAnswerFunc) (implicit ec: ExecutionContext): Future[UserAnswers] = {
    saveUserAnswerFunc
      .apply(this, true)
      .map(_ => this)
  }

  /** If user's answer has changed, passes updated user-answers object to given save function  
    *
    * @param questionPage       - the user-answer we might be changing
    * @param newValue           - the user's answer
    * @param saveUserAnswerFunc - function to call if answer has changed
    * @param format             - formatter for user's answer object type
    * @tparam A - type of user's answer
    * @return
    *  - Future of false if user's answer is the same as the current value
    *  - Future of true if user's answer has changed
    */
  def change[A](questionPage: QuestionPage[A], newValue: A, saveUserAnswerFunc: SaveUserAnswerFunc)
    (implicit format: Format[A]): Future[Boolean] = {
    val updatedUserAnswers = set(questionPage, newValue).get
    if (get(questionPage).contains(newValue))
      Future.successful(false)
    else {
      saveUserAnswerFunc.apply(updatedUserAnswers, true)
    }
  }

  /**
    * Removes all answers, preserves id, updates timestamp  
    * @return UserAnswers with all answers removed
    */
  def reset: UserAnswers = copy(data = Json.obj(), lastUpdated = Instant.now)


  def remove[A](page: Settable[A]): Try[UserAnswers] = {

    val updatedData = data.removeObject(page.path) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(_) =>
        Success(data)
    }

    updatedData.flatMap {
      d =>
        val updatedAnswers = copy(data = d)
        page.cleanup(None, updatedAnswers)
    }
  }

}

object UserAnswers {

  val reads: Reads[UserAnswers] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "_id").read[String] and
        (__ \ "data").read[JsObject] and
        (__ \ "lastUpdated").read(MongoJavatimeFormats.instantFormat)
    )(UserAnswers.apply _)
  }

  val writes: OWrites[UserAnswers] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "_id").write[String] and
        (__ \ "data").write[JsObject] and
        (__ \ "lastUpdated").write(MongoJavatimeFormats.instantFormat)
    )(unlift(UserAnswers.unapply))
  }

  implicit val format: OFormat[UserAnswers] = OFormat(reads, writes)

  type SaveUserAnswerFunc = (UserAnswers, Boolean) => Future[Boolean]

}
