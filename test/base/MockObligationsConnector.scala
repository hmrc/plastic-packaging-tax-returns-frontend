/*
 * Copyright 2025 HM Revenue & Customs
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

package base

import connectors.ObligationsConnector
import models.obligations.PPTObligations
import models.returns.TaxReturnObligation
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.{reset, when}
import org.mockito.stubbing.ScalaOngoingStubbing
import org.scalatest.Suite
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.Future

trait MockObligationsConnector extends MockitoSugar {
  self: Suite =>

  protected val mockObligationsConnector: ObligationsConnector = mock[ObligationsConnector]

  protected def mockGetObligations(
    obligations: PPTObligations
  ): ScalaOngoingStubbing[Future[PPTObligations]] = {
    reset(mockObligationsConnector)
    when(mockObligationsConnector.getOpen(any())(any())).thenReturn(Future.successful(obligations))
  }

  protected def mockGetFulfilledObligations(
    obligations: Seq[TaxReturnObligation]
  ): ScalaOngoingStubbing[Future[Seq[TaxReturnObligation]]] = {
    reset(mockObligationsConnector)
    when(mockObligationsConnector.getFulfilled(any())(any())).thenReturn(Future.successful(obligations))
  }
}
