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
import play.api.data.FormError
import uk.gov.hmrc.plasticpackagingtax.returns.forms.ExportedPlasticWeight.{
  creditAboveMaxError,
  maxTotalKg,
  oneHundredMillion,
  totalKg,
  totalKgEmptyError,
  totalKgInvalidFormatError,
  totalValueForCredit,
  totalValueForCreditEmptyError,
  totalValueForCreditInvalidFormatError,
  weightAboveMaxError
}

class ExportedPlasticWeightSpec extends AnyWordSpec with Matchers {

  "Exported Plastic Weight validation rules" should {

    "return success" when {

      "totalKg and totalValueForCredit input values are valid" in {

        val input = Map(totalKg -> "100", totalValueForCredit -> "123.96")

        val form = ExportedPlasticWeight.form().bind(input)
        form.errors.size mustBe 0
      }

      "both values are 0" in {

        val input = Map(totalKg -> "0", totalValueForCredit -> "0.00")

        val form = ExportedPlasticWeight.form().bind(input)
        form.errors.size mustBe 0
      }

      "is exactly max allowed amount" in {

        val input =
          Map(totalKg             -> maxTotalKg.toString,
              totalValueForCredit -> (oneHundredMillion - 1).toString
          )

        val form = ExportedPlasticWeight.form().bind(input)
        form.errors.size mustBe 0
      }

      "contains spaces before and after value" in {

        val input =
          Map(totalKg -> " 3 ", totalValueForCredit -> " 3 ")

        val form = ExportedPlasticWeight.form().bind(input)
        form.errors.size mustBe 0
      }
    }

    "return errors" when {

      "provided with empty data" in {

        val input =
          Map(totalKg -> "", totalValueForCredit -> "")
        val expectedErrors =
          Seq(FormError(totalKg, totalKgEmptyError),
              FormError(totalValueForCredit, totalValueForCreditEmptyError)
          )

        testFailedValidationErrors(input, expectedErrors)
      }

      "contains invalid characters" in {

        val input = Map(totalKg -> "20A #", totalValueForCredit -> "66,898.00")
        val expectedErrors =
          Seq(FormError(totalKg, totalKgInvalidFormatError),
              FormError(totalValueForCredit, totalValueForCreditInvalidFormatError)
          )

        testFailedValidationErrors(input, expectedErrors)
      }

      "contains negative numbers" in {

        val input = Map(totalKg -> "-1", totalValueForCredit -> "-1000.00")
        val expectedErrors =
          Seq(FormError(totalKg, totalKgInvalidFormatError),
              FormError(totalValueForCredit, totalValueForCreditInvalidFormatError)
          )

        testFailedValidationErrors(input, expectedErrors)
      }

      "totalKg is bigger than maximum allowed weight" in {

        val input = Map(totalKg -> (maxTotalKg + 1).toString, totalValueForCredit -> "123.45")
        val expectedErrors =
          Seq(FormError(totalKg, weightAboveMaxError))

        testFailedValidationErrors(input, expectedErrors)
      }

      "totalValueForCredit is bigger than maximum credit" in {

        val input = Map(totalKg -> "10", totalValueForCredit -> oneHundredMillion.toString)
        val expectedErrors =
          Seq(FormError(totalValueForCredit, creditAboveMaxError))

        testFailedValidationErrors(input, expectedErrors)
      }
    }
  }

  def testFailedValidationErrors(
    input: Map[String, String],
    expectedErrors: Seq[FormError]
  ): Unit = {
    val form = ExportedPlasticWeight.form().bind(input)
    expectedErrors.foreach(form.errors must contain(_))
  }

}
