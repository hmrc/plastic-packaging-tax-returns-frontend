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

    "not bind longs larger than Long.MaxValue" in {
      val tooBig: BigInt = BigInt(Long.MaxValue) + 1
      val result = form.bind(Map(fieldName -> tooBig.toString)).apply(fieldName)
      result.errors must contain only nonNumericError
    }

    "not bind longs smaller than Long.MinValue" in {
      val tooSmall: BigInt = BigInt(Long.MinValue) - 1
      val result = form.bind(Map(fieldName -> tooSmall.toString)).apply(fieldName)
      result.errors must contain only nonNumericError
    }
  }

  def longFieldWithMinimum(
    form: Form[_],
    fieldName: String,
    minimum: Long,
    expectedError: FormError
  ): Unit = {

    s"not bind long integers below $minimum" in {
      val tooSmall = minimum - 1
      val result = form.bind(Map(fieldName -> tooSmall.toString)).apply(fieldName)
      result.errors must contain only expectedError
    }

    s"do bind long integers equal to $minimum" in {
      val tooSmall = minimum
      val result = form.bind(Map(fieldName -> tooSmall.toString)).apply(fieldName)
      result.errors mustBe empty
    }

    s"do bind long integers above $minimum" in {
      val tooBig = minimum + 1
      val result = form.bind(Map(fieldName -> tooBig.toString)).apply(fieldName)
      result.errors mustBe empty
    }

  }

  def longFieldWithMaximum(
    form: Form[_],
    fieldName: String,
    maximum: Long,
    expectedError: FormError
  ): Unit = {

    s"not bind long integers above $maximum" in {
      val tooBig = BigInt(maximum) + 1
      val result = form.bind(Map(fieldName -> tooBig.toString)).apply(fieldName)
      result.errors must contain only expectedError
    }

    s"do bind long integers equal to $maximum" in {
      val justRight = BigInt(maximum)
      val result = form.bind(Map(fieldName -> justRight.toString)).apply(fieldName)
      result.errors mustBe empty
    }

    s"do bind long integers less then $maximum" in {
      val justRight = BigInt(maximum) - 1
      val result = form.bind(Map(fieldName -> justRight.toString)).apply(fieldName)
      result.errors mustBe empty
    }
  }

  def longFieldWithRange(
    form: Form[_],
    fieldName: String,
    minimum: Long,
    maximum: Long,
    expectedError: FormError
  ): Unit = {
    longFieldWithMaximum(form, fieldName, maximum, expectedError)
    longFieldWithMinimum(form, fieldName, minimum, expectedError)
  }

  def numericStringExtractor(
                              form: Form[_],
                              fieldName: String,
                              outOfRangeError: FormError,
                              wholeNumberError: FormError
                            ): Unit = {

    val validInputs = Seq("10", "10suffix", "prefix10", "pre10suffix", "£+10symbolic")

    validInputs.foreach(input =>
      s"field extracts number from $input" in {

        val result = form.bind(Map(fieldName -> input))
        result.errors mustEqual Seq.empty
        result.value mustBe Some(10L)

      }
    )

    "field errors with decimal input" in {
      val result = form.bind(Map(fieldName -> "pre10.2suf")).apply(fieldName)
      result.errors must contain only wholeNumberError
    }

    "field errors with negative input" in {
      val result = form.bind(Map(fieldName -> "pre-10suf")).apply(fieldName)
      result.errors.map(_.message) mustBe Seq("manufacturedPlasticPackagingWeight.error.outOfRange.low")
    }
  }

}
