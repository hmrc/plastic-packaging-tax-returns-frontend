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

package controllers

import base.{MockObligationsConnector, SpecBase}
import connectors.{CacheConnector, ObligationsConnector, TaxReturnsConnector}
import handlers.ErrorHandler
import models.returns.{DDInProgressApi, IdDetails, ReturnDisplayApi, ReturnDisplayDetails, TaxReturnObligation}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.inject
import play.api.mvc.{Call, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.checkAnswers.ViewReturnSummaryViewModel
import views.html.amends.ViewReturnSummaryView

import java.time.LocalDate
import scala.concurrent.Future

class ViewReturnSummaryControllerSpec extends SpecBase with MockitoSugar with MockObligationsConnector {

  val mockMessages: Messages = mock[Messages]
  when(mockMessages.apply(anyString(), any())).thenReturn("August")

  private val mockConnector = mock[TaxReturnsConnector]

  val returnDisplayDetails = ReturnDisplayDetails(1, 1, 1, 1, 1, 1, 1, 1, 1, 1)
  val submittedReturn = ReturnDisplayApi("2019-08-28T09:30:47Z", IdDetails("", ""), Some(charge), returnDisplayDetails)

  "onPageLoad" - {
    "must return OK and the correct view" in {

      val ob: TaxReturnObligation = TaxReturnObligation(
        LocalDate.parse("2022-04-01"),
        LocalDate.parse("2022-06-30"),
        LocalDate.parse("2022-06-30").plusWeeks(8),
        "22C4")

      mockGetFulfilledObligations(Seq(ob))

      when(mockConnector.get(any(), any())(any())).thenReturn(
        Future.successful(submittedReturn)
      )

      when(mockConnector.ddInProgress(any(), any())(any())).thenReturn(Future.successful(DDInProgressApi(false)))

      when(cacheConnector.set(any(), any())(any())).thenReturn(Future.successful(mockResponse))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          inject.bind[TaxReturnsConnector].toInstance(mockConnector),
          inject.bind[ObligationsConnector].toInstance(mockObligationsConnector),
          inject.bind[CacheConnector].toInstance(cacheConnector)
        ).build()

      running(application) {

        val viewModel = ViewReturnSummaryViewModel(submittedReturn)(mockMessages)

        val request = FakeRequest(controllers.amends.routes.ViewReturnSummaryController.onPageLoad("22C4"))

        val result = route(application, request).value

        val view = application.injector.instanceOf[ViewReturnSummaryView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view("April to June 2022", viewModel, Some(Call("", "/plastic-packaging-tax/viewReturnSummary/22C4/amend")))(
          request,
          messages(application)
        ).toString

        verify(mockConnector).get(any(), ArgumentMatchers.eq("22C4"))(any())
      }
    }

    "must return 404 not found" - {
      "when the period key is mistyped" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .build()

        running(application) {
          val request = FakeRequest(controllers.amends.routes.ViewReturnSummaryController.onPageLoad("MALFORMED"))

          val result: Future[Result] = (route(application, request).value)

          val errorHandler = application.injector.instanceOf[ErrorHandler]
          val errorView = errorHandler.notFoundTemplate(request).toString()

          status(result) mustEqual NOT_FOUND
          contentAsString(result) mustEqual errorView
        }
      }
      "when the user doesn't have a fulfilled obligation for the requested period" in {
        val ob: TaxReturnObligation = TaxReturnObligation(
          LocalDate.parse("2022-04-01"),
          LocalDate.parse("2022-06-30"),
          LocalDate.parse("2022-06-30").plusWeeks(8),
          "21C1")

        mockGetFulfilledObligations(Seq(ob))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            inject.bind[TaxReturnsConnector].toInstance(mockConnector),
            inject.bind[ObligationsConnector].toInstance(mockObligationsConnector),
            inject.bind[CacheConnector].toInstance(cacheConnector)
          ).build()

        when(mockConnector.get(any(), any())(any())).thenReturn(
          Future.successful(submittedReturn)
        )

        when(mockConnector.ddInProgress(any(), any())(any())).thenReturn(Future.successful(DDInProgressApi(false)))

        when(cacheConnector.set(any(), any())(any())).thenReturn(Future.successful(mockResponse))

        running(application) {
          val request = FakeRequest(controllers.amends.routes.ViewReturnSummaryController.onPageLoad("90C4"))

          val result: Future[Result] = (route(application, request).value)

          val errorHandler = application.injector.instanceOf[ErrorHandler]
          val errorView = errorHandler.notFoundTemplate(request).toString()

          status(result) mustEqual NOT_FOUND
          contentAsString(result) mustEqual errorView
        }
      }
      "when the user doesn't have any fulfilled obligations" in {
        mockGetFulfilledObligations(Seq.empty)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            inject.bind[TaxReturnsConnector].toInstance(mockConnector),
            inject.bind[ObligationsConnector].toInstance(mockObligationsConnector),
            inject.bind[CacheConnector].toInstance(cacheConnector)
          ).build()

        when(mockConnector.get(any(), any())(any())).thenReturn(
          Future.successful(submittedReturn)
        )

        when(mockConnector.ddInProgress(any(), any())(any())).thenReturn(Future.successful(DDInProgressApi(false)))

        when(cacheConnector.set(any(), any())(any())).thenReturn(Future.successful(mockResponse))

        running(application) {
          val request = FakeRequest(controllers.amends.routes.ViewReturnSummaryController.onPageLoad("21C1"))

          val result: Future[Result] = (route(application, request).value)

          val errorHandler = application.injector.instanceOf[ErrorHandler]
          val errorView = errorHandler.notFoundTemplate(request).toString()

          status(result) mustEqual NOT_FOUND
          contentAsString(result) mustEqual errorView
        }

      }
    }
  }
}
