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

    when(messages.apply(any[String])).thenReturn("key") // todo ?
    when(messages.apply("site.yes")).thenReturn("yes-key")
    when(messages.apply("input-key")).thenReturn("output-key")
  }

  "exportedWeigh" should {
    "return rowInfo" in {
      when(userAnswers.get(ExportedPlasticPackagingWeightPage)) thenReturn Some(200L)
      val result = sut.exportedWeight("input-key")
      result mustEqual RowInfo("output-key", "200kg")
    }

    "throw if page not found" in {
      when(userAnswers.get(ExportedPlasticPackagingWeightPage)) thenReturn None
      intercept[IllegalStateException] {
        sut.exportedWeight("any-key")
      }
    }
  }

  "exportedByAnotherBusinessYesNo" when {
    "answer is 'yes'" in {
      when(userAnswers.get(PlasticExportedByAnotherBusinessPage)) thenReturn Some(true)
      when(messages.apply("site.yes")).thenReturn("key2")
      when(messages.apply("any-key")).thenReturn("key1")

      val result = sut.exportedByAnotherBusinessYesNo("any-key")

      result mustBe RowInfo("key1", "key2")
      verify(messages).apply("site.yes")
      verify(messages).apply("any-key")
    }

    "answer is 'no'" in {
      when(userAnswers.get(PlasticExportedByAnotherBusinessPage)) thenReturn Some(false)
      when(messages.apply("site.no")).thenReturn("No")
      when(messages.apply("any-key")).thenReturn("key1")

      val result = sut.exportedByAnotherBusinessYesNo("any-key")

      result mustBe RowInfo("key1", "No")
      verify(messages).apply("site.no")
      verify(messages).apply("any-key")
    }

    "question is unanswered" in {
      when(userAnswers.get(PlasticExportedByAnotherBusinessPage)) thenReturn None
      intercept[IllegalStateException] {
        sut.exportedByAnotherBusinessYesNo("any-key")
      }
    }
  }
  
  "anotherBusinessExportedWeight" should {
    "return rowInfo" in {
      when(userAnswers.get(AnotherBusinessExportWeightPage)) thenReturn Some(200L)
      val result = sut.anotherBusinessExportedWeight("input-key")
      result mustEqual RowInfo("output-key", "200kg")
    }

    "throw if page not found" in {
      when(userAnswers.get(AnotherBusinessExportWeightPage)) thenReturn None
      intercept[IllegalStateException] {
        sut.anotherBusinessExportedWeight("any-key")
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
