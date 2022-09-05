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

import base.{FakeIdentifierActionWithEnrolment, SpecBase}
import connectors.CacheConnector
import controllers.actions._
import forms.returns.credits.ExportedCreditsFormProvider
import models.Mode.NormalMode
import models.UserAnswers
import org.mockito.Mockito.reset
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import pages.returns.credits.ExportedCreditsPage
import play.api.Application
import play.api.http.Status._
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{AnyContentAsEmpty, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, contentAsString, defaultAwaitTimeout, running, status}
import views.html.returns.credits.ExportedCreditsView

class ExportedCreditsControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  def onwardRoute = Call("GET", "/foo")

  private val form = new ExportedCreditsFormProvider()
  private val view = mock[ExportedCreditsView]
  lazy val exportedCreditsRoute = controllers.returns.credits.routes.ExportedCreditsController.onPageLoad(NormalMode).url

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(view)
  }


  "ExportedCredits Controller" - {

    "must return OK and the correct view" - {

      "a GET is made" in {

        val ans = userAnswers.set(ExportedCreditsPage, 20L).success.value

        val application = applicationBuilder(userAnswers = Some(ans)).build()

        running(application) {
          val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, exportedCreditsRoute)
          val controller = application.injector.instanceOf[ExportedCreditsController]
          val result = controller.onPageLoad(NormalMode)(request)
          println(result)
          status(result) mustEqual OK
        }
      }
    }
  }


  private def buildApplication = {
    applicationBuilder(userAnswers = None).overrides(
      bind[ExportedCreditsView].toInstance(view),
      bind[CacheConnector].toInstance(cacheConnector),
    ).build()
  }


}
