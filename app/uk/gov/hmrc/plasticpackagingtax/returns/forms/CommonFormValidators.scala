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

trait CommonFormValidators {

  val isNotEmpty: Option[String] => Boolean = value => value.nonEmpty && isNonEmpty(value.get)

  val isNonEmpty: String => Boolean = value => !Strings.isNullOrEmpty(value) && value.trim.nonEmpty

  val isDigitsOnly: Option[String] => Boolean = value =>
    isNotEmpty(value) && value.exists(v => v.trim.chars().allMatch(c => Character.isDigit(c)))

  val isEqualToOrBelow: (Option[String], Long) => Boolean = (value, limit) =>
    isNotEmpty(value) && isDigitsOnly(value) && value.exists(v => v.trim.toLong <= limit)

}
