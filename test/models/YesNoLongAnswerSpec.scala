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

package models

import org.mockito.MockitoSugar.{mock, reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.play.PlaySpec
import pages.QuestionPage

class YesNoLongAnswerSpec extends PlaySpec with Matchers with BeforeAndAfterEach {

  private val pageOne = mock[QuestionPage[Boolean]]
  private val pageTwo = mock[QuestionPage[Long]]
  private val userAnswers = mock[UserAnswers]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(pageOne, pageTwo, userAnswers)
  }

  "YesNoLongAnswer" when {
    
    "user answers 'no'" in {
      val answer = YesNoLongAnswer().changeAnswerToNo
      answer.areQuestionsAnswered mustBe true
      answer.isUsersAnswerYes mustBe false
      answer.isUsersAnswerNo mustBe true
      answer.value mustBe 0L
    }

    "user answers 'yes', but has not answered value question yet" in {
      val answer = YesNoLongAnswer().changeAnswerToYes
      answer.areQuestionsAnswered mustBe false
      answer.isUsersAnswerYes mustBe true
      answer.isUsersAnswerNo mustBe false
      the [IllegalStateException] thrownBy answer.value must 
        have message "Question only partially answered (YesNoLongAnswer(Some(true),None))"
    }
    
    "user answers 'yes', and given a value" in {
      val answer = YesNoLongAnswer()
        .changeAnswerToYes
        .changeAnswerToValue(11L)
      
      answer.areQuestionsAnswered mustBe true
      answer.isUsersAnswerYes mustBe true
      answer.isUsersAnswerNo mustBe false
      answer.value mustBe 11L
    }
    
    "user answers with just a value (implying also a 'yes' answer)" in {
      val answer = YesNoLongAnswer()
        .changeAnswerToValue(1L)
      
      answer.areQuestionsAnswered mustBe true
      answer.isUsersAnswerYes mustBe true
      answer.isUsersAnswerNo mustBe false
      answer.value mustBe 1L
    }
    
    "user changes answer to 'no'" in {
      val answer = YesNoLongAnswer()
        .changeAnswerToValue(1L)
        .changeAnswerToNo
      
      answer.areQuestionsAnswered mustBe true
      answer.isUsersAnswerYes mustBe false
      answer.isUsersAnswerNo mustBe true
      answer.value mustBe 0L
    }
    
    "user changes answer to 'no' then back to 'yes'" in {
      val answer = YesNoLongAnswer()
        .changeAnswerToValue(1L)
        .changeAnswerToNo
        .changeAnswerToYes
      
      answer.areQuestionsAnswered mustBe true
      answer.isUsersAnswerYes mustBe true
      answer.isUsersAnswerNo mustBe false
      answer.value mustBe 1L
    }
    
  }

  "fromUserAnswers" when {
    
    "the questions have not been answered" in {
      when(userAnswers.get(pageOne)) thenReturn None
      when(userAnswers.get(pageTwo)) thenReturn None
      YesNoLongAnswer.fromUserAnswers(userAnswers, pageOne, pageTwo) mustBe YesNoLongAnswer(None, None)
    }
    
    "user said 'no'" in {
      when(userAnswers.get(pageOne)) thenReturn Some(false)
      when(userAnswers.get(pageTwo)) thenReturn None
      YesNoLongAnswer.fromUserAnswers(userAnswers, pageOne, pageTwo) mustBe YesNoLongAnswer(Some(false), None)
    }
    
    "user said 'yes', but hasn't given a value yet" in {
      when(userAnswers.get(pageOne)) thenReturn Some(true)
      when(userAnswers.get(pageTwo)) thenReturn None
      YesNoLongAnswer.fromUserAnswers(userAnswers, pageOne, pageTwo) mustBe YesNoLongAnswer(Some(true), None)
    }
    
    "user has answered both questions" in {
      when(userAnswers.get(pageOne)) thenReturn Some(true)
      when(userAnswers.get(pageTwo)) thenReturn Some(17L)
      YesNoLongAnswer.fromUserAnswers(userAnswers, pageOne, pageTwo) mustBe YesNoLongAnswer(Some(true), Some(17L))
    }
  }
  
  "stringify" must {
    "say yes / no bit" ignore {
      ???
    }
    "kg bit" ignore {
      ???      
    }
  }
  
  "toJson" ignore {
    ???
  }

}
