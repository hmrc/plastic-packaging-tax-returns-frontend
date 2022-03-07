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

import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.Mockito.when
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.http.Status._
import play.api.mvc.{AnyContentAsFormUrlEncoded, Request, Result}
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.Helpers.{redirectLocation, status}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.plasticpackagingtax.returns.base.unit.ControllerSpec
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.home.{routes => homeRoutes}
import uk.gov.hmrc.plasticpackagingtax.returns.forms.returns.StartDateReturnForm
import uk.gov.hmrc.plasticpackagingtax.returns.forms.returns.StartDateReturnForm.{FieldKey, Yes}
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.returns.start_date_returns_page
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents

import scala.concurrent.Future

class StartDateReturnControllerTest extends ControllerSpec {

  private val mcc  = stubMessagesControllerComponents()
  private val page = mock[start_date_returns_page]

  private val controller =
    new StartDateReturnController(mockAuthAction, mockJourneyAction, mcc = mcc, view = page)

  "displayPage()" should {

    "return 200" in {
      authorizedUser()
      when(page.apply(any(), refEq(defaultObligation))(any(), any())).thenReturn(HtmlFormat.empty)

      val result = controller.displayPage()(getRequest())

      status(result) mustBe OK
    }
  }
  "onSubmit" should {

    "return a 400" when {
      "form fails to bind" in {
        authorizedUser()
        when(page.apply(any(), refEq(defaultObligation))(any(), any())).thenReturn(HtmlFormat.empty)

        val result = controller.submit()(postRequest)

        status(result) mustBe BAD_REQUEST
      }
    }

    "redirects" when {
      "Yes is checked" in {
        authorizedUser()
        when(page.apply(any(), refEq(defaultObligation))(any(), any())).thenReturn(HtmlFormat.empty)
        val req: Request[AnyContentAsFormUrlEncoded] = postRequest
          .withFormUrlEncodedBody(FieldKey -> Yes)
          .withCSRFToken

        val result: Future[Result] = controller.submit()(req)

        //todo: update when redirect pages are created
        redirectLocation(result) mustBe Some(homeRoutes.HomeController.displayPage().url)

      }
      "No is checked" in {
        authorizedUser()
        when(page.apply(any(), refEq(defaultObligation))(any(), any())).thenReturn(HtmlFormat.empty)
        val req: Request[AnyContentAsFormUrlEncoded] = postRequest
          .withFormUrlEncodedBody(FieldKey -> StartDateReturnForm.No)
          .withCSRFToken

        val result: Future[Result] = controller.submit()(req)

        //todo: update when redirect pages are created
        redirectLocation(result) mustBe Some(routes.StartDateReturnController.displayPage().url)
      }
    }

  }
}
