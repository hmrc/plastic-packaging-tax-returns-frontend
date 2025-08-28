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

package forms.changeGroupLead

import models.subscription.Member
import org.scalatestplus.play.PlaySpec
import play.api.data.FormError

class SelectNewGroupLeadFormSpec extends PlaySpec {

  val sut: SelectNewGroupLeadForm = new SelectNewGroupLeadForm
  val requiredKey                 = "select-new-representative.error.required"

  "the form" should {
    "bind" when {
      "a value is in the members list" in {
        val boundForm = sut.apply(Seq(Member("abc", "1"))).bind(Map("value" -> "1"))

        boundForm.value mustBe Some(Member("abc", "1"))
        boundForm.errors mustBe empty
      }

      "a value is in the members list amoung others " in {
        val boundForm = sut.apply(Seq(Member("xyz", "1"), Member("abc", "2"))).bind(Map("value" -> "2"))

        boundForm.value mustBe Some(Member("abc", "2"))
        boundForm.errors mustBe empty
      }
    }
    "error" when {
      "the form is empty" in {
        val boundForm = sut.apply(Seq(Member("xyz", "1"), Member("abc", "2"))).bind(Map[String, String]())

        boundForm.errors mustBe Seq(FormError("value", requiredKey))
        boundForm.value mustBe None
      }
      "the form has no value" in {
        val boundForm = sut.apply(Seq(Member("xyz", "1"), Member("abc", "2"))).bind(Map("cheese" -> "2"))

        boundForm.errors mustBe Seq(FormError("value", requiredKey))
        boundForm.value mustBe None
      }
      "the form value is empty" in {
        val boundForm = sut.apply(Seq(Member("xyz", "1"), Member("abc", "2"))).bind(Map("value" -> ""))

        boundForm.errors mustBe Seq(FormError("value", requiredKey))
        boundForm.value mustBe None
      }

      "the value is not in the members list" in {
        val boundForm = sut.apply(Seq(Member("xyz", "1"), Member("abc", "2"))).bind(Map("value" -> "4"))

        boundForm.errors mustBe Seq(FormError("value", requiredKey))
        boundForm.value mustBe None
      }
    }
  }

}
