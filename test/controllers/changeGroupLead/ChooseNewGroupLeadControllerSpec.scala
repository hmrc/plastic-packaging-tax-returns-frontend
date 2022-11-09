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

import connectors.CacheConnector
import controllers.BetterMockActionSyntax
import controllers.actions.JourneyAction
import controllers.actions.JourneyAction.RequestAsyncFunction
import forms.changeGroupLead.SelectNewGroupLeadForm
import models.requests.DataRequest
import models.subscription.GroupMembers
import org.mockito.Answers
import org.mockito.ArgumentMatchers.{eq => meq}
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar.{mock, reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import pages.ChooseNewGroupLeadPage
import play.api.data.Form
import play.api.data.Forms.text
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import queries.{Gettable, Settable}
import services.SubscriptionService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.changeGroupLead.ChooseNewGroupLeadView

import scala.AnyRef.{eq => _}
import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future
import scala.util.Try

class ChooseNewGroupLeadControllerSpec extends PlaySpec with BeforeAndAfterEach {

  private val mockMessagesApi: MessagesApi = mock[MessagesApi]
  private val controllerComponents = stubMessagesControllerComponents()
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

  object TestException extends Exception("test")

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
      verify(mockView).apply(meq(form), meq(GroupMembers(Seq())))(any, any)
    }

    "get any previous user answer" in {
      when(mockFormProvider.apply(any)) thenReturn form
      await(sut.onPageLoad().skippingJourneyAction(dataRequest))
      verify(dataRequest.userAnswers).fill(meq(ChooseNewGroupLeadPage), meq(form))(any)
    }

    "create the form" in {
      await(sut.onPageLoad().skippingJourneyAction(dataRequest))
      verify(mockFormProvider).apply(groupMembers.membersNames)
    }

    "call the subscription service" in {
      val hc = mock[HeaderCarrier]
      when(dataRequest.headerCarrier).thenReturn(hc)
      when(dataRequest.pptReference).thenReturn("test-ppt-ref")
      await(sut.onPageLoad().skippingJourneyAction(dataRequest))
      verify(mockSubscriptionService).fetchGroupMemberNames("test-ppt-ref")(hc)
    }

    "error when the subscription service fails" in {
      when(mockSubscriptionService.fetchGroupMemberNames(any)(any)) thenReturn Future.failed(TestException)
      intercept[TestException.type] {
        await(sut.onPageLoad().skippingJourneyAction(dataRequest))
      }
    }
  }

  "onSubmit" must {
    "invoke the journey action" in {
      when(journeyAction.async(any)) thenReturn mock[Action[AnyContent]]
      sut.onSubmit()(FakeRequest())
      verify(journeyAction).async(any)
    }

    "invoke feature guard" in {
      Try(await(sut.onSubmit().skippingJourneyAction(dataRequest)))
      verify(featureGuard).check()
    }

    "bind the form and error" in {
      when(mockFormProvider.apply(any)) thenReturn form
      val errorForm = Form("value" -> text()).withError("key", "error")
      when(form.bindFromRequest()(any, any)).thenReturn(errorForm)

      val result = sut.onSubmit().skippingJourneyAction(dataRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "correct view"
      verify(mockView).apply(meq(errorForm), meq(GroupMembers(Seq())))(any, any)
      verify(mockFormProvider).apply(groupMembers.membersNames)
      verify(form).bindFromRequest()(meq(dataRequest),any)
    }

    "bind the form and bind a value" in {
      when(mockFormProvider.apply(any)) thenReturn form
      val boundForm = Form("value" -> text()).fill("test-member")
      when(form.bindFromRequest()(any, any)).thenReturn(boundForm)
      val userAnswers = dataRequest.userAnswers
      when(dataRequest.userAnswers.setOrFail(any[Settable[String]], any, any)(any)).thenReturn(userAnswers)
      when(mockCache.saveUserAnswerFunc(any)(any)).thenReturn({case _ => Future.successful(true)})
      when(userAnswers.save(any)(any)).thenReturn(Future.successful(userAnswers))

      val result = sut.onSubmit().skippingJourneyAction(dataRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.IndexController.onPageLoad.url) //todo this will be address page
      verify(mockFormProvider).apply(groupMembers.membersNames)
      verify(form).bindFromRequest()(meq(dataRequest),any)
      withClue("the selected member must be cached"){
        verify(dataRequest.userAnswers).setOrFail(ChooseNewGroupLeadPage, "test-member")
        verify(dataRequest.userAnswers).save(mockCache.saveUserAnswerFunc(dataRequest.pptReference)(dataRequest.headerCarrier))(global)
      }
    }

    "error" when {
      "the subscription service fails" in {
        when(mockSubscriptionService.fetchGroupMemberNames(any)(any)) thenReturn Future.failed(TestException)
        intercept[TestException.type] {
          await(sut.onSubmit().skippingJourneyAction(dataRequest))
        }
      }

      "the user answers setOrFail fails" in {
        when(mockFormProvider.apply(any)) thenReturn form
        val boundForm = Form("value" -> text()).fill("test-member")
        when(form.bindFromRequest()(any, any)).thenReturn(boundForm)
        when(dataRequest.userAnswers.setOrFail(any[Settable[String]], any, any)(any)).thenThrow(TestException)

        intercept[TestException.type](await(sut.onSubmit().skippingJourneyAction(dataRequest)))
      }

      "the cache save fails" in {
        when(mockFormProvider.apply(any)) thenReturn form
        val boundForm = Form("value" -> text()).fill("test-member")
        when(form.bindFromRequest()(any, any)).thenReturn(boundForm)
        val userAnswers = dataRequest.userAnswers
        when(dataRequest.userAnswers.setOrFail(any[Settable[String]], any, any)(any)).thenReturn(userAnswers)
        when(mockCache.saveUserAnswerFunc(any)(any)).thenReturn({case _ => Future.successful(true)})
        when(userAnswers.save(any)(any)).thenReturn(Future.failed(TestException))

        intercept[TestException.type](await(sut.onSubmit().skippingJourneyAction(dataRequest)))
      }
    }
  }


}
