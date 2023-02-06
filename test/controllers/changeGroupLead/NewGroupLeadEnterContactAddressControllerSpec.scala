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

package controllers.changeGroupLead

import connectors.CacheConnector
import controllers.BetterMockActionSyntax
import controllers.actions.JourneyAction
import controllers.actions.JourneyAction.RequestAsyncFunction
import forms.changeGroupLead.NewGroupLeadEnterContactAddressFormProvider
import forms.changeGroupLead.NewGroupLeadEnterContactAddressFormProvider.{addressLine1, addressLine2, addressLine4, countryCode}
import models.Mode.NormalMode
import models.changeGroupLead.NewGroupLeadAddressDetails
import models.requests.DataRequest
import models.subscription.Member
import navigation.ChangeGroupLeadNavigator
import org.mockito.Answers
import org.mockito.ArgumentMatchers.{eq => meq}
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.Mockito.{reset, verifyNoInteractions, verifyNoMoreInteractions}
import org.mockito.MockitoSugar.{mock, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import pages.changeGroupLead.NewGroupLeadEnterContactAddressPage
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import queries.{Gettable, Settable}
import services.CountryService
import views.html.changeGroupLead.NewGroupLeadEnterContactAddressView

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future
import scala.util.Try

class NewGroupLeadEnterContactAddressControllerSpec extends PlaySpec with BeforeAndAfterEach {

  private val mockMessagesApi: MessagesApi = mock[MessagesApi]
  private val controllerComponents = stubMessagesControllerComponents()
  private val mockView = mock[NewGroupLeadEnterContactAddressView]
  private val mockFormProvider = mock[NewGroupLeadEnterContactAddressFormProvider]
  private val mockCache = mock[CacheConnector]
  private val journeyAction = mock[JourneyAction]
  private val featureGuard = mock[FeatureGuard]
  private val dataRequest = mock[DataRequest[AnyContent]](Answers.RETURNS_DEEP_STUBS)
  private val form = mock[Form[NewGroupLeadAddressDetails]]
  private val mockNavigator =  mock[ChangeGroupLeadNavigator]
  private val mockCountryService = mock[CountryService]

  val sut = new NewGroupLeadEnterContactAddressController(
    mockMessagesApi,
    mockCache,
    mockNavigator,
    journeyAction,
    featureGuard,
    mockFormProvider,
    mockCountryService,
    controllerComponents,
    mockView
  )(global)

  object TestException extends Exception("test")

  val countryMap = Map("key" -> "value")

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockMessagesApi,
      mockCache,
      mockNavigator,
      journeyAction,
      featureGuard,
      mockFormProvider,
      form,
      mockView,
      dataRequest.userAnswers)

    when(mockView.apply(any, any, any, any)(any, any)).thenReturn(Html("correct view"))
    when(dataRequest.userAnswers.fill(any[Gettable[NewGroupLeadAddressDetails]], any)(any)) thenReturn form
    when(dataRequest.userAnswers.getOrFail(any[Gettable[Member]])(any)) thenReturn Member("organisation-name", "1")
    when(journeyAction.async(any)) thenAnswer byConvertingFunctionArgumentsToFutureAction
    when(mockNavigator.enterContactAddress(any)).thenReturn(Call("GET", "/test-foo"))
    when(mockCountryService.getAll).thenReturn(countryMap)

    when(mockFormProvider.apply()) thenReturn form
    val userAnswers = dataRequest.userAnswers
    when(userAnswers.setOrFail(any[Settable[String]], any, any)(any)).thenReturn(userAnswers)
    when(mockCache.saveUserAnswerFunc(any)(any)).thenReturn({ case _ => Future.successful(true) })
    when(userAnswers.save(any)(any)).thenReturn(Future.successful(userAnswers))
    val createBindForm = new NewGroupLeadEnterContactAddressFormProvider().apply().bind(
      Map(
        addressLine1 -> "1 road",
        addressLine2 -> "1 road",
        addressLine4 -> "London",
        countryCode -> "EN"
      ))
    when(form.bindFromRequest()(any, any)).thenReturn(createBindForm)

  }

  def byConvertingFunctionArgumentsToFutureAction: (RequestAsyncFunction) => Action[AnyContent] = (function: RequestAsyncFunction) =>
    when(mock[Action[AnyContent]].apply(any))
      .thenAnswer((request: DataRequest[AnyContent]) => function(request))
      .getMock[Action[AnyContent]]

  "onPageLoad" must {

    "invoke the journey action" in {
      when(journeyAction.async(any)) thenReturn mock[Action[AnyContent]]
      Try(await(sut.onSubmit(NormalMode)(FakeRequest())))
      verify(journeyAction).async(any)
    }

    "invoke feature guard" in {
      await(sut.onPageLoad(NormalMode).skippingJourneyAction(dataRequest))
      verify(featureGuard).check()
    }

    "return OK and correct view" in {
      val result = sut.onPageLoad(NormalMode).skippingJourneyAction(dataRequest)
      status(result) mustBe OK
      contentAsString(result) mustBe "correct view"
      verify(mockView).apply(meq(form), meq(countryMap), meq("organisation-name"), meq(NormalMode))(any, any)
    }

    "get any previous user answer" in {
      await(sut.onPageLoad(NormalMode).skippingJourneyAction(dataRequest))
      verify(dataRequest.userAnswers).fill(meq(NewGroupLeadEnterContactAddressPage), meq(form))(any)
    }

    "create the form" in {
      await(sut.onPageLoad(NormalMode).skippingJourneyAction(dataRequest))
      verify(mockFormProvider).apply()
    }
  }
  "onSubmit" must {
    "invoke the journey action" in {
      when(journeyAction.async(any)) thenReturn mock[Action[AnyContent]]
      Try(await(sut.onSubmit(NormalMode)(FakeRequest())))
      verify(journeyAction).async(any)
    }

    "invoke feature guard" in {
      Try(await(sut.onSubmit(NormalMode).skippingJourneyAction(dataRequest)))
      verify(featureGuard).check()
    }

    "bind the form and error" in {
      val newForm = new NewGroupLeadEnterContactAddressFormProvider().apply().withError("error", "error")
      when(form.bindFromRequest()(any, any)).thenReturn(newForm)

      val result = sut.onSubmit(NormalMode).skippingJourneyAction(dataRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "correct view"
      verify(mockView).apply(meq(newForm), meq(countryMap), meq("organisation-name"),  meq(NormalMode))(any, any)
      verify(mockFormProvider).apply()
      verify(form).bindFromRequest()(meq(dataRequest),any)
    }

    "redirect to a new page" in {

      val result = sut.onSubmit(NormalMode).skippingJourneyAction(dataRequest)

      status(result) mustBe SEE_OTHER

    }

    "save the user answer to the cache" in {

      await(sut.onSubmit(NormalMode).skippingJourneyAction(dataRequest))

      verify(mockCache).saveUserAnswerFunc(dataRequest.pptReference)(dataRequest.headerCarrier)

    }

    "call the navigator" in {

      val result = sut.onSubmit(NormalMode).skippingJourneyAction(dataRequest)

      redirectLocation(result) mustBe Some("/test-foo")
      verify(mockNavigator).enterContactAddress(NormalMode)

    }

    "error" when {
      "the user answers setOrFail fails" in {
        when(dataRequest.userAnswers.setOrFail(any[Settable[String]], any, any)(any)).thenThrow(TestException)

        intercept[TestException.type](await(sut.onSubmit(NormalMode).skippingJourneyAction(dataRequest)))

        verifyNoMoreInteractions(mockCache, mockNavigator)
      }

      "the cache save fails" in {
        when(dataRequest.userAnswers.save(any)(any)).thenReturn(Future.failed(TestException))
        intercept[TestException.type](await(sut.onSubmit(NormalMode).skippingJourneyAction(dataRequest)))
        verifyNoInteractions(mockNavigator)
      }
    }
  }
}
