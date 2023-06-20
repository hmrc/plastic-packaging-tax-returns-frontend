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

import base.SpecBase
import connectors.FinancialsConnector
import models.financials.PPTFinancials
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito.{verify, when}
import org.mockito.MockitoSugar.mock
import play.api.Application
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class MakePaymentControllerSpec extends SpecBase {

  val mockFinancialsConnector: FinancialsConnector = mock[FinancialsConnector]

    "redirect" - {
      "must redirect" - {
        "when financials connector returns a link" in {

          val pptRegAccount = "http://localhost:8505/plastic-packaging-tax/account"

          val app: Application = applicationBuilder()
            .overrides(
              bind[FinancialsConnector].toInstance(mockFinancialsConnector)
            )
            .build()

          running(app) {
            when(mockFinancialsConnector.getPaymentStatement(any())(any())).thenReturn(Future.successful(PPTFinancials(None, None, None)))
            when(mockFinancialsConnector.getPaymentLink(any(), any(), any())(any())).thenReturn(Future.successful("/blah"))

            val request = FakeRequest(GET, routes.MakePaymentController.redirectLink().url)

            val result = route(app, request).value

            redirectLocation(result) mustBe Some("/blah")
            verify(mockFinancialsConnector).getPaymentStatement(refEq("123"))(any())
            verify(mockFinancialsConnector).getPaymentLink(refEq("123"), refEq(0), eqTo(pptRegAccount))(any())
        }
      }
    }
      "bubble exception" - {
        "when getStatement fails" in {
          val app: Application = applicationBuilder()
            .overrides(
              bind[FinancialsConnector].toInstance(mockFinancialsConnector)
            )
            .build()

          running(app) {
            class MagicExc() extends Exception("boom")

            when(mockFinancialsConnector.getPaymentStatement(any())(any())).thenReturn(Future.failed(new MagicExc()))

            val request = FakeRequest(GET, routes.MakePaymentController.redirectLink().url)

            intercept[MagicExc](await(route(app, request).value))
          }
        }
        "getLink fails" in {
          val app: Application = applicationBuilder()
            .overrides(
              bind[FinancialsConnector].toInstance(mockFinancialsConnector)
            )
            .build()

          running(app) {

            class MagicExc() extends Exception("boom")

            when(mockFinancialsConnector.getPaymentStatement(any())(any())).thenReturn(Future.successful(PPTFinancials(None, None, None)))
            when(mockFinancialsConnector.getPaymentLink(any(), any(), any())(any())).thenReturn(Future.failed(new MagicExc()))

            val request = FakeRequest(GET, routes.MakePaymentController.redirectLink().url)

            intercept[MagicExc](await(route(app, request).value))
          }
        }
      }
  }

}
