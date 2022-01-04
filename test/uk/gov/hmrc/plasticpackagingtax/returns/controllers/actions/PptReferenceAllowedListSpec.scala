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

package uk.gov.hmrc.plasticpackagingtax.returns.controllers.actions

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar

class PptReferenceAllowedListSpec extends AnyWordSpec with Matchers with MockitoSugar {

  "ppt reference allow list" when {
    "is empty" should {
      "allow everyone" in {
        val pptReferenceAllowedList = new PptReferenceAllowedList(Seq.empty)
        pptReferenceAllowedList.isAllowed("12345") mustBe true
        pptReferenceAllowedList.isAllowed("0987") mustBe true
      }
    }
    "has elements" should {
      val pptReferenceAllowedList = new PptReferenceAllowedList(Seq("12345"))
      "allow listed ppt reference" in {
        pptReferenceAllowedList.isAllowed("12345") mustBe true
      }
      "disallow not listed ppt reference" in {
        pptReferenceAllowedList.isAllowed("0987") mustBe false
      }
    }
  }
}
