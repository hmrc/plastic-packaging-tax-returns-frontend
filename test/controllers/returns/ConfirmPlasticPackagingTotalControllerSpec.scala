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

package controllers.returns


import base.utils.JourneyActionAnswer
import connectors.CacheConnector
import controllers.BetterMockActionSyntax
import controllers.actions.JourneyAction
import controllers.helpers.NonExportedAmountHelper
import models.UserAnswers
import models.requests.DataRequest
import navigation.ReturnsJourneyNavigator
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar.{reset, verify, when}
import org.mockito.{Answers, ArgumentCaptor, ArgumentMatchers}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import pages.returns._
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, RequestHeader}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import views.html.returns.ConfirmPlasticPackagingTotalView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

class ConfirmPlasticPackagingTotalControllerSpec
  extends PlaySpec
    with MockitoSugar
    with JourneyActionAnswer
    with BeforeAndAfterEach {

  private val dataRequest = mock[DataRequest[AnyContent]](Answers.RETURNS_DEEP_STUBS)
  private val messages = mock[Messages]
  private val messagesApi = mock[MessagesApi]
  private val journeyAction = mock[JourneyAction]
  private val view = mock[ConfirmPlasticPackagingTotalView]
  private val cacheConnector = mock[CacheConnector]
  private val navigator = mock[ReturnsJourneyNavigator]
  private val nonExportedAmountHelper = mock[NonExportedAmountHelper]

  private val sut = new ConfirmPlasticPackagingTotalController(
    messagesApi,
    journeyAction,
    stubMessagesControllerComponents(),
    view,
    cacheConnector,
    navigator,
    nonExportedAmountHelper
  )

  override def beforeEach() = {
    super.beforeEach()

    reset(journeyAction, view,cacheConnector, dataRequest, messages, navigator)

    when(view.apply(any)(any, any)).thenReturn(HtmlFormat.empty)
    when(journeyAction.apply(any)).thenAnswer(byConvertingFunctionArgumentsToAction)
    when(journeyAction.async(any)).thenAnswer(byConvertingFunctionArgumentsToFutureAction)
    when(messagesApi.preferred(any[RequestHeader])).thenReturn(messages)
    when(nonExportedAmountHelper.totalPlastic(any)).thenReturn(Some(50L))
  }

  "onPageLoad" should {

    "use the journey action" in {
      sut.onPageLoad
      verify(journeyAction).apply(any)
    }

    "return OK" in {
      when(dataRequest.userAnswers).thenReturn(createUserAnswer)

      val result = sut.onPageLoad.skippingJourneyAction(dataRequest)

      status(result) mustEqual OK
    }

    "pass a summary list to the view" in {
      when(dataRequest.userAnswers).thenReturn(createUserAnswer)
      when(messages.apply("site.yes")).thenReturn("Yes")
      when(messages.apply("10kg")).thenReturn("manufacture weight")
      when(messages.apply("1kg")).thenReturn("imported weigh")
      when(messages.apply("50kg")).thenReturn("total weight")

      await(sut.onPageLoad.skippingJourneyAction(dataRequest))

      val captor: ArgumentCaptor[SummaryList] = ArgumentCaptor.forClass(classOf[SummaryList])
      verify(view).apply(captor.capture())(any, any)
      captor.getValue.rows.length mustEqual 5
      captor.getValue.rows(0).value.content.asHtml.toString() mustEqual "Yes"
      captor.getValue.rows(1).value.content.asHtml.toString() mustEqual "manufacture weight"
      captor.getValue.rows(2).value.content.asHtml.toString() mustEqual "Yes"
      captor.getValue.rows(3).value.content.asHtml.toString() mustEqual "imported weigh"
      captor.getValue.rows(4).value.content.asHtml.toString() mustEqual "total weight"
    }

    "redirect on account page if cannot calculate total plastic" in {
      when(nonExportedAmountHelper.totalPlastic(any)).thenReturn(None)

      val result = sut.onPageLoad.skippingJourneyAction(dataRequest)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.IndexController.onPageLoad.url)
    }
  }

  "onwardRouting" should {
    "invoke the journey action" in {
      when(journeyAction.async(any)) thenReturn mock[Action[AnyContent]]
      Try(await(sut.onwardRouting(dataRequest)))
      verify(journeyAction).async(any)
    }

    "redirect" in {
      setUpMocks(createUserAnswer)

      val result = sut.onwardRouting.skippingJourneyAction(dataRequest)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result) mustEqual Some(Call("GET", "/foo").url)
    }

    "set the user answer" in {
      var saveUserAnswerToCache: Option[UserAnswers] = None
      val ans = createUserAnswer

      setUpMocks(ans)
      when(cacheConnector.saveUserAnswerFunc(any)(any)).thenReturn((a: UserAnswers, b: Boolean) => {
        saveUserAnswerToCache = Some(a)
        Future.successful(true)
      })

      await(sut.onwardRouting.skippingJourneyAction(dataRequest))

      saveUserAnswerToCache mustBe Some(ans)
    }

    "save userAnswer to cache" in {
      setUpMocks(createUserAnswer)

      await(sut.onwardRouting.skippingJourneyAction(dataRequest))

      verify(cacheConnector).saveUserAnswerFunc(ArgumentMatchers.eq("123"))(any)
    }
  }

  private def setUpMocks(userAnswer: UserAnswers): Unit = {
    when(dataRequest.userAnswers).thenReturn(userAnswer)
    when(dataRequest.pptReference).thenReturn("123")
    when(cacheConnector.saveUserAnswerFunc(any)(any)).thenReturn((_, _) => Future.successful(true))
    when(navigator.confirmTotalPlasticPackagingRoute(any)).thenReturn(Call("GET", "/foo"))
  }

  private def createUserAnswer: UserAnswers = {
    UserAnswers("123")
      .set(ManufacturedPlasticPackagingPage, true).get
      .set(ManufacturedPlasticPackagingWeightPage, 10L).get
      .set(ImportedPlasticPackagingPage, true).get
      .set(ImportedPlasticPackagingWeightPage, 1L).get
  }
}
