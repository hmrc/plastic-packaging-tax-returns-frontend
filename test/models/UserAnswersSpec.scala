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

import models.UserAnswers.SaveUserAnswerFunc
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play.PlaySpec
import pages.QuestionPage
import play.api.data.Form
import play.api.libs.json.{JsObject, JsPath, JsString, Json, OWrites}
import play.api.test.Helpers.{await, defaultAwaitTimeout}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

class UserAnswersSpec extends PlaySpec with BeforeAndAfterEach {

  private val userAnswers = UserAnswers("henry")
  private val filledUserAnswers = UserAnswers("id", JsObject(Seq("cheese" -> JsObject(Seq("brie" -> JsString("200g"))))))

  private class TestException extends Exception {}
  
  private case class BadValue()
  private object BadValue {
    implicit val writes: OWrites[BadValue] = throw new TestException
  }
  
  private val questionPage = mock[QuestionPage[String]]
  private val saveFunction = mock[SaveUserAnswerFunc]
  
  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(questionPage, saveFunction)
    when(questionPage.path) thenReturn JsPath \ "cheese" \ "brie"
    when(questionPage.cleanup(any(), any())) thenAnswer {i => Try(i.getArgument[UserAnswers](1))}
    when(saveFunction.apply(any(), any())) thenReturn Future.successful(true)
  }

  
  "it" should {
    "remember its id" in {
      userAnswers must have ('id ("henry"))
    }

    "set a value" when {
      "using a string key" in {
        userAnswers.setOrFail("cheese", "please").data.value mustBe Map("cheese" -> JsString("please"))
      }
      "setting a value fails" in {
        a[TestException] must be thrownBy userAnswers.setOrFail("x", BadValue())
      }
      "using a question page key" in {
        userAnswers.setOrFail(questionPage, "much").data.value mustBe Map("cheese" -> JsObject(Seq("brie" -> JsString("much"))))
      }
    }

    "get a value" when {
      "using a string key" in {
        val filledUserAnswers = UserAnswers("id", JsObject(Seq("cheese" -> JsString("please"))))
        filledUserAnswers.getOrFail[String]("cheese") mustBe "please"
      }
      "using a path key" in {
        filledUserAnswers.getOrFail[String](JsPath \ "cheese" \ "brie") mustBe "200g"
      }
      "using a question page key" in {
        filledUserAnswers.getOrFail(questionPage) mustBe "200g"
      }
    }

    "fill in a form's value" in {
      val form = mock[Form[String]]
      when(form.fill(any())) thenReturn form
      filledUserAnswers.fill(questionPage, form) mustBe theSameInstanceAs(form)
      verify(form).fill("200g")
    }

    "change a value" when {
      "new value is different" in {
        await(filledUserAnswers.change(questionPage, "no", saveFunction)) mustBe true
        val updatedJs = JsObject(Seq("cheese" -> JsObject(Seq("brie" -> JsString("no")))))
        verify(saveFunction).apply(UserAnswers("id", updatedJs, filledUserAnswers.lastUpdated), true)
      }
      "new value is the same" in {
        await(filledUserAnswers.change(questionPage, "200g", saveFunction)) mustBe false
        verify(saveFunction, never()).apply(any(), any())
      }
    }
    
    "save changed answers" in {
      await(filledUserAnswers.save(saveFunction)) must be theSameInstanceAs(filledUserAnswers)
      verify(saveFunction).apply(any(), any())
    }
    
    "remove any answers" in {
      val resetUserAnswers = filledUserAnswers.reset
      resetUserAnswers.id mustBe "id"
      resetUserAnswers.data mustBe Json.obj()
    }
  }
}
