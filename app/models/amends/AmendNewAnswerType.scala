/*
 * Copyright 2025 HM Revenue & Customs
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

sealed trait AmendNewAnswerType

object AmendNewAnswerType {

  final case class AnswerWithValue(value: String)            extends AmendNewAnswerType
  final case class AnswerWithoutValue(hiddenMessage: String) extends AmendNewAnswerType

  def apply(value: String, hiddenMessage: String, amendmentMade: Boolean): AmendNewAnswerType =
    amendmentMade match {
      case true => AnswerWithValue(value)
      case _    => AnswerWithoutValue(hiddenMessage)
    }

  def apply(value: Option[String], hiddenMessage: String): AmendNewAnswerType =
    value match {
      case Some(v) => AnswerWithValue(v)
      case _       => AnswerWithoutValue(hiddenMessage)
    }
}
