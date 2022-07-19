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

package controllers

import base.{MockObligationsConnector, MockSubscriptionConnector, SpecBase}
import config.{Features, FrontendAppConfig}
import connectors.{CacheConnector, FinancialsConnector, ObligationsConnector, SubscriptionConnector}
import models.{EisError, EisFailure}
import models.financials.PPTFinancials
import models.obligations.PPTObligations
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito.{atLeastOnce, reset, verify, verifyNoInteractions, when}
import play.api.inject.bind
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import support.PptTestData.{createSubscriptionDisplayResponse, ukLimitedCompanySubscription}
import uk.gov.hmrc.auth.core.InsufficientEnrolments
import views.html.IndexView

import scala.concurrent.Future

class IndexControllerSpec
    extends SpecBase with MockSubscriptionConnector with MockObligationsConnector {

  private val mockFinancialsConnector = mock[FinancialsConnector]
  private val page                    = mock[IndexView]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(page, mockFinancialsConnector)

    // Empty html from page.apply
    when(page.apply(any(), any(), any(), any(), any(), any())(any(), any())).thenReturn(Html.apply(""))

  }


  "Index Controller" - {

    "return 200" - {

      "when user is authorised and display page method is invoked" in {

        val subscription = createSubscriptionDisplayResponse(ukLimitedCompanySubscription)
        mockGetSubscription(subscription)

        val application = applicationBuilder(userAnswers = None).overrides(
          bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
        ).build()

        running(application) {
          val request = FakeRequest(GET, routes.IndexController.onPageLoad.url)

          val result = route(application, request).value

          status(result) mustEqual OK

        }
      }
    }

    "redirect to the de-registered page" - {

      "when get subscription returns a 404 (NOT_FOUND) and EisFailure body confirming this" in {

        mockGetSubscriptionFailure(
          EisFailure(
            Some(
              Seq(
                EisError(
                  "NO_DATA_FOUND",
                  "The remote endpoint has indicated that the requested resource could not be found."
                )
              )
            )
          )
        )

        val application = applicationBuilder(userAnswers = None).overrides(
          bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
        ).build()

        running(application) {
          val request = FakeRequest(GET, routes.IndexController.onPageLoad.url)

          val result = route(application, request).value

          redirectLocation(result) mustBe Some(routes.DeregisteredController.onPageLoad().url)

        }
      }
    }

    "avoid calling Obligation Api" - {

      "when return is not enabled" in {

        setUpMocks()
        when(config.isFeatureEnabled(eqTo(Features.returnsEnabled))).thenReturn(false)

        val application = applicationBuilder(userAnswers = None).overrides(
          bind[FrontendAppConfig].toInstance(config),
          bind[CacheConnector].toInstance(cacheConnector),
          bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
          bind[FinancialsConnector].toInstance(mockFinancialsConnector),
          bind[ObligationsConnector].toInstance(mockObligationsConnector),
          bind[IndexView].toInstance(page)
        ).build()

        val futureResult: Future[Result] = running(application) {
          val request = FakeRequest(GET, routes.IndexController.onPageLoad.url)
          route(application, request).value
        }

        await(futureResult)
        verifyNoInteractions(mockObligationsConnector)
        verifyResults(PPTObligations(None, None, 0, false, false))

      }
    }

    "calls Obligation Api" - {

      "when return is enabled" in {

        val expectedObligation = PPTObligations(None, None, 1, true, true)
        setUpMocks(expectedObligation)

        val application = applicationBuilder(userAnswers = None).overrides(
          bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
          bind[FinancialsConnector].toInstance(mockFinancialsConnector),
          bind[ObligationsConnector].toInstance(mockObligationsConnector),
          bind[IndexView].toInstance(page)
        ).build()

        val futureResult: Future[Result] = running(application) {
          val request = FakeRequest(GET, routes.IndexController.onPageLoad.url)
          route(application, request).value
        }

        await(futureResult)
        verify(mockObligationsConnector).getOpen(any[String])(any())
        verifyResults(expectedObligation)
      }
    }

    "avoid calling Financials Api" - {

      "when payments are not enabled" in {

        setUpMocks()
        when(config.isFeatureEnabled(eqTo(Features.paymentsEnabled))).thenReturn(false)

        val application = applicationBuilder(userAnswers = None).overrides(
          bind[FrontendAppConfig].toInstance(config),
          bind[CacheConnector].toInstance(cacheConnector),
          bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
          bind[FinancialsConnector].toInstance(mockFinancialsConnector),
          bind[ObligationsConnector].toInstance(mockObligationsConnector),
          bind[IndexView].toInstance(page)
        ).build()

        val futureResult: Future[Result] = running(application) {
          val request = FakeRequest(GET, routes.IndexController.onPageLoad.url)
          route(application, request).value
        }

        await(futureResult)
        verifyNoInteractions(mockFinancialsConnector)
        val captor: ArgumentCaptor[Option[String]] =
          ArgumentCaptor.forClass(classOf[Option[String]])
        verify(page, atLeastOnce()).apply(any(), any(), any(), captor.capture(), any(), any())(
          any(),
          any()
        )

        captor.getValue.get mustBe "You have no payments due."
      }
    }

    "calls Financials Api" - {

      "when payments are enabled" in {

        setUpMocks()

        val application = applicationBuilder(userAnswers = None).overrides(
          bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
          bind[FinancialsConnector].toInstance(mockFinancialsConnector),
          bind[ObligationsConnector].toInstance(mockObligationsConnector),
          bind[IndexView].toInstance(page)
        ).build()

        val futureResult: Future[Result] = running(application) {
          val request = FakeRequest(GET, routes.IndexController.onPageLoad.url)
          route(application, request).value
        }

        await(futureResult)
        verify(mockFinancialsConnector).getPaymentStatement(any[String])(any())
      }
    }

    "raise an error" - {

      "when not authorised" in {

        val application = applicationBuilderFailedAuth(userAnswers = None).build()

        running(application) {

          val request = FakeRequest(GET, routes.IndexController.onPageLoad.url)
          val result  = route(application, request).value

          intercept[InsufficientEnrolments](status(result))

        }

      }

      "get subscription returns a 404 (NOT_FOUND) but no confirming EisFailure in the body" in {

        mockGetSubscriptionFailure(
          EisFailure(Some(Seq(EisError("INTERNAL_SERVER_ERROR", "Something's gone BANG!"))))
        )

        val application = applicationBuilderFailedAuth(userAnswers = None).build()

        running(application) {

          val request = FakeRequest(GET, routes.IndexController.onPageLoad.url)
          val result  = route(application, request).value

          intercept[RuntimeException](status(result))

        }

      }

      "get subscription returns a failure other than 404 (NOT_FOUND)" in {

        mockGetSubscriptionFailure(
          EisFailure(None)
        )

        val application = applicationBuilderFailedAuth(userAnswers = None).build()

        running(application) {

          val request = FakeRequest(GET, routes.IndexController.onPageLoad.url)
          val result  = route(application, request).value

          intercept[RuntimeException](status(result))

        }
      }
    }
  }


  private def setUpMocks(obligation: PPTObligations = createDefaultPPTObligation) = {
    reset(mockFinancialsConnector)
    reset(mockSubscriptionConnector)
    reset(mockObligationsConnector)

    val subscription = createSubscriptionDisplayResponse(ukLimitedCompanySubscription)
    mockGetSubscription(subscription)

    when(mockFinancialsConnector.getPaymentStatement(any[String])(any())).thenReturn(
      Future.successful(PPTFinancials(None, None, None))
    )
    when(mockObligationsConnector.getOpen(any[String])(any())).thenReturn(Future.successful(obligation))
  }

  private def verifyResults(obligation: PPTObligations) = {
    val captor: ArgumentCaptor[Option[PPTObligations]] =
      ArgumentCaptor.forClass(classOf[Option[PPTObligations]])
    verify(page, atLeastOnce()).apply(any(), any(), captor.capture(), any(), any(), any())(any(),
                                                                                           any()
    )

    captor.getValue.get mustBe obligation
  }

  private def createDefaultPPTObligation: PPTObligations =
    PPTObligations(None, None, 1, true, true)

}
