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

package forms.returns.credits

import models.returns.CreditRangeOption
import org.scalatestplus.play.PlaySpec
import play.api.data.FormError

import java.time.LocalDate

class ClaimForWhichYearFormProviderSpec extends PlaySpec {

  val sut = new ClaimForWhichYearFormProvider

  "form" must {
    "bind" when {
      "one of the years is selected" in {
        val option = CreditRangeOption(LocalDate.MIN, LocalDate.MAX)

        val form = sut.apply(Seq(option))
        val bound = form.bind(Map("value" -> option.key))

        bound.errors mustBe Nil
        bound.value mustBe Some(option)
      }
    }

    "Error" when {
      "The option is not in the form" in {
        val option = CreditRangeOption(LocalDate.MIN, LocalDate.MAX)

        val form = sut.apply(Seq.empty)
        val bound = form.bind(Map("value" -> option.key))

        bound.value mustBe None
        bound.errors mustBe Seq(FormError("value", "claim-for-which-year.error.required"))
      }
      "The value is not correct" in {
        val option = CreditRangeOption(LocalDate.MIN, LocalDate.MAX)

        val form = sut.apply(Seq(option))
        val bound = form.bind(Map("value" -> "blah-not-correct"))

        bound.value mustBe None
        bound.errors mustBe Seq(FormError("value", "claim-for-which-year.error.required"))
      }
      "The value is not provided" in {
        val option = CreditRangeOption(LocalDate.MIN, LocalDate.MAX)

        val form = sut.apply(Seq(option))
        val bound = form.bind(Map("value" -> ""))

        bound.value mustBe None
        bound.errors mustBe Seq(FormError("value", "claim-for-which-year.error.required"))
      }
      "The binding is malformed" in {
        val option = CreditRangeOption(LocalDate.MIN, LocalDate.MAX)

        val form = sut.apply(Seq(option))
        val bound = form.bind(Map("blah" -> "bloop"))

        bound.value mustBe None
        bound.errors mustBe Seq(FormError("value", "claim-for-which-year.error.required"))
      }
      "The binding is not provided" in {
        val option = CreditRangeOption(LocalDate.MIN, LocalDate.MAX)

        val form = sut.apply(Seq(option))
        val bound = form.bind(Map.empty[String, String])

        bound.value mustBe None
        bound.errors mustBe Seq(FormError("value", "claim-for-which-year.error.required"))
      }
    }
  }

}
