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
import scala.concurrent.Future
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
    * Attempts to change the value represented by settable to newValue. 
    * @return
    *  - Some(UserAnswers) => user answers with updated value
    *  - None => new value matches existing value, nothing changed
    *  @throws Exception if changing the value or cleanup failed
    */
  def change[A] (questionPage: QuestionPage[A], newValue: A) (implicit format: Format[A]): Option[UserAnswers] = 
    if (get(questionPage).contains(newValue))
      None
    else 
      Some(set(questionPage, newValue).get)

  /** If user's answer has changed, passes updated user-answers object to given save function  
    * @param questionPage - the user-answer we might be changing
    * @param newValue - the user's answer
    * @param saveUserAnswerFunc - function to call if answer has changed
    * @param format - formatter for user's answer object type
    * @tparam A - type of user's answer
    * @return
    *  - Future of false if user's answer is the same as the current value
    *  - Future of true if user's answer has changed
    */
  def change_v3[A] (questionPage: QuestionPage[A], newValue: A, saveUserAnswerFunc: SaveUserAnswerFunc) 
    (implicit format: Format[A]): Future[Boolean] =
    if (get(questionPage).contains(newValue))
      Future.successful(false)
    else {
      val updatedUserAnswers = set(questionPage, newValue).get
      saveUserAnswerFunc.apply(updatedUserAnswers, true)
    }
        
    

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
