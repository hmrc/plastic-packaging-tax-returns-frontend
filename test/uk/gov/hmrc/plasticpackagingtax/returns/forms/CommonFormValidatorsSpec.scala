/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.plasticpackagingtax.returns.forms

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.plasticpackagingtax.returns.base.unit.CommonTestUtils

class CommonFormValidatorsSpec
    extends AnyWordSpec with Matchers with CommonFormValidators with CommonTestUtils {

  "isNonEmpty" should {

    "return true" when {

      "string is not empty" in {

        isNonEmpty("myString") mustBe true
      }

      "string optional is not empty" in {

        isNotEmpty(Some("myString")) mustBe true
      }
    }

    "isDigitsOnly" should {

      "return true" when {

        "value contains digits only" in {

          isDigitsOnly(Some("1234567890")) mustBe true
        }
      }
    }

    "isEqualToOrBelow" should {
      "return true" when {
        "value is equal to threshold" in {

          isEqualToOrBelow(Some("1"), 1) mustBe true
        }

        "value is below threshold" in {

          isEqualToOrBelow(Some("1"), 4) mustBe true
        }
      }
    }
  }

  "isNonEmpty" should {

    "return false" when {

      "string is empty" in {

        isNonEmpty("") mustBe false
      }

      "null value passed" in {

        isNonEmpty(null) mustBe false
      }
    }

    "isNotEmpty" should {

      "return false" when {
        "optional is empty" in {

          isNotEmpty(None) mustBe false
        }

        "optional string is empty" in {

          isNotEmpty(Some("")) mustBe false
        }
      }
    }

    "isDigitsOnly" should {

      "return false" when {
        "value contains alpha character" in {

          isDigitsOnly(Some("123a56f7%8&9 0")) mustBe false
        }

        "value is empty" in {

          isDigitsOnly(Some("")) mustBe false
        }
      }
    }

    "isEqualToOrBelow" should {
      "return false" when {
        "value is empty" in {

          isEqualToOrBelow(Some(""), 1) mustBe false
        }

        "value is above threshold" in {

          isEqualToOrBelow(Some("2"), 1) mustBe false
        }
      }
    }
  }
}
