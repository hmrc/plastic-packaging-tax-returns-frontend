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

package controllers.actions

import config.FrontendAppConfig
import models.Mode.NormalMode
import models.requests.IdentifiedRequest
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.refEq
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.Mockito.{reset, verify, verifyNoInteractions}
import org.mockito.MockitoSugar.{mock, spyLambda, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import play.api.mvc.Results.Ok
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, defaultAwaitTimeout, redirectLocation, stubControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.~

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class AuthActionSpec extends PlaySpec with BeforeAndAfterEach {

  val authConnector: AuthConnector = mock[AuthConnector]
  val appConfig: FrontendAppConfig = mock[FrontendAppConfig]
  val sessionRepository: SessionRepository = mock[SessionRepository]

  val sut = new AuthenticatedIdentifierAction(
    authConnector,
    appConfig,
    sessionRepository,
    stubControllerComponents()
  )(global)

  val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/target-url")

  val testBlock: IdentifiedRequest[Any] => Future[Result] = spyLambda(_ => Future.successful(Ok("test")))

  override def beforeEach(): Unit = {
    reset(
      authConnector,
      appConfig,
      sessionRepository,
      testBlock
    )
    super.beforeEach()
  }

  "invokeBlock" must {
    "allow the user and invoke block" when {
      "user enrolled for ppt" in {
        val pptEnrolment = Enrolment("HMRC-PPT-ORG", Seq(EnrolmentIdentifier("EtmpRegistrationNumber", "ppt-ref")), "activated")

        when(authConnector.authorise[Option[String] ~ Option[AffinityGroup] ~ Enrolments](any,any)(any, any)).thenReturn(
          Future.successful(Some("internalId") and Some(AffinityGroup.Organisation) and Enrolments(Set(pptEnrolment)))
        )


        await(sut.invokeBlock(request, testBlock))

        val requestCaptor = ArgumentCaptor.forClass(classOf[IdentifiedRequest[Any]])
        verify(testBlock).apply(requestCaptor.capture())
        requestCaptor.getValue.asInstanceOf[IdentifiedRequest[Any]].pptReference mustBe "ppt-ref"
        verifyNoInteractions(sessionRepository)
      }
      "user is agent with selected PPT ref defined" in {
        when(authConnector.authorise[Option[String] ~ Option[AffinityGroup] ~ Enrolments](any,any)(any, any)).thenReturn(
          Future.successful(Some("internalId") and Some(AffinityGroup.Agent) and Enrolments(Set.empty))
        )
        when(sessionRepository.get[String](any, any)(any)).thenReturn(Future.successful(Some("selected-ppt-ref")))

        await(sut.invokeBlock(request, testBlock))

        val requestCaptor = ArgumentCaptor.forClass(classOf[IdentifiedRequest[Any]])
        verify(testBlock).apply(requestCaptor.capture())
        requestCaptor.getValue.asInstanceOf[IdentifiedRequest[Any]].pptReference mustBe "selected-ppt-ref"
        verify(sessionRepository).get(refEq("internalId"), refEq(SessionRepository.Paths.AgentSelectedPPTRef))(any)
      }
    }
    "redirect agent to select client" when {
      "there is no selection in the cache" in {
        when(authConnector.authorise[Option[String] ~ Option[AffinityGroup] ~ Enrolments](any,any)(any, any)).thenReturn(
          Future.successful(Some("internalId") and Some(AffinityGroup.Agent) and Enrolments(Set.empty))
        )
        when(sessionRepository.get[String](any, any)(any)).thenReturn(Future.successful(None))

        val result = sut.invokeBlock(request, testBlock)

        redirectLocation(result) mustBe Some(controllers.routes.AgentsController.onPageLoad(NormalMode).url)
        verifyNoInteractions(testBlock)
        verify(sessionRepository).get(refEq("internalId"), refEq(SessionRepository.Paths.AgentSelectedPPTRef))(any)
      }
    }
    "redirect to login" when {
      "No active session" in {
        when(authConnector.authorise[Option[String] ~ Option[AffinityGroup] ~ Enrolments](any,any)(any, any)).thenReturn(
          Future.failed(BearerTokenExpired())
        )
        when(appConfig.loginUrl).thenReturn("/login-url")

        val result = sut.invokeBlock(request, testBlock)

        redirectLocation(result).get must startWith("/login-url")
        withClue("must append continue param from request target") {
          redirectLocation(result).get must include("?continue=%2Ftarget-url")
        }
        verifyNoInteractions(testBlock)
        verifyNoInteractions(sessionRepository)
      }
    }
    "redirect to 'cant use this service'" when {
      "user is not enrolled" in {
        when(authConnector.authorise[Option[String] ~ Option[AffinityGroup] ~ Enrolments](any,any)(any, any)).thenReturn(
          Future.failed(InsufficientEnrolments())
        )

        val result = sut.invokeBlock(request, testBlock)

        redirectLocation(result) mustBe Some(controllers.home.routes.UnauthorisedController.notEnrolled().url)
        verifyNoInteractions(testBlock)
        verifyNoInteractions(sessionRepository)
      }
    }
    "error" when {
      "internal id is not returned" in {
        when(authConnector.authorise[Option[String] ~ Option[AffinityGroup] ~ Enrolments](any,any)(any, any)).thenReturn(
          Future.successful(None and Some(AffinityGroup.Organisation) and Enrolments(Set.empty))
        )

        val ex = intercept[IllegalArgumentException](await(sut.invokeBlock(request, testBlock)))

        ex.getMessage mustBe "internalId is required"
        verifyNoInteractions(testBlock)
        verifyNoInteractions(sessionRepository)
      }
    }
  }
}

