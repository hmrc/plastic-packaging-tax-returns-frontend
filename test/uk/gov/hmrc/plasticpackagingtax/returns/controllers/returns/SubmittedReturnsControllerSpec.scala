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

package uk.gov.hmrc.plasticpackagingtax.returns.controllers.returns

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.http.Status.OK
import play.api.test.Helpers.{await, contentAsString, status}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.plasticpackagingtax.returns.base.unit.ControllerSpec
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.returns.submitted_returns_page
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents

class SubmittedReturnsControllerSpec extends ControllerSpec {

  private val mcc = stubMessagesControllerComponents()

  private val mockSubmittedReturnsPage = mock[submitted_returns_page]
  when(mockSubmittedReturnsPage.apply()(any(), any())).thenReturn(
    HtmlFormat.raw("Submitted Returns")
  )

  private val controller = new SubmittedReturnsController(authenticate = mockAuthAction,
                                                          mcc = mcc,
                                                          page = mockSubmittedReturnsPage
  )

  "Submitted Returns Controller" should {
    "display the submitted returns page" when {
      "user is authenticated" in {
        authorizedUser()

        val resp = controller.displayPage()(getRequest())

        status(resp) mustBe OK
        contentAsString(resp) mustBe "Submitted Returns"
      }
    }

    "throw RuntimeException" when {
      "user is unauthenticated" in {
        unAuthorizedUser()

        intercept[RuntimeException](await(controller.displayPage()(getRequest())))
      }
    }
  }
}
