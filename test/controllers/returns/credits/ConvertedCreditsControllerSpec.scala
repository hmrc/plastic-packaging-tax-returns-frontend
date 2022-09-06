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
import models.Mode.NormalMode
import models.returns.ConvertedCreditsAnswer
import org.mockito.Mockito.reset
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.returns.credits.ConvertedCreditsPage
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, POST, defaultAwaitTimeout, running, status}
import views.html.returns.credits.ConvertedCreditsView

class ConvertedCreditsControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  def onwardRoute = Call("GET", "/foo")

  private val view = mock[ConvertedCreditsView]
  lazy val ConvertedCreditsRoute = controllers.returns.credits.routes.ConvertedCreditsController.onPageLoad(NormalMode).url

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(view)
  }

  val validYesAnswer: ConvertedCreditsAnswer = new ConvertedCreditsAnswer(yesNo = true, weight = Some(30L))
  val validNoAnswer: ConvertedCreditsAnswer = new ConvertedCreditsAnswer(yesNo = false, weight = None)


  "ConvertedCredits Controller" - {

    "must return OK and the correct view" - {

      "a GET is made" in {

        val ans = userAnswers.set(ConvertedCreditsPage, validYesAnswer).success.value

        val application = applicationBuilder(userAnswers = Some(ans)).build()

        running(application) {
          val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, ConvertedCreditsRoute)
          val controller = application.injector.instanceOf[ConvertedCreditsController]
          val result = controller.onPageLoad(NormalMode)(request)

          status(result) mustEqual OK
        }
      }
    }
    "must redirect to the next page when No is submitted" in {
      val ans = userAnswers.set(ConvertedCreditsPage, validNoAnswer).success.value
      val mockCacheConnector = mock[CacheConnector]

      val application = applicationBuilder(userAnswers = Some(ans))
        .overrides(
          bind[CacheConnector].toInstance(mockCacheConnector))
        .build()

      running(application) {
        val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(POST, ConvertedCreditsRoute)
        val controller = application.injector.instanceOf[ConvertedCreditsController]
        val result = controller.onSubmit(NormalMode)(request)

        println(result)

        status(result) mustEqual SEE_OTHER
      }
    }
    "must redirect to the next page when Yes and weight is submitted" ignore {
      ???
    }
  }
}
