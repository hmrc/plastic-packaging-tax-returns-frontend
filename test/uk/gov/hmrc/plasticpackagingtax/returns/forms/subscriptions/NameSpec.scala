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

package uk.gov.hmrc.plasticpackagingtax.returns.forms.subscriptions

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.data.FormError
import uk.gov.hmrc.plasticpackagingtax.returns.forms.subscriptions.Name.{
  emptyError,
  formatError,
  lengthError
}

class NameSpec extends AnyWordSpec with Matchers {
  "Subscription Primary Contact Name form" should {
    "return success" when {
      "name is valid" in {

        val input = Map("value" -> "Jack Gatsby")

        val form = Name.form().bind(input)
        form.errors.size mustBe 0
      }

      "name is up to 160 characters long" in {

        val input = Map("value" -> "J" * 160)

        val form = Name.form().bind(input)
        form.errors.size mustBe 0
      }

    }
    "return error" when {
      "name is empty" in {

        val input = Map("value" -> "")

        val form = Name.form().bind(input)
        form.errors.size mustBe 1
        val expectedErrors: Seq[FormError] = Seq(FormError(Name.value, emptyError))
        testFailedValidationErrors(input, expectedErrors)
      }

      "name is over 160 characters long" in {

        val input = Map("value" -> "J J" * 160)

        val form = Name.form().bind(input)
        form.errors.size mustBe 2
        val expectedErrors: Seq[FormError] =
          Seq(FormError(Name.value, lengthError), FormError(Name.value, formatError))
        testFailedValidationErrors(input, expectedErrors)
      }

      "name is in the incorrect format" in {

        val input = Map("value" -> "J[***]ck <script>")

        val form = Name.form().bind(input)
        form.errors.size mustBe 1
        val expectedErrors: Seq[FormError] = Seq(FormError(Name.value, formatError))
        testFailedValidationErrors(input, expectedErrors)

      }

    }
  }

  def testFailedValidationErrors(
    input: Map[String, String],
    expectedErrors: Seq[FormError]
  ): Unit = {
    val form = Name.form().bind(input)
    expectedErrors.foreach(form.errors must contain(_))
  }

}
