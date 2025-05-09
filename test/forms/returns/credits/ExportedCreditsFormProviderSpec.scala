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

package forms.returns.credits

import org.scalatestplus.play.PlaySpec
import play.api.data.{Form, FormError}

class ExportedCreditsFormProviderSpec extends PlaySpec {

  val sut: Form[Boolean] = new ExportedCreditsFormProvider().apply()

  "The form" must {

    "bind correctly" when {
      "yes is provided" in {

        val boundForm = sut.bind(Map("value" -> "true"))
        boundForm.value mustBe Some(true)
        boundForm.errors mustBe Nil
      }
      "no is provided with no weight" in {
        val boundForm = sut.bind(Map("value" -> "false"))
        boundForm.value mustBe Some(false)
        boundForm.errors mustBe Nil
      }
    }
    "radio errors" when {
      "answer is non boolean" in {
        val boundForm = sut.bind(Map("value" -> ""))
        boundForm.value mustBe None
        boundForm.errors mustBe Seq(FormError("value", "exportedCredits.error.required"))
      }

      "answer is empty" in {
        val boundForm = sut.bind(Map.empty[String, String])
        boundForm.value mustBe None
        boundForm.errors mustBe Seq(FormError("value", "exportedCredits.error.required"))
      }
    }

//    "weight input errors" when {
//      "nothing entered in weight field" in {
//        val boundForm = sut.bind(Map("answer" -> "true", "exported-credits-weight" -> ""))
//        boundForm.value mustBe None
//      }
//
//      "entered weight is below 1" in {
//        val boundForm = sut.bind(Map("answer" -> "true", "exported-credits-weight" -> "-1"))
//        boundForm.value mustBe None
//      }
//
//      "entered weight is below above max" in {
//        val boundForm = sut.bind(Map("answer" -> "true", "exported-credits-weight" -> "100000000000"))
//        boundForm.value mustBe None
//      }
//
//      "entered weight is only non numeric" in {
//        val boundForm = sut.bind(Map("answer" -> "true", "exported-credits-weight" -> "porridge"))
//        boundForm.value mustBe None
//      }
//
//      "entered weight is decimal" in {
//        val boundForm = sut.bind(Map("answer" -> "true", "exported-credits-weight" -> "4.20"))
//        boundForm.value mustBe None
//      }
//
//    }

  }
}
