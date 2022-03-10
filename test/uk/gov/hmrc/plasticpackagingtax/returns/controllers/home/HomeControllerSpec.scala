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
import play.api.test.Helpers.status
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.plasticpackagingtax.returns.base.PptTestData.{
  createSubscriptionDisplayResponse,
  ukLimitedCompanySubscription
}
import uk.gov.hmrc.plasticpackagingtax.returns.base.unit.ControllerSpec
import uk.gov.hmrc.plasticpackagingtax.returns.models.subscription.subscriptionDisplay.SubscriptionDisplayResponse
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.home.home_page
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents

class HomeControllerSpec extends ControllerSpec {

  private val mcc  = stubMessagesControllerComponents()
  private val page = mock[home_page]

  private val controller = new HomeController(authenticate = mockAuthAction,
                                              journeyAction = mockJourneyAction,
                                              subscriptionConnector =
                                                mockSubscriptionConnector,
                                              appConfig = config,
                                              mcc = mcc,
                                              page = page
  )

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    when(page.apply(any[SubscriptionDisplayResponse], any())(any(), any())).thenReturn(
      HtmlFormat.empty
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

    "return an error" when {

      "user is not authorised" in {
        unAuthorizedUser()
        val result = controller.displayPage()(getRequest())

        intercept[RuntimeException](status(result))
      }
    }
  }
}
