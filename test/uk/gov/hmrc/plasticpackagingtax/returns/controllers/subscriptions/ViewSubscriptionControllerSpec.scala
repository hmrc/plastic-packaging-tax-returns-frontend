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

package uk.gov.hmrc.plasticpackagingtax.returns.controllers.subscriptions

import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.Inspectors.forAll
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.libs.json.JsObject
import play.api.test.Helpers.{await, redirectLocation, status}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.plasticpackagingtax.returns.base.PptTestData
import uk.gov.hmrc.plasticpackagingtax.returns.base.PptTestData.{
  createSubscriptionDisplayResponse,
  pptEnrolment,
  soleTraderSubscription,
  ukLimitedCompanySubscription
}
import uk.gov.hmrc.plasticpackagingtax.returns.base.unit.ControllerSpec
import uk.gov.hmrc.plasticpackagingtax.returns.connectors.DownstreamServiceError
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.home.{routes => homeRoutes}
import uk.gov.hmrc.plasticpackagingtax.returns.models.subscription.subscriptionDisplay.SubscriptionDisplayResponse
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.subscriptions.view_subscription_page
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents

class ViewSubscriptionControllerSpec extends ControllerSpec {

  private val mcc  = stubMessagesControllerComponents()
  private val page = mock[view_subscription_page]

  private val controller = new ViewSubscriptionController(authenticate = mockAuthAction,
                                                          journeyAction = mockJourneyAction,
                                                          mcc = mcc,
                                                          page = page,
                                                          subscriptionConnector =
                                                            mockSubscriptionConnector
  )

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    when(page.apply(any[SubscriptionDisplayResponse])(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(page)
    super.afterEach()
  }

  "ViewSubscriptionController" should {

    "return 200" when {

      "uk company subscription exists and display page method is invoked" in {
        authorizedUser()
        val subscription = createSubscriptionDisplayResponse(ukLimitedCompanySubscription)
        mockGetSubscription(subscription)

        val result = controller.displayPage()(
          authRequest(user =
            PptTestData.newUser("123", Some(pptEnrolment("XMPPT123456789")))
          )
        )

        status(result) mustBe OK
      }

      "sole trader subscription and display page method is invoked" in {
        authorizedUser()
        val subscription = createSubscriptionDisplayResponse(soleTraderSubscription)
        mockGetSubscription(subscription)

        val result = controller.displayPage()(
          authRequest(user =
            PptTestData.newUser("123", Some(pptEnrolment("XMPPT123456789")))
          )
        )

        status(result) mustBe OK
      }
    }

    forAll(Seq(saveAndContinueFormAction)) { formAction =>
      "return 303 (OK) for " + formAction._1 when {
        "user submits the details" in {
          authorizedUser()
          mockTaxReturnFind(aTaxReturn())
          mockTaxReturnUpdate(aTaxReturn())

          val result =
            controller.submit()(postRequestEncoded(JsObject.empty, formAction))

          status(result) mustBe SEE_OTHER
          formAction match {
            case _ =>
              redirectLocation(result) mustBe Some(homeRoutes.HomeController.displayPage().url)
          }
          reset(mockSubscriptionConnector)
        }
      }
    }

    "return populated page with subscription" when {

      def pageData: SubscriptionDisplayResponse = {
        val captor = ArgumentCaptor.forClass(classOf[SubscriptionDisplayResponse])
        verify(page).apply(captor.capture())(any(), any())
        captor.getValue
      }

      "subscription exist and display method is invoked" in {

        authorizedUser()
        val subscription = createSubscriptionDisplayResponse(ukLimitedCompanySubscription)
        mockGetSubscription(subscription)

        await(
          controller.displayPage()(
            authRequest(user =
              PptTestData.newUser("123", Some(pptEnrolment("XMPPT123456789")))
            )
          )
        )

        pageData mustBe subscription

      }
    }

    "prevent access to page" when {

      "user doesn't have correct enrolment" in {
        authorizedUser(PptTestData.newUser("123", None))

        val result = controller.displayPage()(authRequest(user = PptTestData.newUser("123", None)))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(homeRoutes.UnauthorisedController.notEnrolled().url)
      }
    }

    "get subscription throws error" in {
      authorizedUser()
      mockGetSubscriptionFailure()

      intercept[DownstreamServiceError] {
        await(
          controller.displayPage()(
            authRequest(user = PptTestData.newUser("123", Some(pptEnrolment("123"))))
          )
        )
      }
    }

    "user is not authorised" in {
      unAuthorizedUser()
      val result = controller.displayPage()(getRequest())

      intercept[RuntimeException](status(result))
    }
  }

}
