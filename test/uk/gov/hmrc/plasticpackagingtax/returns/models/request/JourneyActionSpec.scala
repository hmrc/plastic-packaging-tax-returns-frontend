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

package uk.gov.hmrc.plasticpackagingtax.returns.models.request

import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.BDDMockito.`given`
import org.mockito.Mockito.{reset, verify}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.{AnyContentAsEmpty, Headers, Result, Results}
import play.api.test.Helpers.await
import play.api.test.{DefaultAwaitTimeout, FakeRequest}
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.plasticpackagingtax.returns.connectors.{
  DownstreamServiceError,
  TaxReturnsConnector
}
import uk.gov.hmrc.plasticpackagingtax.returns.models.domain.TaxReturn
import uk.gov.hmrc.plasticpackagingtax.returns.models.request.JourneyAction.tempTaxReturnId

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class JourneyActionSpec
    extends AnyWordSpec with Matchers with MockitoSugar with DefaultAwaitTimeout
    with BeforeAndAfterEach {

  private val mockTaxReturnsConnector = mock[TaxReturnsConnector]
  private val responseGenerator       = mock[JourneyRequest[_] => Future[Result]]
  private val actionRefiner           = new JourneyAction(mockTaxReturnsConnector)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockTaxReturnsConnector, responseGenerator)
    given(responseGenerator.apply(any())).willReturn(Future.successful(Results.Ok))
  }

  private def request(headers: Headers = Headers()): JourneyRequest[AnyContentAsEmpty.type] =
    new JourneyRequest(FakeRequest().withHeaders(headers), TaxReturn(tempTaxReturnId))

  "action refine" should {

    "create tax return" when {
      "tax return details not found" in {
        given(mockTaxReturnsConnector.find(refEq(tempTaxReturnId))(any[HeaderCarrier])).willReturn(
          Future.successful(Right(None))
        )
        given(
          mockTaxReturnsConnector.create(refEq(TaxReturn(tempTaxReturnId)))(any[HeaderCarrier])
        ).willReturn(Future.successful(Right(TaxReturn(tempTaxReturnId))))

        await(actionRefiner.invokeBlock(request(), responseGenerator)) mustBe Results.Ok
      }
    }

    "load tax return" when {
      "tax return exists" in {
        given(mockTaxReturnsConnector.find(refEq(tempTaxReturnId))(any[HeaderCarrier])).willReturn(
          Future.successful(Right(Option(TaxReturn(tempTaxReturnId))))
        )

        await(actionRefiner.invokeBlock(request(), responseGenerator)) mustBe Results.Ok
      }
    }
  }

  def getHeaders: HeaderCarrier = {
    val captor = ArgumentCaptor.forClass(classOf[HeaderCarrier])
    verify(mockTaxReturnsConnector).find(refEq(tempTaxReturnId))(captor.capture())
    captor.getValue
  }

  "throw exception" when {
    "cannot load user tax return" in {
      given(mockTaxReturnsConnector.find(refEq(tempTaxReturnId))(any[HeaderCarrier])).willReturn(
        Future.successful(
          Left(DownstreamServiceError("error", new InternalServerException("error")))
        )
      )

      intercept[DownstreamServiceError] {
        await(actionRefiner.invokeBlock(request(), responseGenerator))
      }
    }
  }
}
