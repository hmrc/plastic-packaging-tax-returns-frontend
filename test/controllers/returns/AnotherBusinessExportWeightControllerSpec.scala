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

package controllers.returns

import base.SpecBase
import connectors.CacheConnector
import forms.AnotherBusinessExportWeightFormProvider
import forms.returns.ExportedPlasticPackagingWeightFormProvider
import models.Mode.NormalMode
import models.UserAnswers
import navigation.{FakeNavigator, Navigator, ReturnsJourneyNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.returns.{AnotherBusinessExportWeightPage, ExportedPlasticPackagingWeightPage, ImportedPlasticPackagingWeightPage, ManufacturedPlasticPackagingWeightPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.returns.{AnotherBusinessExportWeightView, ExportedPlasticPackagingWeightView}

import scala.concurrent.Future

class AnotherBusinessExportWeightControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new AnotherBusinessExportWeightFormProvider()
  private val returnsNavigator = mock[ReturnsJourneyNavigator]
  def onwardRoute = Call("GET", "/foo")

  val validAnswer = 0L
  val totalPlastic: Long = 12

  lazy val anotherBusinessExportedWeightRoute = controllers.returns.routes.AnotherBusinessExportWeightController.onPageLoad(NormalMode).url
  val answersWithPreset: UserAnswers = emptyUserAnswers
    .set(ManufacturedPlasticPackagingWeightPage, 7L).get
    .set(ImportedPlasticPackagingWeightPage, 5L).get

  //TODO: Fix tests

  "ExportedPlasticPackagingWeight Controller" - {
//
//    "must return OK and the correct view for a GET" in {
//
//      val application = applicationBuilder(userAnswers = Some(answersWithPreset)).build()
//
//      running(application) {
//        val request = FakeRequest(GET, anotherBusinessExportedWeightRoute)
//        val result = route(application, request).value
//
//        val view = application.injector.instanceOf[AnotherBusinessExportWeightView]
//
//        status(result) mustEqual OK
//        contentAsString(result) mustEqual view(totalPlastic, formProvider(), NormalMode)(request, messages(application)
//        ).toString
//      }
//    }
//
//    "must populate the view correctly on a GET when the question has previously been answered" in {
//
//      val userAnswers = answersWithPreset.set(AnotherBusinessExportWeightPage, validAnswer).success.value
//
//      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
//
//      running(application) {
//        val request = FakeRequest(GET, anotherBusinessExportedWeightRoute)
//
//        val view = application.injector.instanceOf[AnotherBusinessExportWeightView]
//
//        val result = route(application, request).value
//
//        status(result) mustEqual OK
//        contentAsString(result) mustEqual view(totalPlastic, formProvider().fill(validAnswer), NormalMode)(request, messages(application)).toString
//      }
//    }
//
////    "must redirect to the next page when valid data is submitted" in {
////
////      val mockCacheConnector = mock[CacheConnector]
////
////      when(mockCacheConnector.set(any(), any())(any())) thenReturn Future.successful(mockResponse)
////
////      val application =
////        applicationBuilder(userAnswers = Some(answersWithPreset))
////          .overrides(
////            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
////            bind[CacheConnector].toInstance(mockCacheConnector)
////          )
////          .build()
////
////      running(application) {
////        val request =
////          FakeRequest(POST, anotherBusinessExportedWeightRoute)
////            .withFormUrlEncodedBody(("value", validAnswer.toString))
////
////        val result = route(application, request).value
////
////        status(result) mustEqual SEE_OTHER
////        redirectLocation(result).value mustEqual onwardRoute.url
////      }
////    }
//
//    "must return a Bad Request and errors when invalid data is submitted" in {
//
//      val application = applicationBuilder(userAnswers = Some(answersWithPreset)).build()
//
//      running(application) {
//        val request =
//          FakeRequest(POST, anotherBusinessExportedWeightRoute)
//            .withFormUrlEncodedBody(("value", "invalid value"))
//
//        val boundForm = formProvider().bind(Map("value" -> "invalid value"))
//
//        val view = application.injector.instanceOf[AnotherBusinessExportWeightView]
//
//        val result = route(application, request).value
//
//        status(result) mustEqual BAD_REQUEST
//        contentAsString(result) mustEqual view(totalPlastic, boundForm, NormalMode)(request, messages(application)).toString
//      }
//    }
//
//    "must redirect to Journey Recovery for a GET if no existing data is found" in {
//
//      val application = applicationBuilder(userAnswers = None).build()
//
//      running(application) {
//        val request = FakeRequest(GET, anotherBusinessExportedWeightRoute)
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
//          FakeRequest(POST, anotherBusinessExportedWeightRoute)
//            .withFormUrlEncodedBody(("value", validAnswer.toString))
//
//        val result = route(application, request).value
//
//        status(result) mustEqual SEE_OTHER
//
//        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad.url
//      }
//    }
  }
}
