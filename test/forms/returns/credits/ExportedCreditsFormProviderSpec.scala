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

import org.scalatestplus.play.PlaySpec
import play.api.data.Form

class ExportedCreditsFormProviderSpec extends PlaySpec {

  val sut: Form[ExportedCreditsAnswer] = new ExportedCreditsFormProvider().apply()

  "The form" must {

    "bind correctly" when {
      "yes is provided" in {

        val boundForm = sut.bind(Map("answer" -> "true" , "converted-credits-weight" -> "20"))
println(boundForm)
        boundForm.value mustBe Some(ExportedCreditsAnswer(yesNo = true, Some(20)))
        boundForm.errors mustBe Nil
      }
      "no is provided with no weight" in {
        val boundForm = sut.bind(Map("answer" -> "false"))
        boundForm.value mustBe Some(ExportedCreditsAnswer(yesNo = false, None))
        boundForm.errors mustBe Nil
      }
    }

  }
}