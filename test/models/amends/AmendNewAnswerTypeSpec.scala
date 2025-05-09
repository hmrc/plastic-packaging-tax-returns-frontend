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

package models.amends

import models.amends.AmendNewAnswerType.{AnswerWithValue, AnswerWithoutValue}
import org.scalatestplus.play.PlaySpec

class AmendNewAnswerTypeSpec extends PlaySpec {

  "AmendNewAnswerType apply" should {
    "create an instance of AnswerWithValue with no message" when {
      "value is present" in {
        val res = AmendNewAnswerType(Some("value"), "message")

        assert(res.isInstanceOf[AnswerWithValue])
        res.asInstanceOf[AnswerWithValue].value mustEqual "value"
      }

      "value is not present" in {
        val res = AmendNewAnswerType(None, "hidden message")

        assert(res.isInstanceOf[AnswerWithoutValue])
        res.asInstanceOf[AnswerWithoutValue].hiddenMessage mustEqual "hidden message"
      }

      "is in amending Mode" in {
        val res = AmendNewAnswerType("2kg", "message", true)

        assert(res.isInstanceOf[AnswerWithValue])
        res.asInstanceOf[AnswerWithValue].value mustEqual "2kg"
      }

      "is first amending" in {
        val res = AmendNewAnswerType("2kg", "message", false)

        assert(res.isInstanceOf[AnswerWithoutValue])
        res.asInstanceOf[AnswerWithoutValue].hiddenMessage mustEqual "message"
      }
    }
  }
}
