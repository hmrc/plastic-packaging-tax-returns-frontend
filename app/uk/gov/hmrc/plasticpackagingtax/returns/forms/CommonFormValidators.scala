/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.plasticpackagingtax.returns.forms

import com.google.common.base.Strings

import java.util.regex.Pattern

trait CommonFormValidators {

  val maxLength = 100

  val isNonEmpty: String => Boolean = value => !Strings.isNullOrEmpty(value) && value.trim.nonEmpty

  val isDigitsOnly: String => Boolean = value =>
    isNonEmpty(value) && isNotExceedingMaxLength(value, maxLength) && value.trim.chars().allMatch(
      c => Character.isDigit(c)
    )

  val isEqualToOrBelow: (String, Long) => Boolean = (value, limit) =>
    isNonEmpty(value) && isDigitsOnly(value) && value.trim.toLong <= limit

  val isValidDecimal: String => Boolean =
    (input: String) =>
      try isNonEmpty(input) &&
        BigDecimal(input.trim) >= 0 &&
        BigDecimal(input.trim).scale <= 2
      catch {
        case _: java.lang.NumberFormatException => false
      }

  val isLowerOrEqualTo: BigDecimal => String => Boolean = (threshold: BigDecimal) =>
    (input: String) => isValidDecimal(input) && BigDecimal(input.trim) <= threshold

  val isNotExceedingMaxLength: (String, Int) => Boolean = (value, maxLength) =>
    value.trim.length <= maxLength

  val isValidName: String => Boolean = (name: String) =>
    name.isEmpty || isMatchingPattern(name, namePattern)

  private val isMatchingPattern: (String, Pattern) => Boolean = (value, pattern) =>
    pattern.matcher(value).matches()

  private val namePattern =
    Pattern.compile("^[a-zA-Z0-9À-ÿ !#$%&''‘’\\\"“”«»()*+,./:;=?@\\[\\]£€¥\\\\—–‐-]{1,160}$")

}
