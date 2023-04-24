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

  def fill[A](path: JsPath, form: Form[A])(implicit rds: Reads[A]): Form[A] =
    get(path).fold(form)(form.fill)


  /** Fills the given form by reading a user answer and passing that through the given function
    * @param gettable [[Gettable]] provides [[JsPath]] of the user answer
    * @param form [[Form]] to fill with answer
    * @param func function to extract form value from user answer 
    * @param reads [[Reads]] to de-serialise user answer 
    * @tparam FormValue type of the form's value 
    * @tparam AnswerType type of the user answer
    * @return the form as is, or a new form filled with the value returned by the given function. If the function return
    */
  def genericFill[FormValue, AnswerType](gettable: Gettable[AnswerType], form: Form[FormValue], 
    func: AnswerType => Option[FormValue]) (implicit reads: Reads[AnswerType]): Form[FormValue] = {
      get(gettable)
        .flatMap(func)
        .map(form.fill)
        .getOrElse(form) 
  }

  def get[A](page: Gettable[A])(implicit rds: Reads[A]): Option[A] =
    get(page.path)

  def get[A](path: JsPath)(implicit rds: Reads[A]): Option[A] =
    Reads.optionNoError(Reads.at(path)).reads(data).getOrElse(None)

  def getOrFail[A](page: Gettable[A])(implicit rds: Reads[A]): A = 
    getOrFail(page.path)

  def getOrFail[A](answerPath: String)(implicit rds: Reads[A]): A =
    getOrFail[A](JsPath \ answerPath)
    
  def getOrFail[A](answerPath: JsPath)(implicit rds: Reads[A]): A =
    Reads.at(answerPath).reads(data).get
    
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
    * Sets the answer (value) to the given question (settable), or fails if something goes 
    * wrong - unlike set() which returns a failed Try
    * @param settable - question we're setting the answer / data for
    * @param value - value to set
    * @param cleanup - true to call the question's cleanup method
    * @tparam A - type of 'value'
    * @return Future of updated UserAnswers
    */
  def setOrFail[A](settable: Settable[A], value: A, cleanup: Boolean = true) (implicit writes: Writes[A]): UserAnswers = 
    set(settable, value, cleanup).get

  def setOrFail[A](answerPath: String, value: A) (implicit writes: Writes[A]): UserAnswers =
    copy(data = data.setObject(JsPath \ answerPath, Json.toJson(value)).get)

  def setOrFail[A](answerPath: JsPath, value: A) (implicit writes: Writes[A]): UserAnswers =
    copy(data = data.setObject(answerPath, Json.toJson(value)).get)

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

  /** Change the value or the given user answer, or add the user answer if it does not exists. The value of the user
    * answer will be the value returned by `newValueFunc(previousValue)`, where `previousValue = None` if 
    * the user answer does not exist
    * @param questionPage source of path 
    * @param newValueFunc function to calculate new value, passed Some(previous value) if there was one, or None if
    *                     there was not a previous value
    * @param saveUserAnswerFunc function to save the updated user answers once new value applied
    * @tparam A type of user answer field's value
    * @return [[Future]] of Unit
    */
  def changeWithFunc[A](questionPage: QuestionPage[A], newValueFunc: Option[A] => A,  
    saveUserAnswerFunc: SaveUserAnswerFunc) (implicit format: Format[A], ec: ExecutionContext): Future[Unit] = {

    val previousValue = get(questionPage)
    val updatedUserAnswers = setOrFail(questionPage, newValueFunc(previousValue))
    saveUserAnswerFunc
      .apply(updatedUserAnswers, true)
      .map(_ => ())
  }

  /** If user's answer has changed, passes updated user-answers object to given save function  
    *
    * @param path               - [[JsPath]] to where this answer should be added / changed
    * @param newValue           - the user's answer
    * @param saveUserAnswerFunc - function to call if answer has changed
    * @param format             - formatter for user's answer object type
    * @tparam A - type of user's answer
    * @return
    *  - Future of false if user's answer is the same as the current value
    *  - Future of true if user's answer has changed
    */
  def changeWithPath[A](answerPath: JsPath, newValue: A, saveUserAnswerFunc: SaveUserAnswerFunc) (implicit format: Format[A]): Future[Boolean] = {
    val page = new QuestionPage[A] {
      override def path: JsPath = answerPath
    }
    change(page, newValue, saveUserAnswerFunc) // TODO
  }

  /**
    * Removes all answers, preserves id, updates timestamp  
    * @return UserAnswers with all answers removed
    */
  def reset: UserAnswers = copy(data = Json.obj(), lastUpdated = Instant.now)

  def remove[A](page: Settable[A],  cleanup: Boolean = true): Try[UserAnswers] = {

    val updatedData = data.removeObject(page.path) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(_) =>
        Success(data)
    }

    updatedData.flatMap {
      d =>
        val updatedAnswers = copy(data = d)
        if(cleanup) page.cleanup(None, updatedAnswers) else Try(updatedAnswers)
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
