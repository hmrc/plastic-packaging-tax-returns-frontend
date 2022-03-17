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

package uk.gov.hmrc.plasticpackagingtax.returns.forms

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.data.FormError
import uk.gov.hmrc.plasticpackagingtax.returns.forms.agents.ClientIdentifier
import uk.gov.hmrc.plasticpackagingtax.returns.forms.agents.ClientIdentifier.{
  formatError,
  identifier,
  identifierEmptyError,
  lengthError
}

class ClientIdentifierSpec extends AnyWordSpec with Matchers {

  "Client identifier validation rules" should {

    "return success" when {

      "total is valid" in {
        val input = Map(identifier -> "XMPPT0000000123")

        val form = ClientIdentifier.form().bind(input)
        form.errors.size mustBe 0
      }
    }

    "return errors" when {
      "provided with empty data" in {
        val input =
          Map(identifier -> " ")
        val expectedErrors =
          Seq(FormError(identifier, identifierEmptyError))

        testFailedValidationErrors(input, expectedErrors)
      }

      "provided with shorter than 15 character long input" in {
        val input =
          Map(identifier -> "XMPPT000000012")
        val expectedErrors =
          Seq(FormError(identifier, lengthError))

        testFailedValidationErrors(input, expectedErrors)
      }

      "provided with longer than 15 character input" in {
        val input =
          Map(identifier -> "XMPPT00000001234")
        val expectedErrors =
          Seq(FormError(identifier, lengthError))

        testFailedValidationErrors(input, expectedErrors)
      }

      "provided with an identifier which doesn't start with XMPTT" in {
        val input =
          Map(identifier -> "ABCDE0000000123")
        val expectedErrors =
          Seq(FormError(identifier, formatError))

        testFailedValidationErrors(input, expectedErrors)
      }

      "provided with an identifier with none digits in the tail" in {
        val input =
          Map(identifier -> "XMPPT0000000ABC")
        val expectedErrors =
          Seq(FormError(identifier, formatError))

        testFailedValidationErrors(input, expectedErrors)
      }
    }
  }

  def testFailedValidationErrors(
    input: Map[String, String],
    expectedErrors: Seq[FormError]
  ): Unit = {
    val form = ClientIdentifier.form().bind(input)
    expectedErrors.foreach(form.errors must contain(_))
  }

}
