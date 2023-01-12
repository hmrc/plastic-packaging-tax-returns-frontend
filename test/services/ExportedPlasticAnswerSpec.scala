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

package services

import controllers.helpers.InjectableNonExportedAmountHelper
import models.UserAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.mock
import org.scalatestplus.play.PlaySpec
import pages.returns._

class ExportedPlasticAnswerSpec extends PlaySpec {


  private val answer = createUserAnswer

  private val nonExportedAmountHelper = mock[InjectableNonExportedAmountHelper]

  "resetExportedByYouIfAllExportedPlastic" should {
    "reset userAnswer when exported amount is greater that total plastic" in {

      val expectedUserAnswer = answer
        .set(DirectlyExportedComponentsPage, true, cleanup = false).get
        .set(PlasticExportedByAnotherBusinessPage, false).get
        .set(AnotherBusinessExportWeightPage, 0L, cleanup = false).get
        .set(NonExportedHumanMedicinesPlasticPackagingPage, false, cleanup = false).get
        .set(NonExportedHumanMedicinesPlasticPackagingWeightPage, 0L, cleanup = false).get
        .set(NonExportedRecycledPlasticPackagingPage, false, cleanup = false).get
        .set(NonExportedRecycledPlasticPackagingWeightPage, 0L, cleanup = false)

      ExportedPlasticAnswer(answer).resetExportedByYouIfAllExportedPlastic mustEqual expectedUserAnswer

    }

    "return user answer when exported plastic is less then total plastic" in {
      val ans = answer
        .set(ManufacturedPlasticPackagingWeightPage, 300L).get
        .remove(DirectlyExportedComponentsPage).get

      ExportedPlasticAnswer(ans).resetExportedByYouIfAllExportedPlastic mustEqual
        ans.set(DirectlyExportedComponentsPage, true)
    }

  }

  "resetAnotherBusinessIfAllExportedPlastic" should {
    "reset userAnswer when exported amount is greater that total plastic" in {

      val expectedUserAnswer = answer
        .set(DirectlyExportedComponentsPage, true, cleanup = false).get
        .set(NonExportedHumanMedicinesPlasticPackagingPage, false, cleanup = false).get
        .set(NonExportedHumanMedicinesPlasticPackagingWeightPage, 0L, cleanup = false).get
        .set(NonExportedRecycledPlasticPackagingPage, false, cleanup = false).get
        .set(NonExportedRecycledPlasticPackagingWeightPage, 0L, cleanup = false)

      ExportedPlasticAnswer(answer).resetAnotherBusinessIfAllExportedPlastic mustEqual expectedUserAnswer
    }

    "return user answer when exported plastic is less then total plastic" in {
      val ans = answer
        .set(ManufacturedPlasticPackagingWeightPage, 300L).get
        .remove(AnotherBusinessExportWeightPage).get
        .remove(PlasticExportedByAnotherBusinessPage).get

      ExportedPlasticAnswer(ans).resetAnotherBusinessIfAllExportedPlastic mustEqual
        ans.set(PlasticExportedByAnotherBusinessPage, true)
    }
  }

  "isAllPlasticExported" should {
    "return true when exported amount is greater than total plastic" in {
      val ans = answer.set(AnotherBusinessExportWeightPage, 300L).get

      ExportedPlasticAnswer(ans).isAllPlasticExported mustEqual true
    }

    "return false when exported amount is less than total plastic" in {

      val ans = answer
        .set(ManufacturedPlasticPackagingWeightPage, 1000L).get

      ExportedPlasticAnswer(ans).isAllPlasticExported mustEqual false
    }

    "return true when exported amount is equal than total plastic" in {

      val ans = answer
        .set(ExportedPlasticPackagingWeightPage, 0L).get
        .set(AnotherBusinessExportWeightPage, 300L).get

      ExportedPlasticAnswer(ans).isAllPlasticExported mustEqual true
    }
  }

  "resetAllIfNoTotalPlastic" should {
    val ans = answer
      .set(ManufacturedPlasticPackagingWeightPage, -1L).get
      .set(ImportedPlasticPackagingWeightPage, -1L).get
    "reset all if total plastic is less than 0" in {
      when(nonExportedAmountHelper.totalPlastic(any())).thenReturn(Some(-1L))

      ExportedPlasticAnswer(ans).resetAllIfNoTotalPlastic(nonExportedAmountHelper) mustBe expectedUserAnswer(ans)
    }

    "reset all if total plastic is equal to 0" in {
      when(nonExportedAmountHelper.totalPlastic(any())).thenReturn(Some(0L))

      ExportedPlasticAnswer(ans).resetAllIfNoTotalPlastic(nonExportedAmountHelper) mustBe expectedUserAnswer(ans)
    }

    "reset all if total plastic is not defined" in {
      when(nonExportedAmountHelper.totalPlastic(any())).thenReturn(None)
      ExportedPlasticAnswer(ans).resetAllIfNoTotalPlastic(nonExportedAmountHelper) mustBe expectedUserAnswer(ans)
    }

    "do not change user answers if total plastic is greater than 0" in {
      when(nonExportedAmountHelper.totalPlastic(any())).thenReturn(Some(1L))
      ExportedPlasticAnswer(ans).resetAllIfNoTotalPlastic(nonExportedAmountHelper) mustBe ans
    }
  }

  private def expectedUserAnswer(ans: UserAnswers) = {
    ans
      .set(DirectlyExportedComponentsPage, false).get
      .set(ExportedPlasticPackagingWeightPage, 0L, cleanup = false).get
      .set(PlasticExportedByAnotherBusinessPage, false).get
      .set(AnotherBusinessExportWeightPage, 0L, cleanup = false).get
      .set(NonExportedHumanMedicinesPlasticPackagingPage, false, cleanup = false).get
      .set(NonExportedHumanMedicinesPlasticPackagingWeightPage, 0L, cleanup = false).get
      .set(NonExportedRecycledPlasticPackagingPage, false, cleanup = false).get
      .set(NonExportedRecycledPlasticPackagingWeightPage, 0L, cleanup = false).get
  }

  private def createUserAnswer: UserAnswers = {
    UserAnswers("123")
      .set(ManufacturedPlasticPackagingPage, true).get
      .set(ManufacturedPlasticPackagingWeightPage, 10L).get
      .set(ImportedPlasticPackagingPage, true).get
      .set(ImportedPlasticPackagingWeightPage, 1L).get
      .set(ExportedPlasticPackagingWeightPage, 5L).get
      .set(AnotherBusinessExportWeightPage, 200L).get
  }
}
