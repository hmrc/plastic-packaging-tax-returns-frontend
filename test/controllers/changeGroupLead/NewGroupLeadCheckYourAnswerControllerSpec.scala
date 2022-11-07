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

package controllers.changeGroupLead

import controllers.BetterMockActionSyntax
import controllers.actions.JourneyAction
import controllers.actions.JourneyAction.{RequestAsyncFunction, RequestFunction}
import models.UserAnswers
import models.changeGroupLead.RepresentativeMemberDetails
import models.requests.DataRequest
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.Mockito.{never, verifyNoInteractions}
import org.mockito.MockitoSugar.{mock, reset, verify, when}
import org.mockito.{Answers, ArgumentCaptor, ArgumentMatchers}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import pages.ChooseNewGroupLeadPage
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, RequestHeader}
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, defaultAwaitTimeout, redirectLocation, status, stubMessagesControllerComponents}
import play.twirl.api.HtmlFormat
import views.html.changeGroupLead.NewGroupLeadCheckYourAnswerView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class NewGroupLeadCheckYourAnswerControllerSpec extends PlaySpec with BeforeAndAfterEach{

  private val view: NewGroupLeadCheckYourAnswerView = mock[NewGroupLeadCheckYourAnswerView]
  private val journeyAction = mock[JourneyAction]
  private val dataRequest = mock[DataRequest[AnyContent]](Answers.RETURNS_DEEP_STUBS)
  private val messagesApi = mock[MessagesApi]
  private val messages = mock[Messages]
  private val featureGuard = mock[FeatureGuard]

  private val sut = new NewGroupLeadCheckYourAnswerController(
    messagesApi,
    journeyAction,
    featureGuard,
    controllerComponents = stubMessagesControllerComponents(),
    view
  )


  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(view, journeyAction, featureGuard, dataRequest)

    when(view.apply(any)(any, any)).thenReturn(HtmlFormat.empty)
    when(journeyAction.apply(any)) thenAnswer byConvertingFunctionArgumentsToAction
    when(journeyAction.async(any)) thenAnswer byConvertingFunctionArgumentsToFutureAction
    when(messagesApi.preferred(any[RequestHeader])) thenReturn messages
  }

  def byConvertingFunctionArgumentsToAction: (RequestFunction) => Action[AnyContent] = (function: RequestFunction) =>
    when(mock[Action[AnyContent]].apply(any))
      .thenAnswer((request: DataRequest[AnyContent]) =>
        Future.successful(function(request)))
      .getMock[Action[AnyContent]]

  def byConvertingFunctionArgumentsToFutureAction: (RequestAsyncFunction) => Action[AnyContent] = (function: RequestAsyncFunction) =>
    when(mock[Action[AnyContent]].apply(any))
      .thenAnswer((request: DataRequest[AnyContent]) =>
        function(request))
      .getMock[Action[AnyContent]]

  "onPageLoad" should {
    "return OK" in {

      when(dataRequest.userAnswers.get(ArgumentMatchers.eq(ChooseNewGroupLeadPage))(any))
        .thenReturn(None)
      val result = sut.onPageLoad.skippingJourneyAction(dataRequest)

      status(result) mustBe OK
    }

    "use the journey action" in {
      when(journeyAction.apply(any)) thenReturn mock[Action[AnyContent]]

      sut.onPageLoad(FakeRequest())

      verify(journeyAction).apply(any)
    }

    "get member from userAnswer" in {
      when(dataRequest.userAnswers.get(ArgumentMatchers.eq(ChooseNewGroupLeadPage))(any))
        .thenReturn(Some("test test"))

      await(sut.onPageLoad.skippingJourneyAction(dataRequest))

      verify(dataRequest.userAnswers).get(ArgumentMatchers.eq(ChooseNewGroupLeadPage))(any)
    }

    "show member details" in {
    when(dataRequest.userAnswers.get(ArgumentMatchers.eq(ChooseNewGroupLeadPage))(any))
        .thenReturn(Some("test test"))

      await(sut.onPageLoad.skippingJourneyAction(dataRequest))

      verifyAndCaptureValue.member mustBe "test test"
    }

    "check feature flag" in {
      when(dataRequest.userAnswers.get(ArgumentMatchers.eq(ChooseNewGroupLeadPage))(any))
        .thenReturn(Some("test test"))

      await(sut.onPageLoad.skippingJourneyAction(dataRequest))

      verify(featureGuard).check()
    }

    "do nothing if feature flag is not set" in {
      when(featureGuard.check()).thenThrow(new Exception("error"))

      intercept[Exception] {
        await(sut.onPageLoad.skippingJourneyAction(dataRequest))
      }

      verifyNoInteractions(dataRequest)
      verifyNoInteractions(view)
    }
  }

  "onSubmit" should {
    "use the journey action" in {
      when(journeyAction.async(any)) thenReturn mock[Action[AnyContent]]

      sut.onSubmit(FakeRequest())

      verify(journeyAction).async(any)
    }

    "redirect to confirmation page" in {
      val result = sut.onSubmit.skippingJourneyAction(dataRequest)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual
        controllers.changeGroupLead.routes.NewGroupLeadConfirmationController.onPageLoad.url
    }

    "check feature flag" in {
      await(sut.onSubmit.skippingJourneyAction(dataRequest))

      verify(featureGuard).check()
    }
  }
  private def verifyAndCaptureValue:  RepresentativeMemberDetails  = {
    val captor = ArgumentCaptor.forClass(classOf[RepresentativeMemberDetails])

    verify(view).apply(captor.capture())(any, any)
    captor.getValue
  }

}
