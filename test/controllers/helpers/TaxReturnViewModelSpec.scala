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
import models.requests.DataRequest
import models.returns.{Calculations, TaxReturnObligation}
import org.mockito.Answers
import org.mockito.ArgumentMatchers.anyString
import org.mockito.MockitoSugar.{mock, reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import pages.returns.{AnotherBusinessExportWeightPage, ExportedPlasticPackagingWeightPage, PlasticExportedByAnotherBusinessPage}
import play.api.i18n.Messages
import play.api.mvc.AnyContent

class TaxReturnViewModelSpec extends PlaySpec with BeforeAndAfterEach {

  private val dataRequest = mock[DataRequest[AnyContent]](Answers.RETURNS_DEEP_STUBS)
  private val messages = mock[Messages]

  private val sut = TaxReturnViewModel(
    dataRequest,
    mock[TaxReturnObligation],
    mock[Calculations]
    )(messages)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(dataRequest, messages)

    when(messages.apply(anyString())).thenReturn("key")
  }

  "exportedWeigh" should {
    "return rowInfo" in {
      when(dataRequest.userAnswers).thenAnswer(UserAnswers("123").set(ExportedPlasticPackagingWeightPage, 200L).get)

      val result = sut.exportedWeight("any-key")

      result mustEqual RowInfo("key", "200kg")
    }

    "throw if page not found" in {
      when(dataRequest.userAnswers).thenAnswer(UserAnswers("123"))

      intercept[IllegalStateException] {
        sut.exportedWeight("any-key")
      }
    }
  }

  "exportedByAnotherBusinessYesNo" should {
    "return rowInfo with yes as answer" in {
      when(dataRequest.userAnswers).thenAnswer(UserAnswers("123").set(PlasticExportedByAnotherBusinessPage, true).get)
      when(messages.apply("site.yes")).thenReturn("key2")
      when(messages.apply("any-key")).thenReturn("key1")

      val result = sut.exportedByAnotherBusinessYesNo("any-key")

      result mustBe RowInfo("key1", "key2")
      verify(messages).apply("site.yes")
      verify(messages).apply("any-key")
    }

    "return rowInfo with No as answer" in {
      when(dataRequest.userAnswers).thenAnswer(UserAnswers("123").set(PlasticExportedByAnotherBusinessPage, false).get)
      when(messages.apply("site.no")).thenReturn("No")
      when(messages.apply("any-key")).thenReturn("key1")

      val result = sut.exportedByAnotherBusinessYesNo("any-key")

      result mustBe RowInfo("key1", "No")
      verify(messages).apply("site.no")
      verify(messages).apply("any-key")
    }

    "throw if page not found" in {
      when(dataRequest.userAnswers).thenAnswer(UserAnswers("123"))

      intercept[IllegalStateException] {
        sut.exportedByAnotherBusinessYesNo("any-key")
      }
    }
  }
  "anotherBusinessExportedWeight" should {
    "return rowInfo" in {
      when(dataRequest.userAnswers).thenAnswer(UserAnswers("123").set(AnotherBusinessExportWeightPage, 200L).get)

      val result = sut.anotherBusinessExportedWeight("any-key")

      result mustEqual RowInfo("key", "200kg")
    }

    "throw if page not found" in {
      when(dataRequest.userAnswers).thenAnswer(UserAnswers("123"))

      intercept[IllegalStateException] {
        sut.anotherBusinessExportedWeight("any-key")
      }
    }
  }
}
