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

package controllers.helpers

import models.UserAnswers
import models.returns.{Calculations, TaxReturnObligation}
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar.{mock, reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import pages.returns.{AnotherBusinessExportWeightPage, ExportedPlasticPackagingWeightPage, PlasticExportedByAnotherBusinessPage}
import play.api.i18n.Messages

class TaxReturnViewModelSpec extends PlaySpec with BeforeAndAfterEach {

  private val calculations = mock[Calculations]
  private val messages = mock[Messages]
  private val userAnswers = mock[UserAnswers]

  private val sut = TaxReturnViewModel(
    userAnswers,
    "ppt-ref", 
    mock[TaxReturnObligation],
    calculations
    )(messages)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(calculations, messages, userAnswers)

    // expect these to match a message-file entry
    when(messages.apply("site.yes")).thenReturn("yes-string")
    when(messages.apply("site.no")).thenReturn("no-string")

    // expect this to not match a message-file entry
    when(messages.apply("part-of-a-key")).thenReturn("part-of-a-key") 
  }

  "exportedWeigh" should {
    "return rowInfo" in {
      when(userAnswers.get(ExportedPlasticPackagingWeightPage)) thenReturn Some(200L)
      val result = sut.exportedWeight("part-of-a-key")
      result mustEqual RowInfo("part-of-a-key", "200kg")
    }

    "throw if page not found" in {
      when(userAnswers.get(ExportedPlasticPackagingWeightPage)) thenReturn None
      intercept[IllegalStateException] {
        sut.exportedWeight("part-of-a-key")
      }
    }
  }

  "exportedByAnotherBusinessYesNo" when {

    "answer is 'yes'" in {
      when(userAnswers.get(PlasticExportedByAnotherBusinessPage)) thenReturn Some(true)
      val result = sut.exportedByAnotherBusinessYesNo("part-of-a-key")

      result mustBe RowInfo("part-of-a-key", "yes-string")
      verify(messages).apply("site.yes")
      verify(messages).apply("part-of-a-key")
    }

    "answer is 'no'" in {
      when(userAnswers.get(PlasticExportedByAnotherBusinessPage)) thenReturn Some(false)
      val result = sut.exportedByAnotherBusinessYesNo("part-of-a-key")

      result mustBe RowInfo("part-of-a-key", "no-string")
      verify(messages).apply("site.no")
      verify(messages).apply("part-of-a-key")
    }

    "question is unanswered" in {
      when(userAnswers.get(PlasticExportedByAnotherBusinessPage)) thenReturn None
      val result = sut.exportedByAnotherBusinessYesNo("part-of-a-key")

      result mustBe RowInfo("part-of-a-key", "no-string")
      verify(messages).apply("site.no")
      verify(messages).apply("part-of-a-key")
    }
  }
  
  "anotherBusinessExportedWeight" should {
    "return rowInfo" in {
      when(userAnswers.get(AnotherBusinessExportWeightPage)) thenReturn Some(200L)
      val result = sut.anotherBusinessExportedWeight("part-of-a-key")
      result mustEqual RowInfo("part-of-a-key", "200kg")
    }

    "throw if page not found" in {
      when(userAnswers.get(AnotherBusinessExportWeightPage)) thenReturn None
      intercept[IllegalStateException] {
        sut.anotherBusinessExportedWeight("part-of-a-key")
      }
    }
  }

  "canEditExported" should {

    "return true when exported plastic amount is greater tan zero" in {
      when(userAnswers.get(ExportedPlasticPackagingWeightPage)) thenReturn Some(0L)
      when(userAnswers.get(AnotherBusinessExportWeightPage)) thenReturn Some(50L)
      when(calculations.packagingTotal) thenReturn 0L
      sut.canEditExported mustBe true
    }

    "return true when total plastic greater than exported plastic amount" in {
      when(userAnswers.get(ExportedPlasticPackagingWeightPage)) thenReturn Some(0L)
      when(userAnswers.get(AnotherBusinessExportWeightPage)) thenReturn Some(0L)
      when(calculations.packagingTotal) thenReturn 10L
      sut.canEditExported mustBe true
    }

    "return false when total plastic and exported plastic are Zero" in {
      when(userAnswers.get(ExportedPlasticPackagingWeightPage)) thenReturn Some(0L)
      when(userAnswers.get(AnotherBusinessExportWeightPage)) thenReturn Some(0L)
      when(calculations.packagingTotal) thenReturn 0L
      sut.canEditExported mustBe false
    }
  }
}
