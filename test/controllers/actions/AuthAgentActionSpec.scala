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

package controllers.actions

import base.{FakeAuthConnector, MetricsMocks, SpecBase}
import config.FrontendAppConfig
import controllers.home.{routes => homeRoutes}
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.mock
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status.SEE_OTHER
import play.api.mvc.{Headers, Results}
import play.api.test.Helpers.{await, defaultAwaitTimeout, redirectLocation, running, status, stubMessagesControllerComponents}
import support.{FakeCustomRequest, PptTestData}
import uk.gov.hmrc.auth.core.{AffinityGroup, AuthConnector, CredentialStrength, IncorrectCredentialStrength, InternalError, MissingBearerToken}

class AuthAgentActionSpec extends SpecBase with GuiceOneAppPerSuite with FakeCustomRequest with MetricsMocks {

  private val appConfig = mock[FrontendAppConfig]
  val application = applicationBuilder(userAnswers = None).build()

  val expectedPredicate = AffinityGroup.Agent.and(
    AffinityGroup.Agent.or(CredentialStrength(CredentialStrength.strong))
  )

  private def createAuthAction(authConnector: AuthConnector): AuthAgentAction =
    new AuthAgentActionImpl(authConnector,
                            appConfig,
                            metricsMock,
                            stubMessagesControllerComponents()
    )

  class Harness(authAction: AuthAgentAction) {
    def onPageLoad() = authAction(_ => Results.Ok)
  }

  "Auth Agent Action" - {

    "should authorise with strong credential" in {
      val user = PptTestData.newUser()

      running(application) {
        val fakeAuthConnector = FakeAuthConnector.createSuccessAuthConnector(user)
        val controller = new Harness(createAuthAction(fakeAuthConnector))
        await(controller.onPageLoad()(authRequest(Headers(), user)))

        fakeAuthConnector.predicate.get mustBe expectedPredicate
      }
    }

    "time calls to authorisation" in {
      val agent = PptTestData.newAgent()

      running(application) {
        val controller = new Harness(createAuthAction(FakeAuthConnector.createSuccessAuthConnector(agent)))
        controller.onPageLoad()(authRequest(Headers(), agent))

        metricsMock.defaultRegistry.timer("ppt.returns.upstream.auth.timer").getCount must be > 0L
      }
    }

    "process request when signed in user has agent affinity" in {
      val agent = PptTestData.newAgent()

      running(application) {
        val controller = new Harness(createAuthAction(FakeAuthConnector.createSuccessAuthConnector(agent)))
        val result = controller.onPageLoad()(authRequest(Headers(), agent))

        status(result) mustBe 200
      }
    }

    "redirect when user not logged in" in {
      running(application) {
        when(appConfig.loginUrl).thenReturn("login-url")
        when(appConfig.loginContinueUrl).thenReturn("login-continue-url")

        val controller = new Harness(createAuthAction(FakeAuthConnector.createFailingAuthConnector(new MissingBearerToken)))
        val result = controller.onPageLoad()(authRequest(Headers(), PptTestData.newAgent()))

        redirectLocation(result) mustBe Some("login-url?continue=login-continue-url")
      }
    }

    "redirect the user to MFA Uplift page if the user has incorrect credential strength " in {
      running(application) {
        when(appConfig.mfaUpliftUrl).thenReturn("mfa-uplift-url")
        when(appConfig.loginContinueUrl).thenReturn("login-continue-url")
        when(appConfig.serviceIdentifier).thenReturn("PPT")

        val controller = new Harness(createAuthAction(FakeAuthConnector.createFailingAuthConnector(new IncorrectCredentialStrength)))
        val result = controller.onPageLoad()(authRequest(Headers(), PptTestData.newAgent()))

        redirectLocation(result) mustBe Some(
          "mfa-uplift-url?origin=PPT&continueUrl=login-continue-url"
        )
      }
    }

    "redirect to unauthorised page when authorisation fails" in {
      running(application) {
        val controller = new Harness(createAuthAction(FakeAuthConnector.createFailingAuthConnector(new InternalError("Some unexpected auth error"))))
        val result = controller.onPageLoad()(authRequest(Headers(), PptTestData.newAgent()))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(homeRoutes.UnauthorisedController.unauthorised().url)
      }
    }
  }
}

