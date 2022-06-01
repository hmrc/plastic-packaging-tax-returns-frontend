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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.mockito.stubbing.OngoingStubbing
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.{Entry, SessionRepository}
import views.html.returns.ReturnConfirmationView

import scala.concurrent.Future

class ReturnConfirmationControllerSpec extends SpecBase {

  def setupMock: OngoingStubbing[Future[Option[Entry]]] = {
    reset(mockSessionRepo)
    when(mockSessionRepo.get(any())).thenReturn(Future.successful(Some(Entry("ID", Some("12345")))))
  }

  "ReturnConfirmation Controller" - {

    "must return OK and the correct view for a GET" in {

      setupMock

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).overrides(
        bind[SessionRepository].toInstance(mockSessionRepo)
      ).build()

      val expected = Some("12345")

      running(application) {
        val request = FakeRequest(GET, controllers.returns.routes.ReturnConfirmationController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ReturnConfirmationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(expected)(request, messages(application)).toString
      }
    }
  }
}
