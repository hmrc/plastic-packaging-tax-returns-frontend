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
import org.mockito.ArgumentMatchersSugar.{any, eqTo}
import org.mockito.MockitoSugar.{mock, reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import pages.returns._

class NonExportedAmountHelperSpec extends PlaySpec with BeforeAndAfterEach {
  
  private val mockUserAnswers = mock[UserAnswers]

  private val userAnswer = UserAnswers("123")
    .set(ManufacturedPlasticPackagingPage, true).get
    .set(ManufacturedPlasticPackagingWeightPage, 100L).get
    .set(ImportedPlasticPackagingPage, true).get
    .set(ImportedPlasticPackagingWeightPage, 200L).get
    .set(DirectlyExportedWeightPage, 100L).get
    .set(AnotherBusinessExportedPage, true).get
    .set(AnotherBusinessExportedWeightPage, 100L).get

  override protected def beforeEach(): Unit = { 
    super.beforeEach()
    reset(mockUserAnswers)
  }

  private val sut = new NonExportedAmountHelper

  "returnsQuestionsAnswered" should {
    "return true" when {
      "all are defined" in {
        val ua =
          UserAnswers("id")
            .setOrFail(ManufacturedPlasticPackagingWeightPage, 0L)
            .setOrFail(ImportedPlasticPackagingWeightPage, 0L)
            .setOrFail(DirectlyExportedWeightPage, 0L)
            .setOrFail(AnotherBusinessExportedWeightPage, 0L)
            .setOrFail(NonExportedHumanMedicinesPlasticPackagingWeightPage, 0L)
            .setOrFail(NonExportedRecycledPlasticPackagingWeightPage, 0L)

        sut.returnsQuestionsAnswered(ua) mustBe true
      }
    }
    "return false" when {
      "the questions are not answered" in {
        val ua = UserAnswers("id")

        sut.returnsQuestionsAnswered(ua) mustBe false
      }
    }
  }

  "nonExportedAmount" should {
    "return total plastic" when {
      "plastic is not exported" in {
      val ans = userAnswer
        .set(DirectlyExportedPage, false).get
        .set(AnotherBusinessExportedPage, false).get
      sut.nonExportedAmount(ans) mustBe Some(300L)
    }

      "plastic is exported" in {
        val ans = userAnswer
          .set(AnotherBusinessExportedPage, true).get
          .set(DirectlyExportedWeightPage, 100L).get
          .set(AnotherBusinessExportedWeightPage, 100L).get
        sut.nonExportedAmount(ans) mustBe Some(100L)
      }
    }

    "return an error or default value" when {

      "ImportedPlasticPackagingPage is missing" in {
        val ans = userAnswer.remove(ImportedPlasticPackagingPage).get

        sut.nonExportedAmount(ans) mustBe None
      }

      "ImportedPlasticPackagingWeightPage is missing" in {
        val ans = userAnswer.remove(ImportedPlasticPackagingWeightPage).get

        sut.nonExportedAmount(ans) mustBe None
      }

      "ManufacturedPlasticPackagingPage is missing" in {
        val ans = userAnswer.remove(ManufacturedPlasticPackagingPage).get

        sut.nonExportedAmount(ans) mustBe None
      }

      "ManufacturedPlasticPackagingWeightPage is missing" in {
        val ans = userAnswer.remove(ManufacturedPlasticPackagingWeightPage).get

        sut.nonExportedAmount(ans) mustBe None
      }

      "DirectlyExportedPage is missing" in {
        val ans = userAnswer.remove(DirectlyExportedPage).get
        sut.nonExportedAmount(ans) mustBe None
      }

      "DirectlyExportedWeightPage is missing" in {
        val ans = userAnswer.remove(DirectlyExportedWeightPage).get
        sut.nonExportedAmount(ans) mustBe None
      }

      "AnotherBusinessExportedPage is missing" in {
        val ans = userAnswer.remove(AnotherBusinessExportedPage).get
        sut.nonExportedAmount(ans) mustBe Some(200L)
      }

      "AnotherBusinessExportedWeightPage is missing" in {
        val ans = userAnswer.remove(AnotherBusinessExportedWeightPage).get
        sut.nonExportedAmount(ans) mustBe Some(200L)
      }
    }
  }

  "totalPlasticAdditions" when {
    
    "accretion questions answered yes and weights given" in {
      when(mockUserAnswers.get(ManufacturedPlasticPackagingPage)) thenReturn Some(true)
      when(mockUserAnswers.get(ManufacturedPlasticPackagingWeightPage)) thenReturn Some(1)
      when(mockUserAnswers.get(ImportedPlasticPackagingPage)) thenReturn Some(true) 
      when(mockUserAnswers.get(ImportedPlasticPackagingWeightPage)) thenReturn Some(10) 
      sut.totalPlasticAdditions(mockUserAnswers) mustBe Some(11)
    }
    
    "manufactured was 'yes' and weight given, imported 'no'" ignore {
      when(mockUserAnswers.get(ManufacturedPlasticPackagingPage)) thenReturn Some(true)
      when(mockUserAnswers.get(ManufacturedPlasticPackagingWeightPage)) thenReturn Some(1)
      when(mockUserAnswers.get(ImportedPlasticPackagingPage)) thenReturn Some(false) 
      when(mockUserAnswers.get(ImportedPlasticPackagingWeightPage)) thenReturn None // TODO make this work
      sut.totalPlasticAdditions(mockUserAnswers) mustBe Some(1)
    }
      
    "accretion questions answered no (therefore no weight questions answered)" ignore {
      when(mockUserAnswers.get(ManufacturedPlasticPackagingPage)) thenReturn Some(false)
      when(mockUserAnswers.get(ManufacturedPlasticPackagingWeightPage)) thenReturn None // TODO support None
      when(mockUserAnswers.get(ImportedPlasticPackagingPage)) thenReturn Some(false)
      when(mockUserAnswers.get(ImportedPlasticPackagingWeightPage)) thenReturn None
      sut.totalPlasticAdditions(mockUserAnswers) mustBe Some(0)
    }
      
    "accretion questions answered no (and weights there none the less)" ignore {
      when(mockUserAnswers.get(ManufacturedPlasticPackagingPage)) thenReturn Some(false)
      when(mockUserAnswers.get(ManufacturedPlasticPackagingWeightPage)) thenReturn Some(1)
      when(mockUserAnswers.get(ImportedPlasticPackagingPage)) thenReturn Some(false)
      when(mockUserAnswers.get(ImportedPlasticPackagingWeightPage)) thenReturn Some(10)
      sut.totalPlasticAdditions(mockUserAnswers) mustBe Some(0)
    }
    
    "accretion answers missing" ignore {
      when(mockUserAnswers.get(ManufacturedPlasticPackagingPage)) thenReturn None // TODO support None
      when(mockUserAnswers.get(ManufacturedPlasticPackagingWeightPage)) thenReturn None
      when(mockUserAnswers.get(ImportedPlasticPackagingPage)) thenReturn None
      when(mockUserAnswers.get(ImportedPlasticPackagingWeightPage)) thenReturn None
      sut.totalPlasticAdditions(mockUserAnswers) mustBe Some(0)
    }
  }
  
  "nonExportedAmount" when {
    
    def whenManufactured100kg = {
      when(mockUserAnswers.get(ManufacturedPlasticPackagingPage)) thenReturn Some(true)
      when(mockUserAnswers.get(ManufacturedPlasticPackagingWeightPage)) thenReturn Some(100)
      when(mockUserAnswers.get(ImportedPlasticPackagingPage)) thenReturn Some(true)
      when(mockUserAnswers.get(ImportedPlasticPackagingWeightPage)) thenReturn Some(0)
    }

    "exported questions answered and weights given" in {
      whenManufactured100kg

      when(mockUserAnswers.get(DirectlyExportedPage)) thenReturn Some(true)
      when(mockUserAnswers.get(DirectlyExportedWeightPage)) thenReturn Some(10)

      when(mockUserAnswers.get(AnotherBusinessExportedPage)) thenReturn Some(true)
      when(mockUserAnswers.get(AnotherBusinessExportedWeightPage)) thenReturn Some(1)
      
      sut.nonExportedAmount(mockUserAnswers) mustBe Some(89)
    }

    "directly exported everything" in {
      whenManufactured100kg

      when(mockUserAnswers.get(DirectlyExportedPage)) thenReturn Some(true)
      when(mockUserAnswers.get(DirectlyExportedWeightPage)) thenReturn Some(100)

      when(mockUserAnswers.get(AnotherBusinessExportedPage)) thenReturn None
      when(mockUserAnswers.get(AnotherBusinessExportedWeightPage)) thenReturn None
      
      sut.nonExportedAmount(mockUserAnswers) mustBe Some(0)
    }

    "indirectly exported everything" in {
      whenManufactured100kg

      when(mockUserAnswers.get(DirectlyExportedPage)) thenReturn Some(false)
      when(mockUserAnswers.get(DirectlyExportedWeightPage)) thenReturn Some(0L) // TODO make this work for None

      when(mockUserAnswers.get(AnotherBusinessExportedPage)) thenReturn Some(true)
      when(mockUserAnswers.get(AnotherBusinessExportedWeightPage)) thenReturn Some(100)
      
      sut.nonExportedAmount(mockUserAnswers) mustBe Some(0)
    }

    "no exports" in {
      whenManufactured100kg

      when(mockUserAnswers.get(DirectlyExportedPage)) thenReturn Some(false)
      when(mockUserAnswers.get(DirectlyExportedWeightPage)) thenReturn Some(0L) // TODO make this work for None

      when(mockUserAnswers.get(AnotherBusinessExportedPage)) thenReturn Some(false)
      when(mockUserAnswers.get(AnotherBusinessExportedWeightPage)) thenReturn None
      
      sut.nonExportedAmount(mockUserAnswers) mustBe Some(100)
    }

    "export questions have not been answered" ignore {
      whenManufactured100kg

      when(mockUserAnswers.get(DirectlyExportedPage)) thenReturn None // TODO make this work for None
      when(mockUserAnswers.get(DirectlyExportedWeightPage)) thenReturn None

      when(mockUserAnswers.get(AnotherBusinessExportedPage)) thenReturn None
      when(mockUserAnswers.get(AnotherBusinessExportedWeightPage)) thenReturn None
      
      sut.nonExportedAmount(mockUserAnswers) mustBe Some(100)
    }
  }

  "getAmountAndDirectlyExportedAnswer" when {
    "return amount and Directly exported plus Exported by another business answer" in {
      sut.getAmountAndDirectlyExportedAnswer(userAnswer) mustBe Some((100L, true, true))
    }

    "AnotherBusinessExportedPage is missing" in {
      val ans = userAnswer.remove(AnotherBusinessExportedPage).get
      sut.getAmountAndDirectlyExportedAnswer(ans) mustBe Some(200, true, false)
    }

    "AnotherBusinessExportedWeightPage is missing" in {
      val ans = userAnswer.remove(AnotherBusinessExportedWeightPage).get
      sut.getAmountAndDirectlyExportedAnswer(ans) mustBe Some(200, true, true)
    }
    
    "manufactured yes-no answer is missing" in {
      val changedAnswers = userAnswer.remove(ManufacturedPlasticPackagingPage).get
      sut.getAmountAndDirectlyExportedAnswer(changedAnswers) mustBe None
    }
    
    "manufactured weight answer is missing" in {
      val changedAnswers = userAnswer.remove(ManufacturedPlasticPackagingWeightPage).get
      sut.getAmountAndDirectlyExportedAnswer(changedAnswers) mustBe None
    }
    
    "imported yes-no answer is missing" in {
      val changedAnswers = userAnswer.remove(ImportedPlasticPackagingPage).get
      sut.getAmountAndDirectlyExportedAnswer(changedAnswers) mustBe None
    }
    
    "imported weight answer is missing" in {
      val changedAnswers = userAnswer.remove(ImportedPlasticPackagingWeightPage).get
      sut.getAmountAndDirectlyExportedAnswer(changedAnswers) mustBe None
    }
    
    "directly exported yes-no answer is missing" in {
      val changedAnswers = userAnswer.remove(DirectlyExportedPage).get
      sut.getAmountAndDirectlyExportedAnswer(changedAnswers) mustBe None
    }
    
    "directly exported weight answer is missing" in {
      val changedAnswers = userAnswer.remove(DirectlyExportedWeightPage).get
      sut.getAmountAndDirectlyExportedAnswer(changedAnswers) mustBe None
    }
  }

}
