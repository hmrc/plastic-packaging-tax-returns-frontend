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
import uk.gov.hmrc.plasticpackagingtax.returns.forms.ConvertedPackagingCredit.{
  aboveMaxError,
  creditEmptyError,
  invalidFormatError,
  maxTotalCredit,
  totalInPounds
}

class ConvertedPackagingCreditSpec extends AnyWordSpec with Matchers {

  "Converted packaging credit validation rules" should {

    "return success" when {

      "totalInPence is withing allowed range" in {

        val input = Map(totalInPounds -> "100")

        val form = ConvertedPackagingCredit.form().bind(input)
        form.errors.size mustBe 0
      }

      "totalInPence allows two decimal values" in {

        val input = Map(totalInPounds -> "100.34")

        val form = ConvertedPackagingCredit.form().bind(input)
        form.errors.size mustBe 0
      }

      "totalInPence is 0" in {

        val input = Map(totalInPounds -> "0")

        val form = ConvertedPackagingCredit.form().bind(input)
        form.errors.size mustBe 0
      }

      "totalInPence is exactly max allowed amount" in {

        val input =
          Map(totalInPounds -> maxTotalCredit.toString)

        val form = ConvertedPackagingCredit.form().bind(input)
        form.errors.size mustBe 0
      }

      "totalInPence contains spaces before and after value" in {

        val input =
          Map(totalInPounds -> " 3 ")

        val form = ConvertedPackagingCredit.form().bind(input)
        form.errors.size mustBe 0
      }
    }

    "return errors" when {

      "provided with empty data" in {

        val input = Map(totalInPounds -> "")
        val expectedErrors =
          Seq(FormError(totalInPounds, creditEmptyError))

        testFailedValidationErrors(input, expectedErrors)
      }

      "totalInPence contains alphanumerical or special character" in {

        val input = Map(totalInPounds -> "20A #,")
        val expectedErrors =
          Seq(FormError(totalInPounds, invalidFormatError))

        testFailedValidationErrors(input, expectedErrors)
      }

      "totalInPence contains negative number" in {

        val input          = Map(totalInPounds -> "-1")
        val expectedErrors = Seq(FormError(totalInPounds, invalidFormatError))

        testFailedValidationErrors(input, expectedErrors)
      }

      "totalInPence contains more than two decimals" in {

        val input          = Map(totalInPounds -> "5.467")
        val expectedErrors = Seq(FormError(totalInPounds, invalidFormatError))

        testFailedValidationErrors(input, expectedErrors)
      }

      "totalInPence is more than maximum allowed weight" in {

        val input = Map(totalInPounds -> (maxTotalCredit + 1).toString)
        val expectedErrors =
          Seq(FormError(totalInPounds, aboveMaxError))

        testFailedValidationErrors(input, expectedErrors)
      }
    }
  }

  def testFailedValidationErrors(
    input: Map[String, String],
    expectedErrors: Seq[FormError]
  ): Unit = {
    val form = ConvertedPackagingCredit.form().bind(input)
    expectedErrors.foreach(form.errors must contain(_))
  }

}
