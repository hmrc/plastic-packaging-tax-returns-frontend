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
import forms.returns.credits.ConvertedCreditsFormProvider
import models.Mode.NormalMode
import models.UserAnswers
import models.returns.ConvertedCreditsAnswer
import navigation.ReturnsJourneyNavigator
import org.mockito.{ArgumentCaptor, ArgumentMatchers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify}
import org.mockito.MockitoSugar.when
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import pages.returns.credits.ConvertedCreditsPage
import play.api.data.Form
import play.api.http.Status._
import play.api.i18n.MessagesApi
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, defaultAwaitTimeout, status, stubMessagesControllerComponents, stubPlayBodyParsers}
import play.twirl.api.Html
import uk.gov.hmrc.http.HttpResponse
import views.html.returns.credits.ConvertedCreditsView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ConvertedCreditsControllerSpec extends PlaySpec with MockitoSugar with BeforeAndAfterEach {


  private val mockMessages: MessagesApi = mock[MessagesApi]
  private val mockCacheConnector: CacheConnector = mock[CacheConnector]
  private val mockNavigator: ReturnsJourneyNavigator = mock[ReturnsJourneyNavigator]
  private val controllerComponents = stubMessagesControllerComponents()
  private val mockView = mock[ConvertedCreditsView]
  private val mockForm = mock[ConvertedCreditsFormProvider]

  val sut: ConvertedCreditsController = new ConvertedCreditsController(
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

  "onPageLoad" must {

    "return OK" in {

      when(mockView.apply(any(), any())(any(), any())).thenReturn(Html("correct view"))
      val result = sut.onPageLoad(NormalMode)(FakeRequest(GET, "/foo"))

      status(result) mustEqual OK
    }

  }

  "onSumbit" must {

    "redirect to newStartReturn page if no credits" in {
      setUpMocks

      val result = sut.onSubmit(NormalMode)(FakeRequest("POST", "")
        .withFormUrlEncodedBody(("answer" -> "false")))

      status(result) mustEqual SEE_OTHER
      verify(mockNavigator).convertedCreditsRoute(ArgumentMatchers.eq(NormalMode), ArgumentMatchers.eq(Some(false)))

    }
    "redirect to confirmCredits page if credits claimed" when {
      "ConvertedCredits is claimed" in {
        setUpMocks
        val answers = UserAnswers("123").set(ConvertedCreditsPage, ConvertedCreditsAnswer(true,Some(20))).get
        val result = createSut(answers).onSubmit(NormalMode)(FakeRequest("POST", "")
          .withFormUrlEncodedBody(
            "answer" -> "true",
            "converted-credits-weight" -> "20"))

        status(result) mustEqual SEE_OTHER
        verify(mockNavigator).convertedCreditsRoute(ArgumentMatchers.eq(NormalMode), ArgumentMatchers.eq(Some(true)))
      }

    }

    "redirect to the next page" in {

      setUpMocks

      val result = sut.onSubmit(NormalMode)(FakeRequest("POST", "")
        .withFormUrlEncodedBody(("answer" -> "false")))

      status(result) mustEqual SEE_OTHER
    }

    "return 400 on error" in {
      when(mockView.apply(any(), any())(any(), any())).thenReturn(Html("correct view"))
      when(mockForm.apply()).thenReturn(new ConvertedCreditsFormProvider()())

      val result = sut.onSubmit(NormalMode)(FakeRequest("POST", "")
        .withFormUrlEncodedBody(("answer" -> "true")))

      status(result) mustEqual BAD_REQUEST
      formVerifyAndCapture.hasErrors mustBe true
    }

  }

  private def createSut(userAnswers: UserAnswers): ConvertedCreditsController = {
    new ConvertedCreditsController(
      mockMessages,
      mockCacheConnector,
      mockNavigator,
      new FakeIdentifierActionWithEnrolment(stubPlayBodyParsers(NoMaterializer)),
      new FakeDataRetrievalAction(Some(userAnswers)),
      new DataRequiredActionImpl(),
      mockForm,
      controllerComponents,
      mockView)
  }


  private def setUpMocks = {
    when(mockView.apply(any(), any())(any(), any())).thenReturn(Html("correct view"))
    when(mockForm.apply()).thenReturn(new ConvertedCreditsFormProvider()())
    when(mockCacheConnector.set(any(), any())(any())).thenReturn(Future.successful(HttpResponse.apply(200, "")))
    when(mockNavigator.convertedCreditsRoute(any(), any())).thenReturn(Call("GET", "/foo"))
  }

  private def formVerifyAndCapture: Form[ConvertedCreditsAnswer] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[ConvertedCreditsAnswer]])
    verify(mockView).apply(captor.capture(), any())(any(), any())
    captor.getValue
  }

}
