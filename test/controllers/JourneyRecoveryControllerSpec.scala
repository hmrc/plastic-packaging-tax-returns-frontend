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

package controllers

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.InsufficientEnrolments
import views.html.JourneyRecoveryView

class JourneyRecoveryControllerSpec extends SpecBase {

  "JourneyRecovery Controller" - {

    "must return OK and the start again view" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {

          val request = FakeRequest(GET, routes.JourneyRecoveryController.onPageLoad.url)

          val result = route(application, request).value

          val startAgainView = application.injector.instanceOf[JourneyRecoveryView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual startAgainView()(request,
                                                             messages(application)
          ).toString
        }
      }

    "must return an error if not authorized" in {
      val application = applicationBuilderFailedAuth().build()

      running(application) {
        val request =
          FakeRequest(GET, routes.JourneyRecoveryController.onPageLoad.url)

        val result = route(application, request).value

        intercept[InsufficientEnrolments](status(result))
      }
    }
  }
}
