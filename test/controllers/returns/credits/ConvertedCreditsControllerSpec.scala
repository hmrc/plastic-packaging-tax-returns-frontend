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
import models.returns.CreditsAnswer
import navigation.ReturnsJourneyNavigator
import org.mockito.ArgumentMatchers.{eq => meq}
import org.mockito.Mockito.{reset, verify, verifyNoInteractions, when}
import org.mockito.{ArgumentCaptor, ArgumentMatchers}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import pages.returns.credits.{ConvertedCreditsPage, ExportedCreditsPage}
import play.api.data.Form
import play.api.http.Status
import play.api.i18n.MessagesApi
import play.api.mvc.{Call, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import views.html.returns.credits.ConvertedCreditsView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ConvertedCreditsControllerSpec extends PlaySpec with MockitoSugar with BeforeAndAfterEach {

  private val mockMessages: MessagesApi = mock[MessagesApi]
  private val mockCacheConnector: CacheConnector = mock[CacheConnector]
  private val mockNavigator: ReturnsJourneyNavigator = mock[ReturnsJourneyNavigator]
  private val mockView = mock[ConvertedCreditsView]
  private val formProvider = mock[ConvertedCreditsFormProvider]
  private val form = mock[Form[CreditsAnswer]]
  private val userAnswers = mock[UserAnswers]

  private val controllerComponents = stubMessagesControllerComponents()
  private val sut: ConvertedCreditsController = createSut(UserAnswers("123"))
  private val sut2: ConvertedCreditsController = createSut(userAnswers)
  
  private def any[T] = ArgumentMatchers.any[T]()

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(
      mockMessages,
      mockCacheConnector,
      mockNavigator, 
      mockView,
      formProvider,
      form,
      userAnswers
    )
    when(userAnswers.fill[CreditsAnswer](any, any)(any)) thenReturn form
    when(userAnswers.setOrFail(any, any, any)(any)) thenReturn userAnswers
    when(userAnswers.save(any)(any)) thenReturn Future.successful(userAnswers)

    when(formProvider.apply()).thenReturn(form)
    when(form.bindFromRequest()(any, any)).thenReturn(form)
    when(mockView.apply(any, any)(any, any)).thenReturn(Html("correct view"))

    when(mockCacheConnector.saveUserAnswerFunc(any)(any)) thenReturn ((_, _) => Future.successful(false))
    when(mockNavigator.convertedCreditsRoute(any, any)).thenReturn(Call("GET", "/next/page"))

  }

  "onPageLoad" must {
    
    "render the page" in {
      val result = sut2.onPageLoad(NormalMode)(FakeRequest("", ""))
      status(result) mustEqual Status.OK
      contentAsString(result) mustEqual "correct view"
    }

    "create the form from user answers" in {
      await(sut2.onPageLoad(NormalMode)(FakeRequest("", "")))
      verify(userAnswers).fill[CreditsAnswer](meq(ConvertedCreditsPage), meq(form))(any)
      verify(mockView).apply(meq(form), meq(NormalMode))(any, any)
    }
  }

  "onSubmit" must {

    "remember the user's answers" in {
      // Invokes the "form is good" side of the fold() call
      when(form.fold(any, any)).thenAnswer(i => i.getArgument[CreditsAnswer => Future[Result]](1).apply(CreditsAnswer(true, Some(20))))
      await(sut2.onSubmit(NormalMode)(FakeRequest("", "")))
      verify(userAnswers).setOrFail(meq(ConvertedCreditsPage), meq(CreditsAnswer(true, Some(20))), meq(false))(any)
      verify(userAnswers).save(any)(any)
    }

    "redirect to the next page" in {
      when(userAnswers.getOrFail(meq(ExportedCreditsPage))(any)) thenReturn CreditsAnswer(true, Some(11))
      when(userAnswers.getOrFail(meq(ConvertedCreditsPage))(any)) thenReturn CreditsAnswer(true, Some(22))

      // Invokes the "form is good" side of the fold() call
      when(form.fold(any, any)).thenAnswer(i => i.getArgument[CreditsAnswer => Future[Result]](1).apply(CreditsAnswer(false, None)))
      val result = sut2.onSubmit(NormalMode)(FakeRequest("", ""))
      status(result) mustEqual SEE_OTHER
      redirectLocation(result) mustBe Some("/next/page")
      val expectedCreditsClaim = ClaimedCredits(CreditsAnswer(true, Some(11)), CreditsAnswer(true, Some(22)))
      verify(mockNavigator).convertedCreditsRoute(meq(NormalMode), meq(expectedCreditsClaim))
    }

    "return 400 on error" in {
      when(mockView.apply(any, any)(any, any)).thenReturn(Html("correct view"))
      when(formProvider.apply()).thenReturn(new ConvertedCreditsFormProvider()())

      val result = sut.onSubmit(NormalMode)(FakeRequest("", "")
        .withFormUrlEncodedBody(("answer" -> "true")))

      status(result) mustEqual BAD_REQUEST
      formVerifyAndCapture.hasErrors mustBe true
      verifyNoInteractions(mockCacheConnector)
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
      formProvider,
      controllerComponents,
      mockView)
  }

  private def formVerifyAndCapture: Form[CreditsAnswer] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[CreditsAnswer]])
    verify(mockView).apply(captor.capture(), any)(any, any)
    captor.getValue
  }

}
