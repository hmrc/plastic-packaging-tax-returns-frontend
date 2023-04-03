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

package controllers.returns.credits

import base.utils.JourneyActionAnswer.{byConvertingFunctionArgumentsToAction, byConvertingFunctionArgumentsToFutureAction}
import controllers.actions.JourneyAction
import forms.returns.credits.ConvertedCreditsWeightFormProvider
import models.Mode.NormalMode
import models.requests.DataRequest
import navigation.ReturnsJourneyNavigator
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar
import org.mockito.integrations.scalatest.ResetMocksAfterEachTest
import org.mockito.stubbing.ReturnsDeepStubs
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import play.api.data.Form
import play.api.http.Status
import play.api.i18n.MessagesApi
import play.api.libs.json.JsPath
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.Helpers.{await, defaultAwaitTimeout, status}
import play.twirl.api.HtmlFormat
import views.html.returns.credits.ConvertedCreditsWeightView

import scala.concurrent.ExecutionContext.Implicits.global

class ConvertedCreditsWeightControllerSpec extends PlaySpec 
  with MockitoSugar with BeforeAndAfterEach with ResetMocksAfterEachTest {

  private val messagesApi = mock[MessagesApi]
  private val journeyAction = mock[JourneyAction]
  private val controllerComponents = mock[MessagesControllerComponents]
  private val view = mock[ConvertedCreditsWeightView]
  private val formProvider = mock[ConvertedCreditsWeightFormProvider]
  private val navigator = mock[ReturnsJourneyNavigator]

  private val controller = new ConvertedCreditsWeightController(
    messagesApi, journeyAction, controllerComponents, view, formProvider, navigator
  )
  
  private val request = mock[DataRequest[AnyContent]](ReturnsDeepStubs)
  private val form = mock[Form[Long]]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    
    when(journeyAction.apply(any)) thenAnswer byConvertingFunctionArgumentsToAction
    when(journeyAction.async(any)) thenAnswer byConvertingFunctionArgumentsToFutureAction
    
    when(formProvider.apply()) thenReturn form
    when(view.apply(any, any) (any, any)) thenReturn HtmlFormat.raw("a-view")
  }

  "onPageLoad" should {

    "use the journey action" in {
      controller.onPageLoad(NormalMode)
      verify(journeyAction).apply(any)
    }
    
    "use an existing user-answer if present" in {
      status(controller.onPageLoad(NormalMode)(request)) mustBe Status.OK
      verify(request.userAnswers).fill(JsPath \ "convertedCredits" \ "weight", form)
    }

  }

}
