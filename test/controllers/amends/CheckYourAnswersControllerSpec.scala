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

package controllers.amends

import base.SpecBase
import config.FrontendAppConfig
import models.UserAnswers
import models.Mode.NormalMode
import org.mockito.Mockito.when
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Application, inject}
import viewmodels.govuk.SummaryListFluency
import views.html.amends.CheckYourAnswersView

import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {

  override def applicationBuilder(userAnswers: Option[UserAnswers]): GuiceApplicationBuilder =
    super.applicationBuilder(userAnswers)
      .overrides(inject.bind[FrontendAppConfig].toInstance(config))

  "(Amend journey) Check Your Answers Controller" - {

    "must redirect to account page when amends toggle is disabled" in{
      when(config.isAmendsFeatureEnabled).thenReturn(false)

      val application: Application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)

        val result: Future[Result] = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.IndexController.onPageLoad.url
         }
    }

    "must return OK and the correct view for a GET" in {
      when(config.isAmendsFeatureEnabled).thenReturn(true)
      when(config.userResearchUrl).thenReturn("some Url")

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckYourAnswersView]
        val list = SummaryListViewModel(Seq.empty)
        val mode = NormalMode

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(mode, list, taxReturnOb)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      when(config.isAmendsFeatureEnabled).thenReturn(true)

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad.url
      }
    }

    "must redirect when previous tax return is not in user answers" in {
      when(config.isAmendsFeatureEnabled).thenReturn(true)

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET,  routes.CheckYourAnswersController.onPageLoad.url)

        val result = route(application, request).value

        redirectLocation(result) mustBe Some(routes.SubmittedReturnsController.onPageLoad().url)
      }
    }
  }
}
