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

package forms

import base.utils.CommonTestUtils
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class CommonFormValidatorsSpec
    extends AnyWordSpec with Matchers with CommonFormValidators with CommonTestUtils {

  "isNonEmpty" should {

    "return true" when {

      "string is not empty" in {

        isNonEmpty("myString") mustBe true
      }
    }

    "isDigitsOnly" should {

      "return true" when {

        "value contains digits only" in {

          isDigitsOnly("1234567890") mustBe true
        }
      }
    }

    "isEqualToOrBelow" should {
      "return true" when {
        "value is equal to threshold" in {

          isEqualToOrBelow("1", 1) mustBe true
        }

        "value is below threshold" in {

          isEqualToOrBelow("1", 4) mustBe true
        }
      }
    }
  }

  "isLowerOrEqual" should {
    "return false" when {
      "value is not a number" in {

        isLowerOrEqualTo(2)("a") mustBe false
      }

      "value is above threshold" in {

        isLowerOrEqualTo(5)("6") mustBe false
      }
    }

    "return true" when {
      "value is below threshold" in {

        isLowerOrEqualTo(5)("2") mustBe true
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

    "isDigitsOnly" should {

      "return false" when {
        "value contains alpha character" in {

          isDigitsOnly("123a56f7%8&9 0") mustBe false
        }

        "value is empty" in {

          isDigitsOnly("") mustBe false
        }
      }
    }

    "isEqualToOrBelow" should {
      "return false" when {
        "value is empty" in {

          isEqualToOrBelow("", 1) mustBe false
        }

        "value is above threshold" in {

          isEqualToOrBelow("2", 1) mustBe false
        }
      }
    }
  }
}
