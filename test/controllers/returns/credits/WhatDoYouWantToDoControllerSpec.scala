/*
 * Copyright 2025 HM Revenue & Customs
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

import base.utils.JourneyActionAnswer
import cacheables.ReturnObligationCacheable
import connectors.CacheConnector
import controllers.actions.JourneyAction
import forms.returns.credits.DoYouWantToClaimFormProvider
import models.requests.DataRequest
import models.returns.TaxReturnObligation
import navigation.ReturnsJourneyNavigator
import org.mockito.ArgumentMatchersSugar.{any, eqTo}
import org.mockito.MockitoSugar
import org.mockito.stubbing.ReturnsDeepStubs
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import pages.returns.credits.WhatDoYouWantToDoPage
import play.api.data.Form
import play.api.data.Forms.boolean
import play.api.http.Status
import play.api.http.Status.OK
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, Call}
import play.api.test.Helpers._
import play.twirl.api.Html
import queries.Gettable
import views.html.returns.credits.DoYouWantToClaimView

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class WhatDoYouWantToDoControllerSpec
    extends PlaySpec
    with JourneyActionAnswer
    with MockitoSugar
    with BeforeAndAfterEach {

  private val messagesApi: MessagesApi = mock[MessagesApi]
  private val cacheConnector           = mock[CacheConnector]
  private val journeyAction            = mock[JourneyAction]
  private val formProvider             = mock[DoYouWantToClaimFormProvider]
  private val controllerComponents     = stubMessagesControllerComponents()
  private val view                     = mock[DoYouWantToClaimView]
  private val navigator                = mock[ReturnsJourneyNavigator]
  private val dataRequest              = mock[DataRequest[AnyContent]](ReturnsDeepStubs)
  private val form                     = mock[Form[Boolean]]
  private val obligation               = mock[TaxReturnObligation]

  val sut = new WhatDoYouWantToDoController(
    messagesApi,
    cacheConnector,
    journeyAction,
    formProvider,
    controllerComponents,
    view,
    navigator
  )(global)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(
      journeyAction,
      dataRequest,
      navigator,
      form,
      obligation,
      cacheConnector,
      formProvider
    )
    when(journeyAction.apply(any)).thenAnswer(byConvertingFunctionArgumentsToAction)
    when(journeyAction.async(any)).thenAnswer(byConvertingFunctionArgumentsToFutureAction)
    when(view.apply(any, any)(any, any)).thenReturn(Html("correct view"))
    when(formProvider.apply()).thenReturn(form)

    when(dataRequest.userAnswers.get(eqTo(ReturnObligationCacheable))(any)) thenReturn Some(obligation)
    when(dataRequest.userAnswers.getOrFail(eqTo(ReturnObligationCacheable))(any, any)).thenReturn(obligation)

  }

  "onPageLoad" must {
    "use the journey action" in {
      when(journeyAction.apply(any)).thenReturn(mock[Action[AnyContent]])
      sut.onPageLoad
      verify(journeyAction).apply(any)
    }

    "return a 200" in {
      val result = sut.onPageLoad(dataRequest)
      status(result) mustBe OK
      contentAsString(result) mustBe "correct view"
    }

    "take in a value from form and return a view" in {
      val validForm = Form("value" -> boolean).fill(true)
      when(dataRequest.userAnswers.fill(any[Gettable[Boolean]], any)(any)).thenReturn(validForm)

      val result = sut.onPageLoad(dataRequest)
      status(result) mustBe OK
      verify(dataRequest.userAnswers).fill(eqTo(WhatDoYouWantToDoPage), eqTo(form))(any)
      verify(view).apply(eqTo(validForm), eqTo(obligation))(any, any)
    }

    "redirect to account page if obligation is missing" in {
      when(dataRequest.userAnswers.get(eqTo(ReturnObligationCacheable))(any)) thenReturn None
      val result = sut.onPageLoad(dataRequest)
      status(result) mustBe Status.SEE_OTHER
      redirectLocation(result).value mustBe controllers.routes.IndexController.onPageLoad.url
    }

  }

  "onSubmit" must {
    "use the journey action" in {
      when(journeyAction.async(any)).thenReturn(mock[Action[AnyContent]])
      sut.onSubmit
      verify(journeyAction).async(any)
    }

    "redirect to the next page" in {
      when(navigator.whatDoYouWantDo(any)).thenReturn(Call(GET, "/foo"))
      when(form.bindFromRequest()(any, any)).thenReturn(Form("value" -> boolean).fill(true))
      when(dataRequest.userAnswers.change(any, any, any)(any)).thenReturn(Future.successful(true))

      val result = await(sut.onSubmit(dataRequest))
      verify(cacheConnector).saveUserAnswerFunc(eqTo(dataRequest.pptReference))(any)
      verify(navigator).whatDoYouWantDo(eqTo(true))
      verify(dataRequest.userAnswers, never).get(eqTo(ReturnObligationCacheable))(any)

      result.header.status mustBe SEE_OTHER
      redirectLocation(Future.successful(result)) mustBe Some("/foo")
    }

    "return bad request when form validation fails" in {
      val formWithErrors = Form("v" -> boolean).withError("key", "message")
      when(form.bindFromRequest()(any, any)).thenReturn(formWithErrors)

      val result = await(sut.onSubmit(dataRequest))
      verify(dataRequest.userAnswers).getOrFail(eqTo(ReturnObligationCacheable))(any, any)
      verify(view).apply(eqTo(formWithErrors), eqTo(obligation))(any, any)

      result.header.status mustBe BAD_REQUEST
      contentAsString(Future.successful(result)) mustBe "correct view"

    }
  }

}
