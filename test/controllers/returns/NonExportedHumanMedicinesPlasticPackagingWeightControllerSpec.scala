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
import forms.returns.NonExportedHumanMedicinesPlasticPackagingWeightFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.{any, contains}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.returns.NonExportedHumanMedicinesPlasticPackagingWeightPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.returns.NonExportedHumanMedicinesPlasticPackagingWeightView

import scala.concurrent.Future

class NonExportedHumanMedicinesPlasticPackagingWeightControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new NonExportedHumanMedicinesPlasticPackagingWeightFormProvider()
  val form = formProvider()

  def onwardRoute = Call("GET", "/foo")

  val validAnswer = 0L
  val exportedAmount = 8L

  lazy val nonExportedHumanMedicinesPlasticPackagingWeightRoute = routes.NonExportedHumanMedicinesPlasticPackagingWeightController.onPageLoad(NormalMode).url

  val userAnswersWithExportAmount = userAnswers.set(NonExportedHumanMedicinesPlasticPackagingWeightPage, value = exportedAmount).success.value

  "NonExportedHumanMedicinesPlasticPackagingWeight Controller" - {

    "must return OK and the correct view for a GET" in {
      val ans = userAnswersWithExportAmount.set(NonExportedHumanMedicinesPlasticPackagingWeightPage, validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(ans)).build()

      running(application) {
        val request = FakeRequest(GET, nonExportedHumanMedicinesPlasticPackagingWeightRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[NonExportedHumanMedicinesPlasticPackagingWeightView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(0L, form.fill(validAnswer), NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val ans = userAnswersWithExportAmount.set(NonExportedHumanMedicinesPlasticPackagingWeightPage, validAnswer).success.value

      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any(), any())(any())) thenReturn Future.successful(mockResponse)

      val application =
        applicationBuilder(userAnswers = Some(ans))
          .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[CacheConnector].toInstance(mockCacheConnector)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, nonExportedHumanMedicinesPlasticPackagingWeightRoute)

        val view = application.injector.instanceOf[NonExportedHumanMedicinesPlasticPackagingWeightView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(0L, form.fill(validAnswer), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any(), any())(any())) thenReturn Future.successful(mockResponse)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithExportAmount))
          .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[CacheConnector].toInstance(mockCacheConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, nonExportedHumanMedicinesPlasticPackagingWeightRoute)
            .withFormUrlEncodedBody(("value", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, nonExportedHumanMedicinesPlasticPackagingWeightRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[NonExportedHumanMedicinesPlasticPackagingWeightView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(0L, boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, nonExportedHumanMedicinesPlasticPackagingWeightRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, nonExportedHumanMedicinesPlasticPackagingWeightRoute)
            .withFormUrlEncodedBody(("value", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
