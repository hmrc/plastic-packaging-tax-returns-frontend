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

package viewmodels.checkYourAnswer.amends

import models.UserAnswers
import models.amends.AmendNewAnswerType.{AnswerWithValue, AnswerWithoutValue}
import models.amends.AmendSummaryRow
import models.returns.{AmendsCalculations, Calculations}
import org.mockito.ArgumentMatchers.{eq => meq}
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar.{mock, reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import pages.amends.{AmendImportedPlasticPackagingPage, AmendManufacturedPlasticPackagingPage}
import queries.Gettable
import viewmodels.checkAnswers.amends.AmendTotalPlasticPackagingSummary

class AmendTotalPlasticPackagingSummarySpec extends PlaySpec with BeforeAndAfterEach {

  private val ans = mock[UserAnswers]
  private val calculations = AmendsCalculations(
    Calculations(1, 2, 200, 100, true, 0.2),
    Calculations(1, 2, 100, 100, true, 0.2)
  )

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(ans)

    when(ans.get(any[Gettable[Long]])(any)).thenReturn(None)
  }

  "AmendTotalPlasticPackagingSummarySpec" should {

    "get the amended exported by you weight" in {
      AmendTotalPlasticPackagingSummary(calculations, ans)

      verify(ans).get(meq(AmendManufacturedPlasticPackagingPage))(any)
    }

    "get the amended exported by another Business weight" in {
      AmendTotalPlasticPackagingSummary(calculations, ans)

      verify(ans).get(meq(AmendImportedPlasticPackagingPage))(any)
    }

    "return a summary row with amended value" in {

      when(ans.get(any[Gettable[Long]])(any)).thenReturn(Some(10L))

      AmendTotalPlasticPackagingSummary(calculations, ans) mustEqual
        AmendSummaryRow(
          "AmendsCheckYourAnswers.packagingTotal",
          "100kg",
          AnswerWithValue("100kg"),
          None
        )
    }

    "return a summary row without amended value and a hidden message" in {

      AmendTotalPlasticPackagingSummary(calculations, ans) mustEqual
        AmendSummaryRow(
          "AmendsCheckYourAnswers.packagingTotal",
          "100kg",
          AnswerWithoutValue("AmendsCheckYourAnswers.hiddenCell.newAnswer.2"),
          None
        )
    }
  }
}
