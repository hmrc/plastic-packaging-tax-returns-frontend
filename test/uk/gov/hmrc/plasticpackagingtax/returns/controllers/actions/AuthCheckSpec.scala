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

package uk.gov.hmrc.plasticpackagingtax.returns.controllers.actions

import org.mockito.Mockito.when
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.mvc.{Headers, Results}
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.plasticpackagingtax.returns.base.unit.ControllerSpec
import uk.gov.hmrc.plasticpackagingtax.returns.base.{MetricsMocks, PptTestData}
import uk.gov.hmrc.plasticpackagingtax.returns.config.AppConfig
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.home.{routes => homeRoutes}
import uk.gov.hmrc.plasticpackagingtax.returns.models.request.AuthenticatedRequest

import scala.concurrent.Future

class AuthCheckSpec extends ControllerSpec with MetricsMocks {

  private val appConfig = mock[AppConfig]

  private def createAuthAction(): AuthAgentAction =
    new AuthAgentActionImpl(mockAuthConnector,
                            appConfig,
                            metricsMock,
                            stubMessagesControllerComponents()
    )

  private val okResponseGenerator = (_: AuthenticatedRequest[_]) => Future(Results.Ok)

  "Auth Check Action" should {

    "time calls to authorisation" in {
      val user = PptTestData.newUser()
      authorizedUser(user,
                     requiredPredicate =
                       AffinityGroup.Agent.and(expectedAcceptableCredentialsPredicate)
      )

      val result =
        await(createAuthAction().invokeBlock(authRequest(Headers(), user), okResponseGenerator))
      metricsMock.defaultRegistry.timer("ppt.returns.upstream.auth.timer").getCount should be > 0L
    }

    "process request when signed in user" in {
      val user = PptTestData.newUser()
      authorizedUser(user,
                     requiredPredicate =
                       AffinityGroup.Agent.and(expectedAcceptableCredentialsPredicate)
      )

      await(
        createAuthAction().invokeBlock(authRequest(Headers(), user), okResponseGenerator)
      ) mustBe Results.Ok
    }

    "redirect when user not logged in" in {
      when(appConfig.loginUrl).thenReturn("login-url")
      when(appConfig.loginContinueUrl).thenReturn("login-continue-url")

      whenAuthFailsWith(MissingBearerToken())

      val result =
        createAuthAction().invokeBlock(authRequest(Headers(), PptTestData.newUser()),
                                       okResponseGenerator
        )

      redirectLocation(result) mustBe Some("login-url?continue=login-continue-url")
    }

    "redirect the user to MFA Uplift page if the user has incorrect credential strength " in {
      when(appConfig.mfaUpliftUrl).thenReturn("mfa-uplift-url")
      when(appConfig.loginContinueUrl).thenReturn("login-continue-url")
      when(appConfig.serviceIdentifier).thenReturn("PPT")

      whenAuthFailsWith(IncorrectCredentialStrength())
      val result =
        createAuthAction().invokeBlock(authRequest(Headers(), PptTestData.newUser()),
                                       okResponseGenerator
        )

      redirectLocation(result) mustBe Some(
        "mfa-uplift-url?origin=PPT&continueUrl=login-continue-url"
      )
    }

    "redirect to unauthorised page when authorisation fails" in {
      whenAuthFailsWith(InternalError("Some unexpected auth error"))

      val result =
        createAuthAction().invokeBlock(authRequest(Headers(), PptTestData.newUser()),
                                       okResponseGenerator
        )

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(homeRoutes.UnauthorisedController.unauthorised().url)
    }
  }
}
