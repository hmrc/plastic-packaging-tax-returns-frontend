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

package controllers.returns

import base.SpecBase
import cacheables.ReturnObligationCacheable
import connectors.CacheConnector
import forms.returns.ManufacturedPlasticPackagingFormProvider
import models.Mode.{CheckMode, NormalMode}
import models.UserAnswers
import models.returns.TaxReturnObligation
import navigation.ReturnsJourneyNavigator
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.{eq => eqq}
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.{reset, verify}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.returns.ManufacturedPlasticPackagingPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.returns.ManufacturedPlasticPackagingView

import java.time.LocalDate
import scala.concurrent.Future

class ManufacturedPlasticPackagingControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  private def onwardRoute = Call("GET", "/foo")
  private val formProvider = new ManufacturedPlasticPackagingFormProvider()
  private lazy val manufacturedPlasticPackagingRoute = routes.ManufacturedPlasticPackagingController.onPageLoad(NormalMode).url
  private val mockUserAnswers = mock[UserAnswers]
  private val mockCacheConnector = mock[CacheConnector]
  private val returnsJourneyNavigator = mock[ReturnsJourneyNavigator]
  private val saveAnswersFunc = mock[UserAnswers.SaveUserAnswerFunc]

  private def any[T]: T = ArgumentMatchers.any[T]()
  
  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockUserAnswers, mockCacheConnector, returnsJourneyNavigator)

    val date = LocalDate.ofEpochDay(0)
    val obligation = TaxReturnObligation(date, date, date, "")
    when(mockUserAnswers.get(eqq(ReturnObligationCacheable))(any)).thenReturn(Some(obligation))

    when(mockUserAnswers.change (any, any, any) (any)) thenReturn Future.successful(false)
    when(mockCacheConnector.saveUserAnswerFunc(any) (any)) thenReturn saveAnswersFunc
    when(returnsJourneyNavigator.manufacturedPlasticPackaging(any, any, any)).thenReturn(onwardRoute)
  }

  "ManufacturedPlasticPackaging Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .configure("bootstrap.filters.csrf.enabled" -> false)
        .build()

      running(application) {
        val request = FakeRequest(GET, manufacturedPlasticPackagingRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ManufacturedPlasticPackagingView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode, taxReturnOb)(request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val ans =
        userAnswers.set(ManufacturedPlasticPackagingPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(ans))
        .configure("bootstrap.filters.csrf.enabled" -> false)
        .build()

      running(application) {
        val request = FakeRequest(GET, manufacturedPlasticPackagingRoute)

        val view = application.injector.instanceOf[ManufacturedPlasticPackagingView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill(true), NormalMode, taxReturnOb)(request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(mockUserAnswers))
          .overrides(
            bind[CacheConnector].toInstance(mockCacheConnector), 
            bind[ReturnsJourneyNavigator].toInstance(returnsJourneyNavigator)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, manufacturedPlasticPackagingRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
        
        verify(mockCacheConnector).saveUserAnswerFunc(eqq("123"))(any)
        verify(mockUserAnswers).change (eqq(ManufacturedPlasticPackagingPage), eqq(true), any) (any)
        verify(returnsJourneyNavigator).manufacturedPlasticPackaging (NormalMode, false, true)
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, manufacturedPlasticPackagingRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = formProvider().bind(Map("value" -> ""))

        val view = application.injector.instanceOf[ManufacturedPlasticPackagingView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, taxReturnOb)(request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, manufacturedPlasticPackagingRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad.url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, manufacturedPlasticPackagingRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad.url
      }
    }
    
    "submit must redirect to the mini-cya page if the answer has not changed" in {
      when(mockUserAnswers.change (any, any, any) (any)) thenReturn Future.successful(false)

      val application = applicationBuilder(Some(mockUserAnswers))
        .overrides(
          bind[CacheConnector].toInstance(mockCacheConnector),
          bind[ReturnsJourneyNavigator].toInstance(returnsJourneyNavigator)
        )
        .build()

      running(application) {
        val request = FakeRequest(POST, routes.ManufacturedPlasticPackagingController.onPageLoad(CheckMode).url)
          .withFormUrlEncodedBody(("value", "true"))
        val result = route(application, request).value
        
        status(result) mustBe 303
        redirectLocation(result) mustBe Some("/foo")
      }

      verify(mockCacheConnector).saveUserAnswerFunc(eqq("123")) (any)
      verify(mockUserAnswers).change(eqq(ManufacturedPlasticPackagingPage), eqq(true), eqq(saveAnswersFunc)) (any)
      verify(returnsJourneyNavigator).manufacturedPlasticPackaging(CheckMode, false, true)
    }
    
  }
}
