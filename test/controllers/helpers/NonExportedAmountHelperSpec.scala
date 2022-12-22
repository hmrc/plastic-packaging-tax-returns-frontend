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

package controllers.helpers

import models.UserAnswers
import org.scalatestplus.play.PlaySpec
import pages.returns.{AnotherBusinessExportWeightPage, DirectlyExportedComponentsPage, ExportedPlasticPackagingWeightPage, ImportedPlasticPackagingPage, ImportedPlasticPackagingWeightPage, ManufacturedPlasticPackagingPage, ManufacturedPlasticPackagingWeightPage, PlasticExportedByAnotherBusinessPage}

class NonExportedAmountHelperSpec extends PlaySpec {

  private val userAnswer = UserAnswers("123")
    .set(ManufacturedPlasticPackagingPage, true).get
    .set(ManufacturedPlasticPackagingWeightPage, 100L).get
    .set(ImportedPlasticPackagingPage, true).get
    .set(ImportedPlasticPackagingWeightPage, 200L).get
    .set(DirectlyExportedComponentsPage, true).get
    .set(ExportedPlasticPackagingWeightPage, 100L).get
    .set(PlasticExportedByAnotherBusinessPage, true).get
    .set(AnotherBusinessExportWeightPage, 100L).get

  "nonExportedAmount" should {
    "return total plastic" when { "" +
      "plastic is not exported" in {
      val ans = userAnswer.set(DirectlyExportedComponentsPage, false).get
        .set(PlasticExportedByAnotherBusinessPage, false).get

      NonExportedAmountHelper.nonExportedAmount(ans) mustBe Some(300L)
    }

      "plastic is exported" in {
        val ans = userAnswer.set(DirectlyExportedComponentsPage, true).get
          .set(PlasticExportedByAnotherBusinessPage, true).get
          .set(ExportedPlasticPackagingWeightPage, 100L).get
          .set(AnotherBusinessExportWeightPage, 100L).get

        NonExportedAmountHelper.nonExportedAmount(ans) mustBe Some(100L)
      }
    }

    "return an error" when {

      "ImportedPlasticPackagingPage is missing" in {
        val ans = userAnswer.remove(ImportedPlasticPackagingPage).get

        NonExportedAmountHelper.nonExportedAmount(ans) mustBe None
      }

      "ImportedPlasticPackagingWeightPage is missing" in {
        val ans = userAnswer.remove(ImportedPlasticPackagingWeightPage).get

        NonExportedAmountHelper.nonExportedAmount(ans) mustBe None
      }

      "ManufacturedPlasticPackagingPage is missing" in {
        val ans = userAnswer.remove(ManufacturedPlasticPackagingPage).get

        NonExportedAmountHelper.nonExportedAmount(ans) mustBe None
      }

      "ManufacturedPlasticPackagingWeightPage is missing" in {
        val ans = userAnswer.remove(ManufacturedPlasticPackagingWeightPage).get

        NonExportedAmountHelper.nonExportedAmount(ans) mustBe None
      }

      "DirectlyExportedComponentsPage is missing" in {
        val ans = userAnswer.remove(DirectlyExportedComponentsPage).get

        NonExportedAmountHelper.nonExportedAmount(ans) mustBe None
      }

      "ExportedPlasticPackagingWeightPage is missing" in {
        val ans = userAnswer.remove(ExportedPlasticPackagingWeightPage).get

        NonExportedAmountHelper.nonExportedAmount(ans) mustBe None
      }
    }
  }

  "totalPlasticPackaging" should {
    "return total plastic" in {
      NonExportedAmountHelper.totalPlastic(userAnswer) mustBe Some(300)
    }
  }

  "getAmountAndDirectlyExportedAnswer" should {
    "return amount and Directly exported plus Exported by another business answer" in {
      NonExportedAmountHelper.getAmountAndDirectlyExportedAnswer(userAnswer) mustBe Some((100L, true))
    }

    "should return none" in {
      val ans = userAnswer.remove(DirectlyExportedComponentsPage).get

      NonExportedAmountHelper.getAmountAndDirectlyExportedAnswer(ans) mustBe None
    }
  }

}
