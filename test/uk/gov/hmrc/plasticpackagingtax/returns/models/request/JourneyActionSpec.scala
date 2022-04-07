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

package uk.gov.hmrc.plasticpackagingtax.returns.models.request

import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.BDDMockito.`given`
import org.mockito.Mockito.{reset, verify}
import org.mockito.{ArgumentCaptor, Mockito}
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.mvc.{Headers, Result, Results}
import play.api.test.Helpers.await
import uk.gov.hmrc.auth.core.InsufficientEnrolments
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, InternalServerException, RequestId}
import uk.gov.hmrc.plasticpackagingtax.returns.base.PptTestData
import uk.gov.hmrc.plasticpackagingtax.returns.base.PptTestData.pptEnrolment
import uk.gov.hmrc.plasticpackagingtax.returns.base.unit.ControllerSpec
import uk.gov.hmrc.plasticpackagingtax.returns.connectors.DownstreamServiceError

import scala.concurrent.{ExecutionContext, Future}

class JourneyActionSpec extends ControllerSpec {

  private val responseGenerator = mock[JourneyRequest[_] => Future[Result]]

  private val actionRefiner =
    new JourneyAction(mockTaxReturnsConnector, mockAuditor, mockObligationsConnector)(
      ExecutionContext.global
    )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockTaxReturnsConnector, responseGenerator, mockObligationsConnector)
    given(responseGenerator.apply(any())).willReturn(Future.successful(Results.Ok))
  }

  val taxReturnId: String = "123"

  "action refine" should {

    "permit request and send audit event" when {
      "enrolmentId found" in {
        given(mockTaxReturnsConnector.find(refEq(taxReturnId))(any[HeaderCarrier])).willReturn(
          Future.successful(Right(Option(aTaxReturn(withId(taxReturnId)))))
        )
        mockGetObligation()

        await(
          actionRefiner.invokeBlock(
            authRequest(user = PptTestData.newUser("123", Some(pptEnrolment(taxReturnId)))),
            responseGenerator
          )
        ) mustBe Results.Ok
      }
    }

    "pass through headers" when {
      "enrolmentId found" in {
        val headers = Headers().add(HeaderNames.xRequestId -> "req1")
        given(mockTaxReturnsConnector.find(refEq(taxReturnId))(any[HeaderCarrier])).willReturn(
          Future.successful(Right(Option(aTaxReturn(withId(taxReturnId)))))
        )
        mockGetObligation()

        await(
          actionRefiner.invokeBlock(
            authRequest(headers,
                        user = PptTestData.newUser("123", Some(pptEnrolment(taxReturnId)))
            ),
            responseGenerator
          )
        ) mustBe Results.Ok

        getHeaders.requestId mustBe Some(RequestId("req1"))
      }
    }

    "create tax return and send audit event" when {
      "tax return details not found" in {
        given(mockTaxReturnsConnector.find(refEq(taxReturnId))(any[HeaderCarrier])).willReturn(
          Future.successful(Right(None))
        )
        given(mockTaxReturnsConnector.create(any())(any[HeaderCarrier])).willReturn(
          Future.successful(Right(aTaxReturn(withId(taxReturnId))))
        )
        mockGetObligation()

        await(
          actionRefiner.invokeBlock(
            authRequest(user = PptTestData.newUser("123", Some(pptEnrolment(taxReturnId)))),
            responseGenerator
          )
        ) mustBe Results.Ok
        verify(mockAuditor, Mockito.atLeast(1)).newTaxReturnStarted()(any(), any())
      }
    }

    "load tax return" when {
      "tax return exists" in {
        given(mockTaxReturnsConnector.find(refEq(taxReturnId))(any[HeaderCarrier])).willReturn(
          Future.successful(Right(Option(aTaxReturn(withId(taxReturnId)))))
        )
        mockGetObligation()

        await(
          actionRefiner.invokeBlock(
            authRequest(user = PptTestData.newUser("123", Some(pptEnrolment(taxReturnId)))),
            responseGenerator
          )
        ) mustBe Results.Ok
      }
    }
  }

  def getHeaders: HeaderCarrier = {
    val captor = ArgumentCaptor.forClass(classOf[HeaderCarrier])
    verify(mockTaxReturnsConnector).find(refEq(taxReturnId))(captor.capture())
    captor.getValue
  }

  "throw exception" when {
    "enrolmentId not found" in {
      intercept[InsufficientEnrolments] {
        await(
          actionRefiner.invokeBlock(authRequest(user = PptTestData.newUser("123", None)),
                                    responseGenerator
          )
        )
      }
    }

    "enrolmentId is empty" in {
      intercept[InsufficientEnrolments] {
        await(
          actionRefiner.invokeBlock(
            authRequest(user = PptTestData.newUser("123", Some(pptEnrolment("")))),
            responseGenerator
          )
        )
      }
    }
    "cannot load user tax return" in {
      given(mockTaxReturnsConnector.find(refEq(taxReturnId))(any[HeaderCarrier])).willReturn(
        Future.successful(
          Left(DownstreamServiceError("error", new InternalServerException("error")))
        )
      )
      mockGetObligation()

      intercept[DownstreamServiceError] {
        await(
          actionRefiner.invokeBlock(
            authRequest(user = PptTestData.newUser("123", Some(pptEnrolment(taxReturnId)))),
            responseGenerator
          )
        )
      }
    }
  }
}
