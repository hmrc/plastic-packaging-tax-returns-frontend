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

package controllers.returns.credits

import akka.stream.testkit.NoMaterializer
import base.FakeIdentifierActionWithEnrolment
import connectors.CacheConnector
import controllers.actions.{DataRequiredActionImpl, FakeDataRetrievalAction}
import forms.returns.credits.ExportedCreditsFormProvider
import models.Mode.NormalMode
import models.UserAnswers
import models.returns.CreditsAnswer
import navigation.ReturnsJourneyNavigator
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify}
import org.mockito.MockitoSugar.when
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.data.Form
import play.api.http.Status._
import play.api.i18n.MessagesApi
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, defaultAwaitTimeout, status, stubMessagesControllerComponents, stubPlayBodyParsers}
import play.twirl.api.Html
import uk.gov.hmrc.http.HttpResponse
import views.html.returns.credits.ExportedCreditsView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ExportedCreditsControllerSpec extends PlaySpec with MockitoSugar with BeforeAndAfterEach {

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
      when(mockForm.apply()).thenReturn(new ExportedCreditsFormProvider()())
      when(mockCacheConnector.set(any(), any())(any())).thenReturn(Future.successful(HttpResponse.apply(200, "")))
      when(mockNavigator.exportedCreditsRoute(NormalMode)).thenReturn(Call("GET", "/foo"))

      val result = sut.onSubmit(NormalMode)(FakeRequest("POST", "")
        .withFormUrlEncodedBody(("answer" -> "false")))

      status(result) mustEqual SEE_OTHER
    }

    "return 400 on error" in {
      when(mockView.apply(any(), any())(any(), any())).thenReturn(Html("correct view"))
      when(mockForm.apply()).thenReturn(new ExportedCreditsFormProvider()())

      val result = sut.onSubmit(NormalMode)(FakeRequest("POST", "")
        .withFormUrlEncodedBody(("answer" -> "true")))

      status(result) mustEqual BAD_REQUEST
      formVerifyAndCapture.hasErrors mustBe true
    }
  }

  private def formVerifyAndCapture: Form[CreditsAnswer] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[CreditsAnswer]])
    verify(mockView).apply(captor.capture(), any())(any(), any())
    captor.getValue
  }

}
