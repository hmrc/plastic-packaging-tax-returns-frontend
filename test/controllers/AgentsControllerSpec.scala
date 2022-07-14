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

package controllers

import base.SpecBase
import forms.AgentsFormProvider
import models.NormalMode
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.AgentsView

class AgentsControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")
  val formProvider = new AgentsFormProvider()

  lazy val agentsRoute = routes.AgentsController.onPageLoad(NormalMode).url

  "Agents Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilderAgent().build()

      running(application) {
        val request = FakeRequest(GET, agentsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AgentsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to account page when valid data is submitted" in {

      val application = applicationBuilderAgent().build()

      running(application) {
        val request =
          FakeRequest(POST, agentsRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.IndexController.onPageLoad.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilderAgent().build()

      running(application) {
        val request =
          FakeRequest(POST, agentsRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = formProvider().bind(Map("value" -> ""))

        val view = application.injector.instanceOf[AgentsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "show error" - {

      "not enrolled has flashed an auth error" in {

        val application = applicationBuilderAgent().build()

        running(application) {
          val request =
            FakeRequest(GET, agentsRoute)
              .withFormUrlEncodedBody(("value", "answer"))
              .withFlash(data = ("clientPPTFailed", "true"))

          val result = route(application, request).value

          status(result) mustEqual FORBIDDEN

        }
      }
    }
  }
}
