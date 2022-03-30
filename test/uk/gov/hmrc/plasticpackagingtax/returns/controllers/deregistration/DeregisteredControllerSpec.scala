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

package uk.gov.hmrc.plasticpackagingtax.returns.controllers.deregistration

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.test.Helpers.{await, contentAsString, redirectLocation, session, status}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.plasticpackagingtax.returns.base.unit.ControllerSpec
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.deregistration.deregistered_page
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.home.session_timed_out
import uk.gov.hmrc.plasticpackagingtax.returns.views.model.SignOutReason
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents

class DeregisteredControllerSpec extends ControllerSpec {

  private val mcc              = stubMessagesControllerComponents()
  private val deregisteredPage = mock[deregistered_page]
  private val controller       = new DeregisteredController(mockAuthAction, mcc, deregisteredPage)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    when(deregisteredPage.apply()(any(), any())).thenReturn(HtmlFormat.raw("Deregistered"))
  }

  override protected def afterEach(): Unit = {
    reset(deregisteredPage)
    super.afterEach()
  }

  "Deregistered Controller" should {
    "display the deregistered page" in {
      authorizedUser()

      val resp = controller.displayPage()(getRequest())

      status(resp) mustBe OK
      contentAsString(resp) mustBe "Deregistered"
    }

    "throw an exception when user not authenticated" in {
      unAuthorizedUser()

      intercept[RuntimeException] {
        await(controller.displayPage()(getRequest()))
      }
    }
  }
}
