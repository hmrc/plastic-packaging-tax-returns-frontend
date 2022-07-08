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
import base.utils.NonExportedPlasticTestHelper
import connectors.CacheConnector
import controllers.{routes => appRoutes}
import forms.returns.NonExportedHumanMedicinesPlasticPackagingFormProvider
import models.NormalMode
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.returns.{NonExportedHumanMedicinesPlasticPackagingPage, NonExportedHumanMedicinesPlasticPackagingWeightPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.returns.NonExportedHumanMedicinesPlasticPackagingView

import scala.concurrent.Future

class NonExportedHumanMedicinesPlasticPackagingControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new NonExportedHumanMedicinesPlasticPackagingFormProvider()
  private def form = formProvider()

  lazy val nonExportedHumanMedicinesPlasticPackagingRoute = routes.NonExportedHumanMedicinesPlasticPackagingController.onPageLoad(NormalMode).url

  val answersWithPreset = emptyUserAnswers.set(NonExportedHumanMedicinesPlasticPackagingWeightPage, 0L).get

  val manufacturedAmount = 200L
  val importedAmount = 100L
  val exportedAmount = 50L
  val nonExportedAmount = (manufacturedAmount + importedAmount) - exportedAmount
  lazy val nonExportedAnswer = NonExportedPlasticTestHelper.createUserAnswer(exportedAmount, manufacturedAmount, importedAmount)

  "NonExportedHumanMedicinesPlasticPackaging Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(nonExportedAnswer)).build()

      running(application) {
        val request = FakeRequest(GET, nonExportedHumanMedicinesPlasticPackagingRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[NonExportedHumanMedicinesPlasticPackagingView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(nonExportedAmount, form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = nonExportedAnswer.set(NonExportedHumanMedicinesPlasticPackagingPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, nonExportedHumanMedicinesPlasticPackagingRoute)

        val view = application.injector.instanceOf[NonExportedHumanMedicinesPlasticPackagingView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(nonExportedAmount, form.fill(true), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect GET to home page when exported amount not found" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, nonExportedHumanMedicinesPlasticPackagingRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual appRoutes.IndexController.onPageLoad.url
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any(), any())(any())) thenReturn Future.successful(mockResponse)

      val application =
        applicationBuilder(userAnswers = Some(answersWithPreset))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[CacheConnector].toInstance(mockCacheConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, nonExportedHumanMedicinesPlasticPackagingRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(nonExportedAnswer)).build()

      running(application) {
        val request =
          FakeRequest(POST, nonExportedHumanMedicinesPlasticPackagingRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[NonExportedHumanMedicinesPlasticPackagingView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(nonExportedAmount, boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, nonExportedHumanMedicinesPlasticPackagingRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad.url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, nonExportedHumanMedicinesPlasticPackagingRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad.url
      }
    }
  }
}
