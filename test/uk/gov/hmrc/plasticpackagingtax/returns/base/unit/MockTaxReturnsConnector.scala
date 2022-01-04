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
import org.mockito.Mockito.{verify, when}
import org.mockito.stubbing.OngoingStubbing
import org.mockito.{ArgumentCaptor, Mockito}
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.plasticpackagingtax.returns.builders.TaxReturnBuilder
import uk.gov.hmrc.plasticpackagingtax.returns.connectors.{
  DownstreamServiceError,
  ServiceError,
  TaxReturnsConnector
}
import uk.gov.hmrc.plasticpackagingtax.returns.models.domain.TaxReturn

import scala.concurrent.Future

trait MockTaxReturnsConnector extends MockitoSugar with TaxReturnBuilder with BeforeAndAfterEach {
  self: Suite =>

  protected val mockTaxReturnsConnector: TaxReturnsConnector = mock[TaxReturnsConnector]

  def mockTaxReturnUpdate(
    dataToReturn: TaxReturn
  ): OngoingStubbing[Future[Either[ServiceError, TaxReturn]]] =
    when(mockTaxReturnsConnector.update(any[TaxReturn])(any()))
      .thenReturn(Future.successful(Right(dataToReturn)))

  def mockTaxReturnFind(
    dataToReturn: TaxReturn
  ): OngoingStubbing[Future[Either[ServiceError, Option[TaxReturn]]]] =
    when(mockTaxReturnsConnector.find(any[String])(any()))
      .thenReturn(Future.successful(Right(Some(dataToReturn))))

  def mockTaxReturnException(): OngoingStubbing[Future[Either[ServiceError, TaxReturn]]] =
    when(mockTaxReturnsConnector.update(any[TaxReturn])(any()))
      .thenThrow(new RuntimeException("some error"))

  def mockTaxReturnFailure(): OngoingStubbing[Future[Either[ServiceError, TaxReturn]]] =
    when(mockTaxReturnsConnector.update(any[TaxReturn])(any()))
      .thenReturn(
        Future.successful(Left(DownstreamServiceError("some error", new Exception("some error"))))
      )

  def modifiedTaxReturn: TaxReturn = {
    val captor = ArgumentCaptor.forClass(classOf[TaxReturn])
    verify(mockTaxReturnsConnector).update(captor.capture())(any())
    captor.getValue
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    Mockito.reset(mockTaxReturnsConnector)
  }

}
