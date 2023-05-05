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
import org.mockito.ArgumentMatchersSugar._
import org.mockito.MockitoSugar
import org.mockito.integrations.scalatest.ResetMocksAfterEachTest
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import pages.QuestionPage
import play.api.data.Form
import play.api.libs.json.Json.obj
import play.api.libs.json.{JsObject, JsPath, JsString, JsValue, Json, OWrites}
import play.api.test.Helpers.{await, defaultAwaitTimeout}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try
import java.time.{Instant, LocalDate}

class UserAnswersSpec extends PlaySpec 
  with BeforeAndAfterEach with MockitoSugar with ResetMocksAfterEachTest {

  private val emptyUserAnswers = UserAnswers("empty")
  private val filledUserAnswers = UserAnswers("filled", obj("cheese" -> obj("brie" -> "200g")))

  private class RandoException extends Exception {}
  
  private case class BadValue()
  private object BadValue {
    implicit val writes: OWrites[BadValue] = throw new RandoException
  }
  
  private val questionPage = mock[QuestionPage[String]]
  private val saveFunction = mock[SaveUserAnswerFunc]
  private val newValueFunc = mock[Option[String] => String]
  private val fillFormFunc = mock[String => Option[String]]
  private val emptyForm = mock[Form[String]]("empty form")
  private val filledForm = mock[Form[String]]("filled form")

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    
    when(questionPage.path) thenReturn JsPath \ "cheese" \ "brie"
    when(questionPage.cleanup(any, any)) thenAnswer {
      (_: Option[String], userAnswers: UserAnswers) => Try(userAnswers) // pass through
    }
    
    when(saveFunction.apply(any, any)) thenReturn Future.successful(true)
    when(newValueFunc.apply(any)) thenReturn "new-value"
    
    when(emptyForm.fill(any)) thenReturn filledForm
  }

  "it" should {
    "have an id" in {
      emptyUserAnswers must have('id ("empty"))
      filledUserAnswers must have('id ("filled"))
    }
    
    // TODO the timestamp... is it needed? (Actual timestamp for mongo in set by session repo in backend)
  }

  "setOrFail" should {
    "set a value" when {
      "using a path" in {
        val updatedAnswers = emptyUserAnswers.setOrFail(JsPath \ "cheese", "please")
        updatedAnswers.data.value mustBe Map("cheese" -> JsString("please"))
      }
      "using a question" in {
        val updatedAnswers = emptyUserAnswers.setOrFail(questionPage, "much")
        updatedAnswers.data.value mustBe Map("cheese" -> JsObject(Seq("brie" -> JsString("much"))))
      }
    }

    "pass on exceptions if something else goes wrong" in {
      a[RandoException] must be thrownBy emptyUserAnswers.setOrFail("x", BadValue())
    }

  }

  "getOrFail" should {

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

    "complain when an answer is missing" when {
      "using a question" in {
        when(questionPage.path) thenReturn JsPath \ "doesnt" \ "exist"
        the [Exception] thrownBy emptyUserAnswers.getOrFail(questionPage) must have message
          "/doesnt/exist is missing from user answers"
      }
      "using a path" in {
        the [Exception] thrownBy emptyUserAnswers.getOrFail[JsValue](JsPath \ "not-there") must have message
          "/not-there is missing from user answers"
      }
    }


  }

  "the rest" should {
    "fill in a form's value" when {
      "the answer exists" in {
        filledUserAnswers.fill(questionPage, emptyForm) mustBe theSameInstanceAs(filledForm)
        verify(emptyForm).fill("200g")
      }
      "the answer does not exist" in {
        emptyUserAnswers.fill(questionPage, emptyForm) mustBe theSameInstanceAs(emptyForm)
        verify(emptyForm, never).fill(any)
      }
    }
    
    "fill a yes-no form using given function" when {
      "user answer does not exist" in {
        emptyUserAnswers.genericFill(questionPage, emptyForm, fillFormFunc) mustBe theSameInstanceAs(emptyForm)
        verify(fillFormFunc, never).apply(any)
        verify(emptyForm, never).fill(any)
      }
      "user answer does exist" in {
        when(fillFormFunc.apply(any)) thenReturn Some("new-value")
        filledUserAnswers.genericFill(questionPage, emptyForm, fillFormFunc) mustBe theSameInstanceAs(filledForm)
        verify(fillFormFunc).apply(any)
        verify(emptyForm).fill(any)
      }
      "user answer does exist by function returns None" in {
        when(fillFormFunc.apply(any)) thenReturn None
        filledUserAnswers.genericFill(questionPage, emptyForm, fillFormFunc) mustBe theSameInstanceAs(emptyForm)
        verify(fillFormFunc).apply(any)
        verify(emptyForm, never).fill(any)
      }
    }

    "change a value" when {
      "new value is different" in {
        await(filledUserAnswers.change(questionPage, "no", saveFunction)) mustBe true
        val updatedJs = JsObject(Seq("cheese" -> JsObject(Seq("brie" -> JsString("no")))))
        verify(saveFunction).apply(UserAnswers("filled", updatedJs, filledUserAnswers.lastUpdated), true)
      }
      "new value is the same" in {
        await(filledUserAnswers.change(questionPage, "200g", saveFunction)) mustBe false
        verify(saveFunction, never).apply(any, any)
      }
    }
    
    "change a value with a function" when {
      
      "previous value exists" in {
        await {
          filledUserAnswers.changeWithFunc(questionPage, newValueFunc, saveFunction)
        }
        verify(newValueFunc).apply(Some("200g"))
        verify(saveFunction).apply(
          eqTo(UserAnswers("filled", obj { "cheese" -> obj("brie" -> "new-value") }, filledUserAnswers.lastUpdated)), 
          any)
      }
      
      "previous value does not exist" in {
        await {
          emptyUserAnswers.changeWithFunc(questionPage, newValueFunc, saveFunction)
        }
        verify(newValueFunc).apply(None)
        verify(saveFunction).apply(
          eqTo(UserAnswers("empty", obj { "cheese" -> obj("brie" -> "new-value") }, emptyUserAnswers.lastUpdated)),
          any)
      }
    }
    
    "save changed answers" in {
      await(filledUserAnswers.save(saveFunction)) must be theSameInstanceAs(filledUserAnswers)
      verify(saveFunction).apply(any, any)
    }
    
    "remove any answers" in {
      val resetUserAnswers = filledUserAnswers.reset
      resetUserAnswers.id mustBe "filled"
      resetUserAnswers.data mustBe Json.obj()
    }
  }
}
