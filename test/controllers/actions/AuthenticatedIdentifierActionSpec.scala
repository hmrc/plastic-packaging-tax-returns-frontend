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
import controllers.actions.AuthenticatedIdentifierAction.IdentifierAction.pptEnrolmentKey
import controllers.actions.AuthenticatedIdentifierActionSpec.RetrievalSugar
import models.Mode.NormalMode
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.Mockito.{reset, verify, when}
import org.mockito.MockitoSugar.mock
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import play.api.mvc.Results.Ok
import play.api.mvc.{Result, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import repositories.SessionRepository.Paths.AgentSelectedPPTRef
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{affinityGroup, allEnrolments, internalId}
import uk.gov.hmrc.auth.core.retrieve.~

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class AuthenticatedIdentifierActionSpec extends PlaySpec with BeforeAndAfterEach {

  val mockAuthConnector = mock[AuthConnector]
  val mockConfig = mock[FrontendAppConfig]
  val mockSessionRepository = mock[SessionRepository]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAuthConnector, mockSessionRepository, mockConfig)
    when(mockConfig.loginUrl).thenReturn("test-login-url")
  }

  val emptyEnrolments: Enrolments = Enrolments.apply(Set.empty)

  val pptEnrolment: Enrolments = Enrolments(
    Set(Enrolment("HMRC-PPT-ORG").withIdentifier("EtmpRegistrationNumber", "ppt-enrolment-ref"))
  )

  val sut: AuthenticatedIdentifierAction = new AuthenticatedIdentifierAction(
    mockAuthConnector,
    mockConfig,
    mockSessionRepository,
    stubControllerComponents()
  )(global)

  def test(): Future[Result] = sut(request => Ok(request.pptReference))(FakeRequest("GET", "/target"))

  def mockAuthRetrieval[A](authResponse: Future[A]) =
    when(mockAuthConnector.authorise[A](any(), any())(any(), any()))
      .thenReturn(authResponse)

  "invokeBlock" must {
    "invoke the block" when {
      "the user is a ppt enrolled user" in{
        val retrieval: Option[String] ~ Option[AffinityGroup] ~ Enrolments =
          Some("user-internal-id") AND Some(AffinityGroup.Organisation) AND pptEnrolment

        mockAuthRetrieval(Future.successful(retrieval))

        val result = test()

        status(result) mustBe OK
        contentAsString(result) mustBe "ppt-enrolment-ref"

        verify(mockAuthConnector).authorise(
          refEq(Enrolment(pptEnrolmentKey).and(CredentialStrength(CredentialStrength.strong)).or(AffinityGroup.Agent)),
          refEq(internalId and affinityGroup and allEnrolments)
        )(any(), any())
      }

      "the user is an agent" in{
        val retrieval: Option[String] ~ Option[AffinityGroup] ~ Enrolments =
          Some("agent-internal-id") AND Some(AffinityGroup.Agent) AND emptyEnrolments

        mockAuthRetrieval(Future.successful(retrieval))
        when(mockSessionRepository.get[String]("agent-internal-id", AgentSelectedPPTRef))
          .thenReturn(Future.successful(Some("AGENT-PPT-REF")))

        val result = test()

        status(result) mustBe OK
        contentAsString(result) mustBe "AGENT-PPT-REF"
      }
    }

    "redirect to Agent select client page" when {
      "the Agent does not have a client ppt cached " in{
        val retrieval: Option[String] ~ Option[AffinityGroup] ~ Enrolments =
          Some("agent-internal-id") AND Some(AffinityGroup.Agent) AND emptyEnrolments

        mockAuthRetrieval(Future.successful(retrieval))
        when(mockSessionRepository.get[String]("agent-internal-id", AgentSelectedPPTRef))
          .thenReturn(Future.successful(None))

        val result = test()

        redirectLocation(result) mustBe Some(controllers.routes.AgentsController.onPageLoad(NormalMode).url)
      }
    }

    "redirect to auth pages" when {
      "the user has NoActiveSession" in{
        mockAuthRetrieval(Future.failed(BearerTokenExpired("this is one of many NoActiveSession exceptions")))

        val result = test()

        redirectLocation(result).get must startWith("test-login-url")
        withClue("the target url should be a query param for auth to continue to"){
          redirectLocation(result).get must include("?continue=%2Ftarget")
        }
      }
    }

    "redirect to not enrolled pages" when {
      "the user is not enrolled for PPT" in{
        mockAuthRetrieval(Future.failed(InsufficientEnrolments("the user is not enrolled")))

        val result = test()

        redirectLocation(result) mustBe Some(controllers.home.routes.UnauthorisedController.notEnrolled().url)
      }
    }
  }

}

object AuthenticatedIdentifierActionSpec {
  implicit class RetrievalSugar[A](val a: A) extends AnyVal {
    def AND[B](b: B): A ~ B = new ~(a, b)
  }
}
