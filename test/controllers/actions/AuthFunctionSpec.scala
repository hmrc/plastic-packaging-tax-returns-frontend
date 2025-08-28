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

package controllers.actions

import base.MetricsMocks
import config.FrontendAppConfig
import org.mockito.ArgumentMatchers.refEq
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.MockitoSugar.{reset, verify}
import org.mockito.MockitoSugar.{mock, spyLambda, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import play.api.mvc.Results.Ok
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, defaultAwaitTimeout, redirectLocation}
import uk.gov.hmrc.auth.core.authorise.EmptyPredicate
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.internalId
import uk.gov.hmrc.auth.core.{AuthConnector, BearerTokenExpired, IncorrectCredentialStrength}

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class AuthFunctionSpec extends PlaySpec with MetricsMocks with BeforeAndAfterEach {

  val authConnector: AuthConnector = mock[AuthConnector]
  val appConfig: FrontendAppConfig = mock[FrontendAppConfig]

  val sut = new AuthFunction(
    authConnector,
    appConfig,
    metricsMock
  )(global)

  val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/target-url")
  val testBlock: AuthedUser[Any] => Future[Result] = spyLambda(_ => Future.successful(Ok("test")))
  val predicate                                    = EmptyPredicate

  override def beforeEach(): Unit = {
    reset(appConfig, authConnector, testBlock)
    super.beforeEach()
  }

  "authorised" must {
    "invoke block" when {
      "user is logged in" in {
        when(authConnector.authorise[Option[String]](any, any)(any, any)).thenReturn(
          Future.successful(Some("internalId"))
        )

        await(sut.authorised(predicate, request, testBlock))

        verify(testBlock).apply(any)
        verify(authConnector).authorise(refEq(predicate), refEq(internalId))(any, any)
      }
    }

    "redirect" when {
      "NoActiveSession" in {
        when(authConnector.authorise[Option[String]](any, any)(any, any)).thenReturn(
          Future.failed(BearerTokenExpired())
        )
        when(appConfig.loginUrl).thenReturn("/login-url")

        val result = sut.authorised(predicate, request, testBlock)

        redirectLocation(result).get must startWith("/login-url")
        withClue("must append continue param from request target") {
          redirectLocation(result).get must include("?continue=%2Ftarget-url")
        }
        verifyNoInteractions(testBlock)
      }
      "IncorrectCredentialStrength" in {
        when(authConnector.authorise[Option[String]](any, any)(any, any)).thenReturn(
          Future.failed(IncorrectCredentialStrength())
        )
        when(appConfig.mfaUpliftUrl).thenReturn("/mfa-uplift-url")
        when(appConfig.serviceIdentifier).thenReturn("service-identifier")

        val result = sut.authorised(predicate, request, testBlock)

        redirectLocation(result).get must startWith("/mfa-uplift-url")
        withClue("must append continueUrl param from request target") {
          redirectLocation(result).get must include("continueUrl=%2Ftarget-url")
        }
        withClue("must append origin param with serviceIdentifier") {
          redirectLocation(result).get must include("origin=service-identifier")
        }

        verifyNoInteractions(testBlock)
      }
    }

    "error" when {
      "internalId is not returned" in {
        when(authConnector.authorise[Option[String]](any, any)(any, any)).thenReturn(
          Future.successful(None)
        )

        val ex = intercept[IllegalStateException](await(sut.authorised(predicate, request, testBlock)))
        ex.getMessage mustBe "internalId is required"
      }
    }
  }

}
