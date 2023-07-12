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

package controllers.helpers

import connectors.{ObligationsConnector, TaxReturnsConnector}
import models.obligations.PPTObligations
import models.returns.{SubmittedReturn, TaxReturnObligation}
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.MockitoSugar.verifyNoMoreInteractions
import org.mockito.MockitoSugar.{mock, reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class TaxReturnHelperSpec extends PlaySpec with BeforeAndAfterEach {

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockObligationsConnector, mockReturnsConnector)
  }

  val hc = HeaderCarrier()

  val mockReturnsConnector = mock[TaxReturnsConnector]
  val mockObligationsConnector = mock[ObligationsConnector]

  val sut = new TaxReturnHelper(mockReturnsConnector, mockObligationsConnector)(global)

  def obligation(periodKey: String) = {
    val now = LocalDate.now()
    TaxReturnObligation(now, now, now, periodKey)
  }

  "fetchTaxReturn" must {
    "proxy to returns connector" in {
      val submittedReturn = mock[SubmittedReturn]
      when(mockReturnsConnector.get(any, any)(any)).thenReturn(Future.successful(submittedReturn))
      val result = await(sut.fetchTaxReturn("userId", "periodKey")(hc))

      result mustBe submittedReturn
      verify(mockReturnsConnector).get("userId", "periodKey")(hc)
    }
    "bubble errors" in {
      object TestException extends Exception("boom!")

      when(mockReturnsConnector.get(any, any)(any)).thenReturn(Future.failed(TestException))

      intercept[TestException.type](await(sut.fetchTaxReturn("userId", "periodKey")(hc)))
      verify(mockReturnsConnector).get("userId", "periodKey")(hc)
    }
  }

  "getObligation" must {
    "get the obligation when it is the only one" in {
      when(mockObligationsConnector.getFulfilled(any)(any)).thenReturn(Future.successful(Seq(obligation("ONLY"))))

      val result = await(sut.getObligation("some-ppt-ref", "ONLY")(hc))

      result mustBe Some(obligation("ONLY"))

      verify(mockObligationsConnector).getFulfilled("some-ppt-ref")(hc)
    }
    "get the obligation when it is one of many" in {
      when(mockObligationsConnector.getFulfilled(any)(any))
        .thenReturn(Future.successful(Seq(obligation("ONE_I_WANT"), obligation("ANOTHER_ONE"))))

      val result = await(sut.getObligation("some-ppt-ref", "ONE_I_WANT")(hc))

      result mustBe Some(obligation("ONE_I_WANT"))
    }
    "return None when there is no obligations" in {
      when(mockObligationsConnector.getFulfilled(any)(any))
        .thenReturn(Future.successful(Seq.empty))

      val result = await(sut.getObligation("some-ppt-ref", "NON_EXISTENT")(hc))

      result mustBe None
    }
    "return None when there is no obligation for that period key" in {
      when(mockObligationsConnector.getFulfilled(any)(any))
        .thenReturn(Future.successful(Seq(obligation("NOT_THE_ONE_YOU_WANT"))))

      val result = await(sut.getObligation("some-ppt-ref", "NON_EXISTENT")(hc))

      result mustBe None
    }
    "throw error when there is multiple obligations for a single period key" in {
      when(mockObligationsConnector.getFulfilled(any)(any))
        .thenReturn(Future.successful(Seq(obligation("DUPLICATE"), obligation("DUPLICATE"))))

      val ex = intercept[IllegalStateException](await(sut.getObligation("some-ppt-ref", "DUPLICATE")(hc)))
      ex.getMessage mustBe "Expected one obligation for 'DUPLICATE', got 2"
    }
  }

  "nextOpenObligationAndIfFirst" must {
    "return None" when {
      "there is no next obligation to return" in {
        when(mockObligationsConnector.getOpen(any)(any)).thenReturn(Future.successful(PPTObligations(None, None, 0, false, false)))

        val result = await(sut.nextOpenObligationAndIfFirst("some-ppt-id")(hc))

        result mustBe None
        verify(mockObligationsConnector).getOpen("some-ppt-id")(hc)
        verifyNoMoreInteractions(mockObligationsConnector)
        verifyNoInteractions(mockReturnsConnector)
      }
    }

    "return the next obligation" when {
      "there is no fulfilled so is the first return" in {
        when(mockObligationsConnector.getOpen(any)(any)).thenReturn(Future.successful(PPTObligations(Some(obligation("key")), None, 0, true, false)))
        when(mockObligationsConnector.getFulfilled(any)(any)).thenReturn(Future.successful(Seq.empty))

        val result = await(sut.nextOpenObligationAndIfFirst("some-ppt-id")(hc))

        result mustBe Some((obligation("key"), true))
        verify(mockObligationsConnector).getOpen("some-ppt-id")(hc)
        verify(mockObligationsConnector).getFulfilled("some-ppt-id")(hc)
      }

      "there is some fulfilled so is NOT the first return" in {
        when(mockObligationsConnector.getOpen(any)(any)).thenReturn(Future.successful(PPTObligations(Some(obligation("key")), None, 0, true, false)))
        when(mockObligationsConnector.getFulfilled(any)(any)).thenReturn(Future.successful(Seq(obligation("key2"))))

        val result = await(sut.nextOpenObligationAndIfFirst("some-ppt-id")(hc))

        result mustBe Some((obligation("key"), false))
        verify(mockObligationsConnector).getOpen("some-ppt-id")(hc)
        verify(mockObligationsConnector).getFulfilled("some-ppt-id")(hc)
      }
    }

    "bubble errors" when {
      object TestException extends Exception("boom!")
      "call to getOpen fails" in {
        when(mockObligationsConnector.getOpen(any)(any)).thenReturn(Future.failed(TestException))

        intercept[TestException.type](await(sut.nextOpenObligationAndIfFirst("some-ppt-id")(hc)))
      }
      "call to getFulfilled fails" in {
        when(mockObligationsConnector.getOpen(any)(any)).thenReturn(Future.successful(PPTObligations(Some(obligation("key")), None, 0, true, false)))
        when(mockObligationsConnector.getFulfilled(any)(any)).thenReturn(Future.failed(TestException))

        intercept[TestException.type](await(sut.nextOpenObligationAndIfFirst("some-ppt-id")(hc)))
      }
    }
  }
}
