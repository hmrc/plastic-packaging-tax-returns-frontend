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

import config.FrontendAppConfig
import connectors.CacheConnector
import controllers.actions.JourneyAction
import controllers.actions.JourneyAction.RequestAsyncFunction
import controllers.changeGroupLead.Test.BetterMockActionSyntax
import forms.changeGroupLead.SelectNewGroupLeadForm
import models.requests.DataRequest
import models.subscription.GroupMembers
import org.mockito.Answers
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.ArgumentMatchers.{eq => meq}
import org.mockito.MockitoSugar.{mock, reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.RecoverMethods.recoverToSucceededIf
import org.scalatestplus.play.PlaySpec
import pages.ChooseNewGroupLeadPage
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import queries.Gettable
import services.SubscriptionService
import views.html.changeGroupLead.ChooseNewGroupLeadView

import scala.concurrent.ExecutionContext.global
import scala.concurrent.{ExecutionContext, Future}
import scala.AnyRef.{eq => _}

object Test {
  implicit class BetterMockActionSyntax(action: Action[AnyContent]){
    def skippingJourneyAction(request: DataRequest[AnyContent]): Future[Result] =
      action.apply(request)
  }
}

class ChooseNewGroupLeadControllerSpec extends PlaySpec with BeforeAndAfterEach {

  private val mockMessagesApi: MessagesApi = mock[MessagesApi]
  private val controllerComponents = stubMessagesControllerComponents()
  private val mockAppConfig = mock[FrontendAppConfig]
  private val mockView = mock[ChooseNewGroupLeadView]
  private val mockFormProvider = mock[SelectNewGroupLeadForm]
  private val mockCache = mock[CacheConnector]
  private val mockSubscriptionService = mock[SubscriptionService]
  private val journeyAction = mock[JourneyAction]
  private val featureGuard = mock[FeatureGuard]
  private val dataRequest = mock[DataRequest[AnyContent]](Answers.RETURNS_DEEP_STUBS)
  private val form = mock[Form[String]]

  val sut = new ChooseNewGroupLeadController(
    mockMessagesApi,
    journeyAction,
    controllerComponents,
    mockView,
    mockFormProvider,
    mockCache,
    featureGuard,
    mockSubscriptionService
  )(global)

  private val groupMembers = GroupMembers(Seq())

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(
      mockMessagesApi,
      journeyAction,
      mockView,
      mockFormProvider,
      mockCache,
      mockSubscriptionService,
      featureGuard,
      form,
      dataRequest
    )

    when(mockView.apply(any, any)(any, any)).thenReturn(Html("correct view"))
    when(mockSubscriptionService.fetchGroupMemberNames(any)(any)) thenReturn Future.successful(groupMembers)
    when(dataRequest.userAnswers.fill(any[Gettable[String]], any)(any)) thenReturn form
    when(journeyAction.async(any)) thenAnswer byConvertingFunctionArgumentsToFutureAction
  }
  
  def byConvertingFunctionArgumentsToFutureAction: (RequestAsyncFunction) => Action[AnyContent] = (function: RequestAsyncFunction) =>
    when(mock[Action[AnyContent]].apply(any))
      .thenAnswer((request: DataRequest[AnyContent]) => function(request))
      .getMock[Action[AnyContent]]


  "onPageLoad" must {
    
    "invoke the journey action" in {
      when(journeyAction.async(any)) thenReturn mock[Action[AnyContent]]
      sut.onPageLoad()(FakeRequest())
      verify(journeyAction).async(any)
    }
    
    "invoke feature guard" in {
      await(sut.onPageLoad().skippingJourneyAction(dataRequest))
      verify(featureGuard).check()
    }
    
    "return a view" in {
      val result = sut.onPageLoad().skippingJourneyAction(dataRequest)
      status(result) mustBe OK
      contentAsString(result) mustBe "correct view"
      verify(mockView).apply(meq(form), meq(Seq()))(any, any)
    }
    
    "get any previous user answer" in {
      when(mockFormProvider.apply(any)) thenReturn form
      await(sut.onPageLoad().skippingJourneyAction(dataRequest))
      verify(dataRequest.userAnswers).fill(meq(ChooseNewGroupLeadPage), meq(form))(any)
    }
    
    "create the form" in {
      await(sut.onPageLoad().skippingJourneyAction(dataRequest))
      verify(mockFormProvider).apply(groupMembers)
    }
    
    "handle the subscription service failing" ignore {
      when(mockSubscriptionService.fetchGroupMemberNames(any)(any)) thenReturn Future.failed(new RuntimeException("oh no"))
      implicit val ec: ExecutionContext = global
      intercept[NoSuchElementException] {
        await(sut.onPageLoad().skippingJourneyAction(dataRequest))
      }
//      result mustBe Future.failed(new RuntimeException("oh no"))
    }
    
//    "feature guard" in {
//      //todo
//    }
//
//    "return the view with the form populated with the returned members" in {
//      when(mockView.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.raw("test view"))
//
//      val result: Future[Result] = sut.onPageLoad()(FakeRequest())
//      status(result) mustBe OK
//      contentAsString(result) mustBe "test view"
//    }
  }


}
