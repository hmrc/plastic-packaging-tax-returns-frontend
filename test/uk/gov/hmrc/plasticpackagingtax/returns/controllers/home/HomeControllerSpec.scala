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

package uk.gov.hmrc.plasticpackagingtax.returns.controllers.home

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.http.Status.OK
import play.api.test.Helpers.{redirectLocation, status}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.http.{Upstream5xxResponse, UpstreamErrorResponse}
import uk.gov.hmrc.plasticpackagingtax.returns.base.PptTestData.{
  createSubscriptionDisplayResponse,
  ukLimitedCompanySubscription
}
import uk.gov.hmrc.plasticpackagingtax.returns.base.unit.ControllerSpec
import uk.gov.hmrc.plasticpackagingtax.returns.connectors.FinancialsConnector
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.deregistration.{
  routes => deregistrationRoutes
}
import uk.gov.hmrc.plasticpackagingtax.returns.models.financials.PPTFinancials
import uk.gov.hmrc.plasticpackagingtax.returns.models.subscription.subscriptionDisplay.SubscriptionDisplayResponse
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.home.home_page
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents

import scala.concurrent.Future

class HomeControllerSpec extends ControllerSpec {

  private val mcc  = stubMessagesControllerComponents()
  private val page = mock[home_page]

  private val mockFinancialsConnector = mock[FinancialsConnector]

  private val controller = new HomeController(authenticate = mockAuthAction,
                                              subscriptionConnector = mockSubscriptionConnector,
                                              financialsConnector = mockFinancialsConnector,
                                              obligationsConnector = mockObligationsConnector,
                                              appConfig = config,
                                              mcc = mcc,
                                              homePage = page
  )

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    when(
      page.apply(any[SubscriptionDisplayResponse], any(), any(), any(), any())(any(), any())
    ).thenReturn(HtmlFormat.empty)
    when(mockFinancialsConnector.getPaymentStatement(any[String])(any())).thenReturn(
      Future.successful(PPTFinancials(None, None, None))
    )
  }

  override protected def afterEach(): Unit = {
    reset(page)
    super.afterEach()
  }

  "HomePage Controller" should {

    "return 200" when {

      "use is authorised and display page method is invoked" in {
        authorizedUser()

        val subscription = createSubscriptionDisplayResponse(ukLimitedCompanySubscription)
        mockGetSubscription(subscription)

        val result = controller.displayPage()(getRequest())

        status(result) mustBe OK
      }
    }

    "redirect to the deregistered page" when {
      "get subscription returns a 404 (NOT_FOUND)" in {
        authorizedUser()
        mockGetSubscriptionFailure(UpstreamErrorResponse("Subscription not found", 404))

        val result = controller.displayPage()(getRequest())

        redirectLocation(result) mustBe Some(
          deregistrationRoutes.DeregisteredController.displayPage().url
        )
      }
    }

    "return an error" when {

      "user is not authorised" in {
        unAuthorizedUser()
        val result = controller.displayPage()(getRequest())

        intercept[RuntimeException](status(result))
      }

      "get subscription returns a failure other than 404 (NOT_FOUND)" in {
        authorizedUser()
        mockGetSubscriptionFailure(UpstreamErrorResponse("BANG!", 500))

        val result = controller.displayPage()(getRequest())

        intercept[UpstreamErrorResponse](status(result))
      }

    }
  }
}
