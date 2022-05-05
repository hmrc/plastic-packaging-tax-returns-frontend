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

package controllers.payments

import base.SpecBase
import config.FrontendAppConfig
import connectors.DirectDebitConnector
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.Mockito.{verify, when}
import org.mockito.MockitoSugar.mock
import play.api.Application
import play.api.http.Status.SEE_OTHER
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, defaultAwaitTimeout, redirectLocation, route, running, status, writeableOf_AnyContentAsEmpty}
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future


class DirectDebitControllerSpec extends SpecBase {

  "DirectDebitController" - {
    "redirectLink" - {
      "redirect to enter email address page" in {

        val mockDirectDebitConnector: DirectDebitConnector = mock[DirectDebitConnector]

        val app: Application = applicationBuilder()
          .overrides(bind[DirectDebitConnector].toInstance(mockDirectDebitConnector))
          .build()


        val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
        running(app) {
          when(mockDirectDebitConnector.getDirectDebitMandate(any())(any()))
            .thenReturn(Future.successful(HttpResponse(200, "something")))

          val request = FakeRequest(GET, routes.DirectDebitController.redirectLink().url)

          val result = route(app, request).value

          redirectLocation(result) mustBe Some(appConfig.directDebitEnterEmailAddressUrl("something"))
        }
      }

      "call the direct debit connector" in {
        val mockDirectDebitConnector: DirectDebitConnector = mock[DirectDebitConnector]

        val app: Application = applicationBuilder()
          .overrides(bind[DirectDebitConnector].toInstance(mockDirectDebitConnector))
          .build()

        running(app) {
          when(mockDirectDebitConnector.getDirectDebitMandate(any())(any()))
            .thenReturn(Future.successful(HttpResponse(200, "something")))

          val request = FakeRequest(GET, routes.DirectDebitController.redirectLink().url)

          val result = route(app, request).value

          status(result) mustBe SEE_OTHER
        verify(mockDirectDebitConnector).getDirectDebitMandate(refEq("123"))(any())
        }
      }
    }
  }
}
