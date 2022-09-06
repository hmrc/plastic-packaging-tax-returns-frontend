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

import akka.stream.testkit.NoMaterializer
import base.FakeIdentifierActionWithEnrolment
import connectors.CacheConnector
import controllers.actions.{DataRequiredActionImpl, FakeDataRetrievalAction}
import forms.returns.credits.ExportedCreditsFormProvider
import models.Mode.NormalMode
import models.UserAnswers
import models.returns.ExportedCreditsAnswer
import navigation.ReturnsJourneyNavigator
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.reset
import org.mockito.MockitoSugar.when
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.data.Form
import play.api.http.Status._
import play.api.i18n.MessagesApi
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, POST, defaultAwaitTimeout, status, stubMessagesControllerComponents, stubPlayBodyParsers}
import play.twirl.api.Html
import views.html.returns.credits.ExportedCreditsView

import scala.concurrent.ExecutionContext.Implicits.global

class ExportedCreditsControllerSpec extends PlaySpec with MockitoSugar with BeforeAndAfterEach {

  def onwardRoute = Call("GET", "/foo")

  lazy val exportedCreditsRoute = controllers.returns.credits.routes.ExportedCreditsController.onPageLoad(NormalMode).url

  private val mockMessages: MessagesApi = mock[MessagesApi]
  private val mockCacheConnector: CacheConnector = mock[CacheConnector]
  private val mockNavigator: ReturnsJourneyNavigator = mock[ReturnsJourneyNavigator]
  private val controllerComponents = stubMessagesControllerComponents()
  private val mockView = mock[ExportedCreditsView]
  private val mockForm = mock[ExportedCreditsFormProvider]

  val sut: ExportedCreditsController = new ExportedCreditsController(
    mockMessages,
    mockCacheConnector,
    mockNavigator,
    new FakeIdentifierActionWithEnrolment(stubPlayBodyParsers(NoMaterializer)),
    new FakeDataRetrievalAction(Some(UserAnswers("123"))),
    new DataRequiredActionImpl(),
    mockForm,
    controllerComponents,
    mockView)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockView, mockCacheConnector, mockForm)
  }

  val validAnswer: ExportedCreditsAnswer = new ExportedCreditsAnswer(yesNo = true, weight = Some(30L))
  val x = mock[Form[ExportedCreditsAnswer]]
  val realFormWithError = new ExportedCreditsFormProvider()().withError("exportedCredits.error.required", "err")

  "ExportedCredits Controller" must {

    "return OK and the correct view" when {

      "a GET is made" in {
        when(mockView.apply(any(), any())(any(), any())).thenReturn(Html("correct view"))
        val result = sut.onPageLoad(NormalMode)(FakeRequest(GET, "/foo"))

        status(result) mustEqual OK
      }
    }
    "must redirect to the next page when No is submitted" in {
      when(mockView.apply(any(), any())(any(), any())).thenReturn(Html("correct view"))
      when(mockForm.apply()).thenReturn(x)

      val value1 = mock[Form[ExportedCreditsAnswer]]
      when(x.bindFromRequest()(any(),any())).thenReturn(value1)

      val result = sut.onSubmit(NormalMode)(FakeRequest(POST, "/foo"))

      status(result) mustEqual SEE_OTHER


    }

    "return 400 on error" ignore {
      ???
    }
  }

}
