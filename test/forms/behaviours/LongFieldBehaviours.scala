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

package forms.behaviours

import play.api.data.{Form, FormError}

trait LongFieldBehaviours extends FieldBehaviours {

  def longField(
    form: Form[_],
    fieldName: String,
    nonNumericError: FormError,
    wholeNumberError: FormError
  ): Unit = {

    "not bind non-numeric numbers" in {

      forAll(nonNumerics -> "nonNumeric") {
        nonNumeric =>
          val result = form.bind(Map(fieldName -> nonNumeric)).apply(fieldName)
          result.errors must contain only nonNumericError
      }
    }

    "not bind decimals" in {

      forAll(decimals -> "decimal") {
        decimal =>
          val result = form.bind(Map(fieldName -> decimal)).apply(fieldName)
          result.errors must contain only wholeNumberError
      }
    }

    "not bind integers larger than Long.MaxValue" in {

      forAll(longsLargerThanMaxValue -> "massiveLong") {
        num: BigInt =>
          val result = form.bind(Map(fieldName -> num.toString)).apply(fieldName)
          result.errors must contain only nonNumericError
      }
    }

    "not bind integers smaller than Long.MinValue" in {

      forAll(longsSmallerThanMinValue -> "massivelySmallLong") {
        num: BigInt =>
          val result = form.bind(Map(fieldName -> num.toString)).apply(fieldName)
          result.errors must contain only nonNumericError
      }
    }
  }

  def longFieldWithMinimum(
    form: Form[_],
    fieldName: String,
    minimum: Long,
    expectedError: FormError
  ): Unit =
    s"not bind long integers below $minimum" in {

      forAll(longsBelowValue(minimum) -> "longBelowMin") {
        number: Long =>
          val result = form.bind(Map(fieldName -> number.toString)).apply(fieldName)
          result.errors must contain only expectedError
      }
    }

  def longFieldWithMaximum(
    form: Form[_],
    fieldName: String,
    maximum: Long,
    expectedError: FormError
  ): Unit =
    s"not bind long integers above $maximum" in {

      forAll(longsAboveValue(maximum) -> "longAboveMax") {
        number: Long =>
          val result = form.bind(Map(fieldName -> number.toString)).apply(fieldName)
          result.errors must contain only expectedError
      }
    }

  def longFieldWithRange(
    form: Form[_],
    fieldName: String,
    minimum: Long,
    maximum: Long,
    expectedError: FormError
  ): Unit =
    s"not bind LONG integers outside the range $minimum to $maximum" in {

      forAll(longsOutsideRange(minimum, maximum) -> "longOutsideRange") {
        number =>
          val result = form.bind(Map(fieldName -> number.toString)).apply(fieldName)
          result.errors must contain only expectedError
      }
    }

}
