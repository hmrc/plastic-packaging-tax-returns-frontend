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

package forms

import play.api.data.validation.Constraint
import play.api.data.{FormError, Mapping}
import uk.gov.voa.play.form.Condition

case class ConditionalMapping[T](
  condition: Condition,
  wrapped: Mapping[T],
  opWrapped: Mapping[T],
  constraints: Seq[Constraint[T]] = Nil,
  keys: Set[String] = Set()
) extends Mapping[T] {

  override val format: Option[(String, Seq[Any])] = wrapped.format

  val key = wrapped.key

  override def verifying(addConstraints: Constraint[T]*): Mapping[T] =
    this.copy(constraints = constraints ++ addConstraints.toSeq)

  def bind(data: Map[String, String]): Either[Seq[FormError], T] =
    if (condition(data)) wrapped.bind(data)
    else opWrapped.bind(data)

  def unbind(value: T): Map[String, String] =
    wrapped.unbind(value) ++
      opWrapped.unbind(value)

  def unbindAndValidate(value: T): (Map[String, String], Seq[FormError]) = {
    val w  = wrapped.unbindAndValidate(value)
    val op = opWrapped.unbindAndValidate(value)
    (w._1 ++ op._1, w._2 ++ op._2)
  }

  def withPrefix(prefix: String): Mapping[T] =
    copy(wrapped = wrapped.withPrefix(prefix), opWrapped = opWrapped.withPrefix(prefix))

  val mappings: Seq[Mapping[_]] = wrapped.mappings :+ opWrapped :+ this
}
