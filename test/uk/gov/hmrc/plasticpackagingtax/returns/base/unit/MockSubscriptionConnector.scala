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

package uk.gov.hmrc.plasticpackagingtax.returns.base.unit

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.plasticpackagingtax.returns.connectors.{
  DownstreamServiceError,
  SubscriptionConnector
}
import uk.gov.hmrc.plasticpackagingtax.returns.models.subscription.subscriptionDisplay.SubscriptionDisplayResponse
import uk.gov.hmrc.plasticpackagingtax.returns.models.subscription.subscriptionUpdate.{
  SubscriptionUpdateRequest,
  SubscriptionUpdateResponse
}

import scala.concurrent.Future

trait MockSubscriptionConnector extends MockitoSugar with BeforeAndAfterEach {
  self: Suite =>

  protected val mockSubscriptionConnector: SubscriptionConnector = mock[SubscriptionConnector]

  def mockGetSubscription(
    dataToReturn: SubscriptionDisplayResponse
  ): OngoingStubbing[Future[SubscriptionDisplayResponse]] =
    when(mockSubscriptionConnector.get(any[String])(any()))
      .thenReturn(Future.successful(dataToReturn))

  def mockGetSubscriptionFailure(): OngoingStubbing[Future[SubscriptionDisplayResponse]] =
    when(mockSubscriptionConnector.get(any[String])(any()))
      .thenThrow(DownstreamServiceError("some error", new Exception("some error")))

  def mockUpdateSubscription(
    dataToReturn: SubscriptionUpdateResponse
  ): OngoingStubbing[Future[SubscriptionUpdateResponse]] =
    when(mockSubscriptionConnector.update(any[String], any[SubscriptionUpdateRequest])(any()))
      .thenReturn(Future.successful(dataToReturn))

  def mockUpdateSubscriptionFailure(): OngoingStubbing[Future[SubscriptionUpdateResponse]] =
    when(mockSubscriptionConnector.update(any[String], any[SubscriptionUpdateRequest])(any()))
      .thenThrow(DownstreamServiceError("some error", new Exception("some error")))

  override protected def afterEach(): Unit = {
    super.afterEach()
    Mockito.reset(mockSubscriptionConnector)
  }

}
