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

package forms.changeGroupLead

import org.scalatestplus.play.PlaySpec
import play.api.data.FormError

class MainContactJobTitleFormProviderSpec extends PlaySpec {

  val requiredKey = "mainContactJobTitle.error.required"
  val lengthKey = "mainContactJobTitle.error.length"
  val maxLength = 155

  val form = new MainContactJobTitleFormProvider()()

  def error(key: String) =
    Seq(FormError("value", Seq(key)))

  "form" must {
    "bind" when {
      "valid input" in {
        val bound = form.bind(Map("value" -> "Teacher"))
        bound.value mustBe Some("Teacher")
        bound.errors mustBe Seq.empty
      }
    }
    "error" when {
      "no value" in {
        val bound = form.bind(Map[String, String]())
        bound.errors mustBe error("mainContactJobTitle.error.required")
        bound.value mustBe None
      }

      "value is empty" in {
        val bound = form.bind(Map("value" -> ""))
        bound.errors mustBe error("mainContactJobTitle.error.required")
        bound.value mustBe None
      }

      "value is just spaces" in {
        val bound = form.bind(Map("value" -> "  "))
        bound.errors mustBe error("mainContactJobTitle.error.required")
        bound.value mustBe None
      }

      "value is above the max length" in {
        val bound = form.bind(Map("value" -> "a" * 161))
        bound.errors mustBe error("mainContactJobTitle.error.length")
        bound.value mustBe None
      }

      "value contains numbers" in {
        val bound = form.bind(Map("value" -> "1"))
        bound.errors mustBe error("mainContactJobTitle.error.invalid")
        bound.value mustBe None
      }

      "value contains special chars" in {
        val bound = form.bind(Map("value" -> "$h!t"))
        bound.errors mustBe error("mainContactJobTitle.error.invalid")
        bound.value mustBe None
      }
    }
  }

}
