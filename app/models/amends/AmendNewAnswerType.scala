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

package models.amends

import play.api.libs.json.{Json, OFormat}


sealed trait  AmendNewAnswerType{
  val value: Option[String]
  val hiddenMessage: String
}
case class AnswerWithValue(value: Option[String]) extends AmendNewAnswerType {
  override val hiddenMessage: String = ""
}

object AnswerWithValue {
  implicit def jsonFormats: OFormat[AnswerWithValue] =
    Json.using[Json.WithDefaultValues].format[AnswerWithValue]
}

case class AnswerWithoutValue(override val hiddenMessage: String ) extends AmendNewAnswerType {
 override val value: Option[String] = None
}

object AnswerWithoutValue {
  implicit def jsonFormats: OFormat[AnswerWithoutValue] =
    Json.using[Json.WithDefaultValues].format[AnswerWithoutValue]
}


object AmendNewAnswerType {
  def apply(value: Option[String], hiddenMessage: String, amendmentMade: Boolean): AmendNewAnswerType = {
    amendmentMade match {
      case true => AnswerWithValue(value)
      case _ => AnswerWithoutValue(hiddenMessage)
    }
  }

  def apply(value: Option[String], hiddenMessage: String): AmendNewAnswerType = {
    value match {
      case Some(_) => AnswerWithValue(value)
      case _ => AnswerWithoutValue(hiddenMessage)
    }
  }

  implicit def jsonFormats: OFormat[AmendNewAnswerType] =
    Json.using[Json.WithDefaultValues].format[AmendNewAnswerType]
}