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

package models.subscription

import org.scalatestplus.play.PlaySpec

class IndividualDetailsSpec extends PlaySpec {

  val sut = IndividualDetails(Some("Title"), "First", Some("Middle"), "Last")

  "toDisplayString" must {
    "construct name" when {
      "has title" in {
        sut.toDisplayString mustBe "Title First Last"
      }
      "has no title" in {
        sut.copy(title = None).toDisplayString mustBe "First Last"
      }
    }
  }

}
