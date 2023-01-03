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

package forms.behaviours

import play.api.data.{Form, FormError}

trait DecimalFieldBehaviours extends FieldBehaviours {

  def decimalField(
    form: Form[_],
    fieldName: String,
    nonNumericError: FormError,
    invalidNumericError: FormError
  ): Unit = {

    "not bind non-numeric numbers" in {

      forAll(nonNumerics -> "nonNumeric") {
        nonNumeric =>
          val result = form.bind(Map(fieldName -> nonNumeric)).apply(fieldName)
          result.errors mustEqual Seq(nonNumericError)
      }
    }

    "not bind invalid decimals (over 2dp)" in {
      val result = form.bind(Map(fieldName -> "12.123")).apply(fieldName)
      result.errors mustEqual Seq(invalidNumericError)
    }
  }

  def fivePlaceDecimalField(
    form: Form[_],
    fieldName: String,
    nonNumericError: FormError,
    invalidNumericError: FormError
  ): Unit = {

    "not bind non-numeric numbers" in {

      forAll(nonNumerics -> "nonNumeric") {
        nonNumeric =>
          val result = form.bind(Map(fieldName -> nonNumeric)).apply(fieldName)
          result.errors mustEqual Seq(nonNumericError)
      }
    }

    "not bind invalid decimals (over 5dp)" in {
      val result = form.bind(Map(fieldName -> "12.123123")).apply(fieldName)
      result.errors mustEqual Seq(invalidNumericError)
    }
  }

  def decimalFieldWithMinimum(
    form: Form[_],
    fieldName: String,
    minimum: BigDecimal,
    expectedError: FormError
  ): Unit =
    "value is less than the minimum" in {

      val result = form.bind(Map(fieldName -> (minimum - 0.01).toString)).apply(fieldName)
      result.errors mustEqual Seq(expectedError)
    }

  def decimalFieldWithMaximum(
    form: Form[_],
    fieldName: String,
    maximum: BigDecimal,
    expectedError: FormError
  ): Unit =
    "value is greater than the maximum" in {

      val result = form.bind(Map(fieldName -> (maximum + 0.01).toString)).apply(fieldName)
      result.errors mustEqual Seq(expectedError)
    }

  def decimalFieldWithRange(
    form: Form[_],
    fieldName: String,
    minimum: BigDecimal,
    maximum: BigDecimal,
    expectedError: FormError
  ): Unit = {

    s"not bind  value is greater than the maximum $maximum" in {

      val result = form.bind(Map(fieldName -> (maximum + 0.01).toString)).apply(fieldName)
      result.errors mustEqual Seq(expectedError)
    }

    s"value is less than the minimum $minimum" in {

      val result = form.bind(Map(fieldName -> (minimum - 0.01).toString)).apply(fieldName)
      result.errors mustEqual Seq(expectedError)
    }
  }

}
