/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.plasticpackagingtax.returns.controllers.subscriptions

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.data.Form
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.libs.json.Json
import play.api.test.Helpers.{redirectLocation, status}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.plasticpackagingtax.returns.base.PptTestData.{
  createSubscriptionDisplayResponse,
  ukLimitedCompanySubscription
}
import uk.gov.hmrc.plasticpackagingtax.returns.base.unit.ControllerSpec
import uk.gov.hmrc.plasticpackagingtax.returns.connectors.DownstreamServiceError
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.subscriptions.{
  routes => subscriptionsRoutes
}
import uk.gov.hmrc.plasticpackagingtax.returns.forms.subscriptions.Name
import uk.gov.hmrc.plasticpackagingtax.returns.models.subscription.subscriptionUpdate.SubscriptionUpdateResponse
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.subscriptions.primary_contact_name_page
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents

import java.time.ZonedDateTime
import java.util.UUID

class PrimaryContactNameControllerSpec extends ControllerSpec {

  private val mcc  = stubMessagesControllerComponents()
  private val page = mock[primary_contact_name_page]

  private val controller = new PrimaryContactNameController(authenticate = mockAuthAction,
                                                            journeyAction = mockJourneyAction,
                                                            mcc = mcc,
                                                            page = page,
                                                            subscriptionConnector =
                                                              mockSubscriptionConnector
  )

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSubscriptionConnector)
    when(page.apply(any[Form[Name]])(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(page)
    super.afterEach()
  }

  "Primary Contact Name controller" should {

    "return 200" when {

      "display page method is invoked" in {
        mockGetSubscription(createSubscriptionDisplayResponse(ukLimitedCompanySubscription))
        authorizedUser()

        val result = controller.displayPage()(getRequest())

        status(result) mustBe OK
      }
    }

    "return 303 " when {
      "user submits a valid name" in {
        authorizedUser()
        mockGetSubscription(createSubscriptionDisplayResponse(ukLimitedCompanySubscription))
        mockUpdateSubscription(
          SubscriptionUpdateResponse(pptReference = UUID.randomUUID().toString,
                                     processingDate = ZonedDateTime.now,
                                     formBundleNumber =
                                       "123456789"
          )
        )

        val result =
          controller.submit()(postRequest(Json.toJson(Name(value = "Jack Russel"))))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(
          subscriptionsRoutes.ViewSubscriptionController.displayPage().url
        )

      }
    }
    "return 400 (BAD_REQUEST)" when {

      "user submits invalid Name" in {
        authorizedUser()
        mockGetSubscription(createSubscriptionDisplayResponse(ukLimitedCompanySubscription))
        mockUpdateSubscription(
          SubscriptionUpdateResponse(pptReference = UUID.randomUUID().toString,
                                     processingDate = ZonedDateTime.now,
                                     formBundleNumber =
                                       "123456789"
          )
        )

        val result =
          controller.submit()(postRequest(Json.toJson(Name(value = "<script> </script>"))))

        status(result) mustBe BAD_REQUEST
      }
    }

    "return an error" when {

      "user submits form and the tax return update fails" in {
        authorizedUser()
        mockGetSubscription(createSubscriptionDisplayResponse(ukLimitedCompanySubscription))
        mockUpdateSubscriptionFailure()

        val result =
          controller.submit()(postRequest(Json.toJson(Name(value = "Jack Russel"))))

        intercept[DownstreamServiceError](status(result))
      }

      "user is not authorised" in {
        unAuthorizedUser()
        mockGetSubscription(createSubscriptionDisplayResponse(ukLimitedCompanySubscription))
        val result = controller.displayPage()(getRequest())

        intercept[RuntimeException](status(result))
      }
    }

  }

}
