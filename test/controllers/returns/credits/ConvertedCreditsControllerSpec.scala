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

package controllers.returns.credits

import base.SpecBase
import connectors.CacheConnector
import controllers.returns.routes
import forms.returns.credits.ConvertedCreditsFormProvider
import models.Mode.NormalMode
import models.UserAnswers
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.returns.credits.ConvertedCreditsPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.returns.credits.ConvertedCreditsView

import scala.concurrent.Future

class ConvertedCreditsControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new ConvertedCreditsFormProvider()
  val form = formProvider()

  lazy val convertedCreditsRoute = controllers.returns.credits.routes.ConvertedCreditsController.onPageLoad(NormalMode).url

  "ConvertedCredits Controller" - {
//
//    "must return OK and the correct view for a GET" in {
//
//      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
//
//      running(application) {
//        val request = FakeRequest(GET, convertedCreditsRoute)
//
//        val result = route(application, request).value
//
//        val view = application.injector.instanceOf[ConvertedCreditsView]
//
//        status(result) mustEqual OK
//        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
//      }
//    }
//
//    "must populate the view correctly on a GET when the question has previously been answered" in {
//
//     // val userAnswers = UserAnswers(userAnswersId).set(ConvertedCreditsPage, true).success.value
//
//      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
//
//      running(application) {
//        val request = FakeRequest(GET, convertedCreditsRoute)
//
//        val view = application.injector.instanceOf[ConvertedCreditsView]
//
//        val result = route(application, request).value
//
//        status(result) mustEqual OK
//        //contentAsString(result) mustEqual view(form.fill(true), NormalMode)(request, messages(application)).toString
//      }
//    }
//
//    "must redirect to the next page when valid data is submitted" in {
//
//      val mockCacheConnector = mock[CacheConnector]
//
//      when(mockCacheConnector.set(any(), any())(any())) thenReturn Future.successful(mockResponse)
//
//      val application =
//        applicationBuilder(userAnswers = Some(emptyUserAnswers))
//          .overrides(
//            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
//            bind[CacheConnector].toInstance(mockCacheConnector)
//          )
//          .build()
//
//      running(application) {
//        val request =
//          FakeRequest(POST, convertedCreditsRoute)
//            .withFormUrlEncodedBody(("value", "true"))
//
//        val result = route(application, request).value
//
//        status(result) mustEqual SEE_OTHER
//        redirectLocation(result).value mustEqual controllers.returns.routes.ConfirmPackagingCreditController.onPageLoad.url
//      }
//    }
//
//    "must return a Bad Request and errors when invalid data is submitted" in {
//
//      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
//
//      running(application) {
//        val request =
//          FakeRequest(POST, convertedCreditsRoute)
//            .withFormUrlEncodedBody(("value", ""))
//
//        val boundForm = form.bind(Map("value" -> ""))
//
//        val view = application.injector.instanceOf[ConvertedCreditsView]
//
//        val result = route(application, request).value
//
//        status(result) mustEqual BAD_REQUEST
//        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
//      }
//    }
//
//    "must redirect to Journey Recovery for a GET if no existing data is found" in {
//
//      val application = applicationBuilder(userAnswers = None).build()
//
//      running(application) {
//        val request = FakeRequest(GET, convertedCreditsRoute)
//
//        val result = route(application, request).value
//
//        status(result) mustEqual SEE_OTHER
//        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad.url
//      }
//    }
//
//    "must redirect to Journey Recovery for a POST if no existing data is found" in {
//
//      val application = applicationBuilder(userAnswers = None).build()
//
//      running(application) {
//        val request =
//          FakeRequest(POST, convertedCreditsRoute)
//            .withFormUrlEncodedBody(("value", "true"))
//
//        val result = route(application, request).value
//
//        status(result) mustEqual SEE_OTHER
//        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad.url
//      }
//    }
  }
}
