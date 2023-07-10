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
import org.mockito.MockitoSugar.{mock, reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import pages.returns.{AnotherBusinessExportedPage, AnotherBusinessExportedWeightPage, DirectlyExportedWeightPage}
import play.api.i18n.Messages
import viewmodels.{RowInfo, TaxReturnViewModel}

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

  "exportedWeight" should {
    "return rowInfo" in {
      when(userAnswers.get(DirectlyExportedWeightPage)) thenReturn Some(200L)
      val result = sut.exportedWeight("part-of-a-key")
      result mustEqual RowInfo("part-of-a-key", "200kg")
    }

    "throw if page not found" in {
      when(userAnswers.get(DirectlyExportedWeightPage)) thenReturn None
      intercept[IllegalStateException] {
        sut.exportedWeight("part-of-a-key")
      }
    }
  }

  "exportedByAnotherBusinessYesNo" when {

    "answer is 'yes'" in {
      when(userAnswers.get(AnotherBusinessExportedPage)) thenReturn Some(true)
      val result = sut.exportedByAnotherBusinessYesNo("part-of-a-key")

      result mustBe RowInfo("part-of-a-key", "yes-string")
      verify(messages).apply("site.yes")
      verify(messages).apply("part-of-a-key")
    }

    "answer is 'no'" in {
      when(userAnswers.get(AnotherBusinessExportedPage)) thenReturn Some(false)
      val result = sut.exportedByAnotherBusinessYesNo("part-of-a-key")

      result mustBe RowInfo("part-of-a-key", "no-string")
      verify(messages).apply("site.no")
      verify(messages).apply("part-of-a-key")
    }

    "question is unanswered" in {
      when(userAnswers.get(AnotherBusinessExportedPage)) thenReturn None
      val result = sut.exportedByAnotherBusinessYesNo("part-of-a-key")

      result mustBe RowInfo("part-of-a-key", "no-string")
      verify(messages).apply("site.no")
      verify(messages).apply("part-of-a-key")
    }
  }
  
  "anotherBusinessExportedWeight" when {
    "question is answered" in {
      when(userAnswers.get(AnotherBusinessExportedWeightPage)) thenReturn Some(200L)
      val result = sut.anotherBusinessExportedWeight("part-of-a-key")
      result mustEqual RowInfo("part-of-a-key", "200kg")
    }

    "question is unanswered" in {
      when(userAnswers.get(AnotherBusinessExportedWeightPage)) thenReturn None
      val result = sut.anotherBusinessExportedWeight("part-of-a-key")
      result mustEqual RowInfo("part-of-a-key", "0kg")
    }
  }

  "canEditExported" should {

    "return true when exported plastic amount is greater tan zero" in {
      when(userAnswers.get(DirectlyExportedWeightPage)) thenReturn Some(0L)
      when(userAnswers.get(AnotherBusinessExportedWeightPage)) thenReturn Some(50L)
      when(calculations.packagingTotal) thenReturn 0L
      sut.canEditExported mustBe true
    }

    "return true when total plastic greater than exported plastic amount" in {
      when(userAnswers.get(DirectlyExportedWeightPage)) thenReturn Some(0L)
      when(userAnswers.get(AnotherBusinessExportedWeightPage)) thenReturn Some(0L)
      when(calculations.packagingTotal) thenReturn 10L
      sut.canEditExported mustBe true
    }

    "return false when total plastic and exported plastic are Zero" in {
      when(userAnswers.get(DirectlyExportedWeightPage)) thenReturn Some(0L)
      when(userAnswers.get(AnotherBusinessExportedWeightPage)) thenReturn Some(0L)
      when(calculations.packagingTotal) thenReturn 0L
      sut.canEditExported mustBe false
    }
    
    "assume zero when AnotherBusinessExportWeightPage is unanswered" in {
      when(userAnswers.get(DirectlyExportedWeightPage)) thenReturn Some(0L)
      when(userAnswers.get(AnotherBusinessExportedWeightPage)) thenReturn None
      when(calculations.packagingTotal) thenReturn 0L
      sut.canEditExported mustBe false
    }
  }

  "canEditNonExported" when {

    "exported-plastic and packaging-total are zero" in {
      when(userAnswers.get(DirectlyExportedWeightPage)) thenReturn Some(0L)
      when(userAnswers.get(AnotherBusinessExportedWeightPage)) thenReturn Some(0L)
      when(calculations.packagingTotal) thenReturn 0L
      sut.canEditNonExported mustBe false
    }

    "exported-plastic is equal to total-additions" in {
      when(userAnswers.get(DirectlyExportedWeightPage)) thenReturn Some(0L)
      when(userAnswers.get(AnotherBusinessExportedWeightPage)) thenReturn Some(50L)
      when(calculations.packagingTotal) thenReturn 50L
      sut.canEditNonExported mustBe false
    }

    "exported-plastic is less then to total-additions" in {
      when(userAnswers.get(DirectlyExportedWeightPage)) thenReturn Some(0L)
      when(userAnswers.get(AnotherBusinessExportedWeightPage)) thenReturn Some(49L)
      when(calculations.packagingTotal) thenReturn 50L
      sut.canEditNonExported mustBe true
    }

    "exported-plastic is more then to total-additions" in {
      when(userAnswers.get(DirectlyExportedWeightPage)) thenReturn Some(0L)
      when(userAnswers.get(AnotherBusinessExportedWeightPage)) thenReturn Some(51L)
      when(calculations.packagingTotal) thenReturn 50L
      sut.canEditNonExported mustBe false
    }

    "AnotherBusinessExportWeightPage is unanswered" in {
      when(userAnswers.get(DirectlyExportedWeightPage)) thenReturn Some(0L)
      when(userAnswers.get(AnotherBusinessExportedWeightPage)) thenReturn None
      when(calculations.packagingTotal) thenReturn 50L
      sut.canEditNonExported mustBe true
    }

  }

  "taxRateInPounds" should {
    "return the taxRate in pounds per tonne" in {
      when(calculations.taxRate).thenReturn(0.3)

      sut.taxRate mustBe "£300.00"
    }
  }
}
