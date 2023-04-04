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
import connectors.CacheConnector
import controllers.BetterMockActionSyntax
import controllers.actions.JourneyAction
import forms.returns.credits.ConvertedCreditsWeightFormProvider
import models.Mode.NormalMode
import models.requests.DataRequest
import navigation.ReturnsJourneyNavigator
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar
import org.mockito.integrations.scalatest.ResetMocksAfterEachTest
import org.mockito.stubbing.{ReturnsDeepStubs, ScalaFirstStubbing}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import play.api.data.Forms.longNumber
import play.api.data.{Form, Mapping}
import play.api.http.Status
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.json.JsPath
import play.api.mvc.{AnyContent, Call, MessagesControllerComponents, RequestHeader}
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
  private val cacheConnector = mock[CacheConnector]

  private val controller = new ConvertedCreditsWeightController(
    messagesApi, journeyAction, controllerComponents, view, formProvider, navigator, cacheConnector
  )
  
  private val request = mock[DataRequest[AnyContent]](ReturnsDeepStubs)
  private val form = mock[Form[Long]]
  private val messages = mock[Messages]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    
    when(journeyAction.apply(any)) thenAnswer byConvertingFunctionArgumentsToAction
    when(journeyAction.async(any)) thenAnswer byConvertingFunctionArgumentsToFutureAction
    
    when(formProvider.apply()) thenReturn form
    
    when(view.apply(any, any) (any, any)) thenReturn HtmlFormat.raw("a-view")
    when(request.userAnswers.fill[Long](any[JsPath], any) (any)) thenReturn form
    
    when(navigator.convertedCreditsWeight(any, any)) thenReturn Call("whoyagonna", "ghostbusters")
  }

  "onPageLoad" should {

    "use the journey action" in {
      controller.onPageLoad(NormalMode)
      verify(journeyAction).apply(any)
    }
    
    "show web page with correct submit url" in {
      when(messagesApi.preferred(any[RequestHeader])) thenReturn messages
      status {
        controller.onPageLoad(NormalMode).skippingJourneyAction(request)
      } mustBe Status.OK
      verify(view).apply(form, routes.ConvertedCreditsWeightController.onSubmit(NormalMode)) (request, messages)
    }
    
    "use an existing user-answer if present" in {
      await {
        controller.onPageLoad(NormalMode).skippingJourneyAction(request)
      }
      verify(request.userAnswers).fill(JsPath \ "convertedCredits" \ "weight", form)
    }

    // TODO ...
  }
  
  "onSubmit" should {
    
    "save the user's answer" in {
      when(form.bindFromRequest()(any, any)) thenReturn Form("value" -> longNumber).fill(1L)
      await {
        controller.onSubmit(NormalMode).skippingJourneyAction(request)
      }
      verify(request.userAnswers).changeWithPath(any, any, any) (any)
    }
    
  }

}
