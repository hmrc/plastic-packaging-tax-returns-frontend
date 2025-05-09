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

package controllers

import base.utils.JourneyActionAnswer
import cacheables.AmendSelectedPeriodKey
import connectors.{CacheConnector, TaxReturnsConnector}
import controllers.actions.JourneyAction
import controllers.amends.ViewReturnSummaryController
import controllers.amends.ViewReturnSummaryController.Unamendable
import controllers.helpers.TaxReturnHelper
import handlers.ErrorHandler
import models.UserAnswers
import models.UserAnswers.SaveUserAnswerFunc
import models.requests.DataRequest
import models.returns._
import org.mockito.Answers
import org.mockito.ArgumentMatchers.{anyString, eq => meq}
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar.atLeastOnce
import org.mockito.MockitoSugar.{reset, verify, verifyZeroInteractions, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.http.Status.{NOT_FOUND, OK, SEE_OTHER}
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{AnyContent, RequestHeader}
import play.api.test.Helpers.{await, defaultAwaitTimeout, redirectLocation, status, stubMessagesControllerComponents}
import play.twirl.api.{Html, HtmlFormat}
import queries.{Gettable, Settable}
import support.AmendExportedData
import uk.gov.hmrc.http.HttpResponse
import util.EdgeOfSystem
import viewmodels.checkAnswers.ViewReturnSummaryViewModel
import views.html.amends.ViewReturnSummaryView

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ViewReturnSummaryControllerSpec
    extends PlaySpec
    with MockitoSugar
    with JourneyActionAnswer
    with AmendExportedData
    with BeforeAndAfterEach {

  private val dataRequest                         = mock[DataRequest[AnyContent]](Answers.RETURNS_DEEP_STUBS)
  private val messages                            = mock[Messages]
  private val messagesApi                         = mock[MessagesApi]
  private val journeyAction                       = mock[JourneyAction]
  private val cacheConnector                      = mock[CacheConnector]
  private val view                                = mock[ViewReturnSummaryView]
  private val taxReturnHelper                     = mock[TaxReturnHelper]
  private val returnsConnector                    = mock[TaxReturnsConnector]
  private val errorHandler                        = mock[ErrorHandler]
  private implicit val edgeOfSystem: EdgeOfSystem = mock[EdgeOfSystem]

  val returnDisplayDetails: ReturnDisplayDetails = ReturnDisplayDetails(1, 1, 1, 1, 1, 1, 1, 1, 1, 1)
  val submittedReturn: SubmittedReturn = SubmittedReturn(
    0.3,
    ReturnDisplayApi("2019-08-28T09:30:47Z", IdDetails("", ""), Some(charge), returnDisplayDetails)
  )

  override val taxReturnOb: TaxReturnObligation =
    TaxReturnObligation(LocalDate.now(), LocalDate.now().plusWeeks(4), LocalDate.now().plusWeeks(8), "00XX")

  private val sut = new ViewReturnSummaryController(
    messagesApi,
    journeyAction,
    cacheConnector,
    stubMessagesControllerComponents(),
    view,
    taxReturnHelper,
    returnsConnector,
    errorHandler
  )

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(journeyAction, view, cacheConnector, dataRequest, errorHandler, edgeOfSystem)

    when(dataRequest.pptReference).thenReturn("123")
    when(view.apply(any, any, any, any)(any, any)).thenReturn(HtmlFormat.empty)
    when(journeyAction.apply(any)).thenAnswer(byConvertingFunctionArgumentsToAction)
    when(journeyAction.async(any)).thenAnswer(byConvertingFunctionArgumentsToFutureAction)
    when(messagesApi.preferred(any[RequestHeader])).thenReturn(messages)
    when(edgeOfSystem.localDateTimeNow).thenReturn(taxReturnOb.dueDate.atStartOfDay())
  }

  "onPageLoad" should {
    "use the journey action" in {
      sut.onPageLoad("anyKey")
      verify(journeyAction).async(any)
    }

    "return ok" in {
      setUpAPiCalls(false, Some(taxReturnOb))

      val result = sut.onPageLoad("22C2")(dataRequest)

      status(result) mustBe OK
    }

    "return the right view" in {
      setUpAPiCalls(false, Some(taxReturnOb))
      when(messages.apply(anyString, any)).thenReturn("any-period")

      await(sut.onPageLoad("22C2")(dataRequest))

      val expected = ViewReturnSummaryViewModel(submittedReturn.displayReturnJson)(messages)

      verify(view).apply(
        meq("any-period"),
        meq(expected),
        meq(Right(controllers.amends.routes.ViewReturnSummaryController.amendReturn("22C2"))),
        meq("£300.00")
      )(any, any)
    }

    "return the right view with an empty calback when DD is in progress" in {
      setUpAPiCalls(true, Some(taxReturnOb))
      when(messages.apply(anyString, any)).thenReturn("any-period")

      await(sut.onPageLoad("22C2")(dataRequest))

      val expected = ViewReturnSummaryViewModel(submittedReturn.displayReturnJson)(messages)

      verify(view).apply(
        meq("any-period"),
        meq(expected),
        meq(Left(Unamendable.DDInProgress)),
        meq("£300.00")
      )(any, any)
    }

    "return the right view with an empty callback when return is too old" in {

      val mockObligation = mock[TaxReturnObligation]
      setUpAPiCalls(false, Some(mockObligation))
      when(messages.apply(anyString, any)).thenReturn("any-period")
      when(mockObligation.tooOldToAmend).thenReturn(true)
      when(mockObligation.fromDate).thenReturn(LocalDate.now())
      when(mockObligation.toDate).thenReturn(LocalDate.now())

      await(sut.onPageLoad("22C2")(dataRequest))

      val expected = ViewReturnSummaryViewModel(submittedReturn.displayReturnJson)(messages)

      verify(view).apply(
        meq("any-period"),
        meq(expected),
        meq(Left(Unamendable.TooOld)),
        meq("£300.00")
      )(any, any)
    }

    "return 404 not found" when {
      "the period key is mistyped" in {
        when(errorHandler.notFoundTemplate(any)).thenReturn(Html("error-handler"))

        val result = sut.onPageLoad("222Ca")(dataRequest)

        status(result) mustBe NOT_FOUND
        verify(errorHandler).notFoundTemplate(dataRequest.request)
      }
    }

    "the user doesn't have a fulfilled obligation for the requested period" in {
      setUpAPiCalls(false, None)
      when(messages.apply(anyString, any)).thenReturn("any-period")
      when(errorHandler.notFoundTemplate(any)).thenReturn(Html("error-handler"))

      val result = sut.onPageLoad("22C2")(dataRequest)

      status(result) mustBe NOT_FOUND
      verify(errorHandler).notFoundTemplate(any)
    }
  }

  "amendReturn" should {
    "use the journey action" in {
      sut.amendReturn("anyKey")
      verify(journeyAction).async(any)
    }

    "redirect to CYA page" in {
      setUpAPiCalls()
      val userAnswer = mock[UserAnswers]

      when(cacheConnector.saveUserAnswerFunc(any)(any)).thenReturn(mock[SaveUserAnswerFunc])
      when(dataRequest.userAnswers.removeAll()).thenReturn(userAnswer)
      when(userAnswer.setOrFail(any[Settable[Long]], any, any)(any)).thenReturn(userAnswer)
      when(userAnswer.save(any)(any)).thenReturn(Future.successful(userAnswer))

      val result = sut.amendReturn("22C3").skippingJourneyAction(dataRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustEqual controllers.amends.routes.CheckYourAnswersController.onPageLoad().url
    }
    "re-initialise cache if is a new period key" in {
      setUpAPiCalls()
      val userAnswer   = mock[UserAnswers]
      val saveFunction = mock[SaveUserAnswerFunc]
      when(cacheConnector.saveUserAnswerFunc(any)(any)).thenReturn(saveFunction)
      when(dataRequest.userAnswers.removeAll()).thenReturn(userAnswer)
      when(userAnswer.setOrFail(any[Settable[Long]], any, any)(any)).thenReturn(userAnswer)
      when(userAnswer.save(any)(any)).thenReturn(Future.successful(userAnswer))

      await(sut.amendReturn("22C3").skippingJourneyAction(dataRequest))

      verify(userAnswer).save(meq(saveFunction))(any)
      verify(userAnswer, atLeastOnce).setOrFail(AmendSelectedPeriodKey, "22C3")
      verify(cacheConnector).saveUserAnswerFunc(meq("123"))(any)
    }

    "not re-initialise cache if period key is found" in {
      setUpAPiCalls()
      when(dataRequest.userAnswers.get(any[Gettable[String]])(any)).thenReturn(Some("22C3"))
      when(cacheConnector.saveUserAnswerFunc(any)(any)).thenReturn(mock[SaveUserAnswerFunc])

      await(sut.amendReturn("22C3").skippingJourneyAction(dataRequest))

      verifyZeroInteractions(dataRequest.userAnswers.removeAll())
      verifyZeroInteractions(cacheConnector.saveUserAnswerFunc(any)(any))
    }

    "return 404 when cannot fetch data" in {
      setUpAPiCalls(obligation = None)
      when(errorHandler.notFoundTemplate(any)).thenReturn(Html("error-handler"))

      val result = sut.amendReturn("22C2").skippingJourneyAction(dataRequest)

      status(result) mustEqual NOT_FOUND
    }

    "throw if Direct debit is in progress" in {
      setUpAPiCalls(isDDInProgress = true)

      intercept[Exception] {
        await(sut.amendReturn("22C2").skippingJourneyAction(dataRequest))
      }
    }
  }

  private def setUpAPiCalls(
    isDDInProgress: Boolean = false,
    obligation: Option[TaxReturnObligation] = Some(taxReturnOb)
  ): Unit = {
    when(taxReturnHelper.fetchTaxReturn(any, any)(any)).thenReturn(Future.successful(submittedReturn))
    when(taxReturnHelper.getObligation(any, any)(any)).thenReturn(Future.successful(obligation))
    when(returnsConnector.ddInProgress(any, any)(any)).thenReturn(Future.successful(DDInProgressApi(isDDInProgress)))
    when(cacheConnector.set(any, any)(any)).thenReturn(Future.successful(mock[HttpResponse]))
  }
}
