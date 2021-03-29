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
import uk.gov.hmrc.plasticpackagingtax.returns.forms.HumanMedicinesPlasticWeight.{
  aboveMaxError,
  invalidFormatError,
  maxTotalKg,
  totalKg,
  weightEmptyError
}

class HumanMedicinesPlasticWeightSpec extends AnyWordSpec with Matchers {

  "Human medicines plastic weight validation rules" should {

    "return success" when {

      "total is withing allowed range" in {

        val input = Map(totalKg -> "100")

        val form = HumanMedicinesPlasticWeight.form().bind(input)
        form.errors.size mustBe 0
      }

      "is 0" in {

        val input = Map(totalKg -> "0")

        val form = HumanMedicinesPlasticWeight.form().bind(input)
        form.errors.size mustBe 0
      }

      "is exactly max allowed amount" in {

        val input =
          Map(totalKg -> maxTotalKg.toString)

        val form = HumanMedicinesPlasticWeight.form().bind(input)
        form.errors.size mustBe 0
      }

      "contains spaces before and after value" in {

        val input =
          Map(totalKg -> " 3 ")

        val form = HumanMedicinesPlasticWeight.form().bind(input)
        form.errors.size mustBe 0
      }
    }

    "return errors" when {

      "provided with empty data" in {

        val input = Map(totalKg -> "")
        val expectedErrors =
          Seq(FormError(totalKg, weightEmptyError))

        testFailedValidationErrors(input, expectedErrors)
      }

      "contains alphanumerical or special character" in {

        val input = Map(totalKg -> "20A #,.")
        val expectedErrors =
          Seq(FormError(totalKg, invalidFormatError))

        testFailedValidationErrors(input, expectedErrors)
      }

      "contains negative number" in {

        val input          = Map(totalKg -> "-1")
        val expectedErrors = Seq(FormError(totalKg, invalidFormatError))

        testFailedValidationErrors(input, expectedErrors)
      }

      "total is more than maximum allowed weight" in {

        val input = Map(totalKg -> (maxTotalKg + 1).toString)
        val expectedErrors =
          Seq(FormError(totalKg, aboveMaxError))

        testFailedValidationErrors(input, expectedErrors)
      }
    }
  }

  def testFailedValidationErrors(
    input: Map[String, String],
    expectedErrors: Seq[FormError]
  ): Unit = {
    val form = HumanMedicinesPlasticWeight.form().bind(input)
    expectedErrors.foreach(form.errors must contain(_))
  }

}
