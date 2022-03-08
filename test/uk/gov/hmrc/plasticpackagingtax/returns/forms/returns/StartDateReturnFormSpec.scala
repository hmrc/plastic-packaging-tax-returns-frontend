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

package uk.gov.hmrc.plasticpackagingtax.returns.forms.returns

import org.scalatestplus.play.PlaySpec
import play.api.data.Form
import uk.gov.hmrc.plasticpackagingtax.returns.forms.returns.StartDateReturnForm.{
  ErrorKey,
  FieldKey,
  YES
}

class StartDateReturnFormSpec extends PlaySpec {

  val form = StartDateReturnForm.form()

  "Start Date Return form" must {
    "bind" when {
      "yes" in {
        val ans: Form[Boolean] = form.bind(Map(FieldKey -> YES))

        ans.value mustBe Some(true)
        ans.errors mustBe empty
      }
      "no" in {
        val ans: Form[Boolean] = form.bind(Map(FieldKey -> StartDateReturnForm.NO))

        ans.value mustBe Some(false)
        ans.errors mustBe empty
      }
    }
    "error" when {
      "question is not submitted" in {
        val ans: Form[Boolean] = form.bind(Map.empty[String, String])

        ans.errors.map(_.message) mustBe Seq(ErrorKey)
        ans.value mustBe None
      }
      "jibberish is submitted" in {
        val ans: Form[Boolean] = form.bind(Map(FieldKey -> "jibberish"))

        ans.errors.map(_.message) mustBe Seq(ErrorKey)
        ans.value mustBe None
      }
    }
  }

}
