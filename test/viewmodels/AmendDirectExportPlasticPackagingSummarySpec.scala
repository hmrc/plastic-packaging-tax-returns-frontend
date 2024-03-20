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

package viewmodels

import cacheables.ReturnDisplayApiCacheable
import models.UserAnswers
import models.amends.{AmendNewAnswerType, AmendSummaryRow}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import pages.amends.{AmendDirectExportPlasticPackagingPage, AmendExportedByAnotherBusinessPage}
import support.AmendExportedData
import viewmodels.checkAnswers.amends.AmendDirectExportPlasticPackagingSummary

class AmendDirectExportPlasticPackagingSummarySpec extends PlaySpec with AmendExportedData with BeforeAndAfterEach {

  private val answer = UserAnswers("123")
    .set(ReturnDisplayApiCacheable, retDisApi).get
    .set(AmendDirectExportPlasticPackagingPage, 10L).get
    .set(AmendExportedByAnotherBusinessPage, 5L).get

  override def beforeEach(): Unit =
    super.beforeEach()

  "Summary" should {
    "return a summary row with amended value" in {

      val expected = AmendSummaryRow(
        "amendDirectExportPlasticPackaging.checkYourAnswersLabel",
        "4kg",
        AmendNewAnswerType(Some("15kg"), "AmendsCheckYourAnswers.hiddenCell.newAnswer.1"),
        Some(("export", controllers.amends.routes.AmendExportedPlasticPackagingController.onPageLoad.url))
      )

      AmendDirectExportPlasticPackagingSummary(answer) mustEqual expected
    }

    "return a summary row empty amended value" in {

      val ans = answer
        .remove(AmendDirectExportPlasticPackagingPage).get
        .remove(AmendExportedByAnotherBusinessPage).get

      val expected = AmendSummaryRow(
        "amendDirectExportPlasticPackaging.checkYourAnswersLabel",
        "4kg",
        AmendNewAnswerType(None, "AmendsCheckYourAnswers.hiddenCell.newAnswer.1"),
        Some(("export", controllers.amends.routes.AmendExportedPlasticPackagingController.onPageLoad.url))
      )

      AmendDirectExportPlasticPackagingSummary(ans) mustEqual expected
    }

    "throw if ReturnDisplayApi is missing" in {
      intercept[IllegalArgumentException] {
        AmendDirectExportPlasticPackagingSummary(answer.remove(ReturnDisplayApiCacheable).get)
      }
    }
  }

}
