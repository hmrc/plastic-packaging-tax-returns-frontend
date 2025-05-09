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

package models.requests

import models.SignedInUser
import org.scalatestplus.play.PlaySpec
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.Enrolments

class IdentifiedRequestSpec extends PlaySpec {

  val request = FakeRequest("GET", "/target")

  val sut = IdentifiedRequest(
    request,
    SignedInUser(Enrolments(Set.empty), IdentityData("the-internal-id", None)),
    "the-ppt-reference"
  )

  "cacheKey" must {
    "be created" in {
      sut.cacheKey mustBe "the-internal-id-the-ppt-reference"
    }
  }

  "headerCarrier" must {
    "create from request" in {
      val result = sut.headerCarrier

      result.otherHeaders mustBe List(("Host", "localhost"), ("path", "/target"))
    }
  }
}
