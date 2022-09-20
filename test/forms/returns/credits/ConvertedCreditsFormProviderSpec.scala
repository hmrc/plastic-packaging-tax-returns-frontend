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

package forms.returns.credits

import models.returns.CreditsAnswer
import org.scalatestplus.play.PlaySpec
import play.api.data.Form

class ConvertedCreditsFormProviderSpec extends PlaySpec {

  val sut: Form[CreditsAnswer] = new ConvertedCreditsFormProvider().apply()


  "bind correctly" when {
    "yes is provided" in {

      val boundForm = sut.bind(Map("answer" -> "true", "converted-credits-weight" -> "20"))
      boundForm.value mustBe Some(CreditsAnswer(yesNo = true, Some(20)))
      boundForm.errors mustBe Nil
    }
    "no is provided with no weight" in {
      val boundForm = sut.bind(Map("answer" -> "false"))
      boundForm.value mustBe Some(CreditsAnswer(yesNo = false, None))
      boundForm.errors mustBe Nil
    }
  }
  "radio errors" when {
    "answer is none boolean" in {
      val boundForm = sut.bind(Map("answer" -> "porridge", "converted-credits-weight" -> "20"))
      boundForm.value mustBe None
    }

    "answer is empty" in {
      val boundForm = sut.bind(Map.empty[String, String])
      boundForm.value mustBe None
    }
  }

  "weight input errors" when {
    "nothing entered in weight field" in {
      val boundForm = sut.bind(Map("answer" -> "true", "converted-credits-weight" -> ""))
      boundForm.value mustBe None
    }

    "entered weight is below 1" in {
      val boundForm = sut.bind(Map("answer" -> "true", "converted-credits-weight" -> "-1"))
      boundForm.value mustBe None
    }

    "entered weight is below above max" in {
      val boundForm = sut.bind(Map("answer" -> "true", "converted-credits-weight" -> "100000000000"))
      boundForm.value mustBe None
    }

    "entered weight is only non numeric" in {
      val boundForm = sut.bind(Map("answer" -> "true", "converted-credits-weight" -> "porridge"))
      boundForm.value mustBe None
    }

    "entered weight is decimal" in {
      val boundForm = sut.bind(Map("answer" -> "true", "converted-credits-weight" -> "4.20"))
      boundForm.value mustBe None
    }
  }

}
