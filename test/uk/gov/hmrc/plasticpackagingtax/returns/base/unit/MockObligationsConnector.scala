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

package uk.gov.hmrc.plasticpackagingtax.returns.base.unit

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.Suite
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.plasticpackagingtax.returns.connectors.ObligationsConnector
import uk.gov.hmrc.plasticpackagingtax.returns.models.obligations.PPTObligations

import scala.concurrent.Future

trait MockObligationsConnector extends MockitoSugar {
  self: Suite =>

  protected val mockObligationsConnector: ObligationsConnector = mock[ObligationsConnector]

  protected def mockGetObligations(
    obligations: PPTObligations
  ): OngoingStubbing[Future[PPTObligations]] =
    when(mockObligationsConnector.get(any())(any())).thenReturn(Future.successful(obligations))

}
