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
import controllers.actions.IdentifierAction.{pptEnrolmentIdentifierName, pptEnrolmentKey}
import controllers.{routes => agentRoutes}
import controllers.home.{routes => homeRoutes}
import models.SignedInUser
import models.Mode.NormalMode
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.{BodyParsers, Headers, Results}
import play.api.test.Helpers._
import support.AuthHelper.expectedAcceptableCredentialsPredicate
import support.PptTestData.{newEnrolment, newEnrolments, pptEnrolment}
import support.{FakeCustomRequest, PptTestData}
import uk.gov.hmrc.auth.core._

import scala.concurrent.ExecutionContext

class AuthenticatedIdentifierActionSpec
    extends SpecBase with GuiceOneAppPerSuite with FakeCustomRequest with MetricsMocks {

  implicit val ec: ExecutionContext = ExecutionContext.global

  val application = applicationBuilder(userAnswers = None).build()
  val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
  val appConfig   = application.injector.instanceOf[FrontendAppConfig]

  class Harness(authAction: AuthenticatedIdentifierAction) {
    def onPageLoad() = authAction(_ => Results.Ok)
  }

  "Identifier Action" - {

    "redirect to not enrolled page when enrolment id is missing" in {
      val user = PptTestData.newUser("123", Some(pptEnrolment("")))

      running(application) {
        val result = runAuth(user, FakeAuthConnector.createSuccessAuthConnector(user))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(homeRoutes.UnauthorisedController.notEnrolled.url)
      }
    }

    "redirect agents to client identification page if we can see client identifier on the agents session" in {
      val agent = PptTestData.newAgent("456")

      running(application) {
        val result = runAuth(agent, FakeAuthConnector.createSuccessAuthConnector(agent))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(
          agentRoutes.AgentsController.onPageLoad(NormalMode).url
        )
      }
    }

    "process request when enrolment id is present on a normal user's auth response" in {
      val user = PptTestData.newUser("123", Some(pptEnrolment("555")))

      running(application) {
        status(runAuth(user, FakeAuthConnector.createSuccessAuthConnector(user))) mustBe OK
      }
    }

    "process request when an authorised client identifier is seen on an agents session" in {
      val agent    = PptTestData.newAgent("456")
      val fakeAuth = FakeAuthConnector.createSuccessAuthConnector(agent)
      val agentDelegatedAuthPredicate =
        Enrolment("HMRC-PPT-ORG").withIdentifier(pptEnrolmentIdentifierName,
                                                 "XMPPT0000000123"
        ).withDelegatedAuthRule("ppt-auth").and(expectedAcceptableCredentialsPredicate)

      running(application) {
        val result = runAuth(agent, fakeAuth, Some("XMPPT0000000123"))

        status(result) mustBe OK
        fakeAuth.predicate.get mustBe agentDelegatedAuthPredicate
      }
    }

    "process request when enrolment id is present and multiple identifier exist for same key" in {
      val user = PptTestData.newUser(
        "123",
        Option(
          newEnrolments(newEnrolment(pptEnrolmentKey, pptEnrolmentIdentifierName, "555"),
                        newEnrolment(pptEnrolmentKey, "ABC-Name", "999")
          )
        )
      )

      running(application) {
        val result = runAuth(user, FakeAuthConnector.createSuccessAuthConnector(user))

        status(result) mustBe OK
      }
    }

    "redirect to not enrolled page when enrolment id is not present and multiple identifier exist for same key" in {
      val user =
        PptTestData.newUser("123",
                            Some(
                              newEnrolments(newEnrolment(pptEnrolmentKey, "DEF-NAME", "555"),
                                            newEnrolment(pptEnrolmentKey, "ABC-Name", "999")
                              )
                            )
        )

      running(application) {
        val result = runAuth(user, FakeAuthConnector.createSuccessAuthConnector(user))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(homeRoutes.UnauthorisedController.notEnrolled().url)
      }
    }

    "redirect to not enrolled page when enrolment id is present but no ppt enrolment key found" in {
      val user = PptTestData.newUser(
        "123",
        Some(
          newEnrolments(newEnrolment("SOME-OTHER-KEY", pptEnrolmentIdentifierName, "555"),
                        newEnrolment(pptEnrolmentKey, "ABC-Name", "999")
          )
        )
      )

      running(application) {
        val result = runAuth(user, FakeAuthConnector.createSuccessAuthConnector(user))

        redirectLocation(result) mustBe Some(homeRoutes.UnauthorisedController.notEnrolled().url)
      }
    }

    "time calls to authorisation" in {
      val user = PptTestData.newUser("123", Some(pptEnrolment("555")))

      running(application) {
        runAuth(user, FakeAuthConnector.createSuccessAuthConnector(user))
        metricsMock.defaultRegistry.timer("ppt.returns.upstream.auth.timer").getCount must be > 0L
      }
    }

    "process request when enrolment id is present and allowed" in {
      val enrolmentId = "555"
      val user        = PptTestData.newUser("123", Some(pptEnrolment(enrolmentId)))

      running(application) {
        val result = runAuth(user,
                             FakeAuthConnector.createSuccessAuthConnector(user),
                             None,
                             new PptReferenceAllowedList(Seq(enrolmentId))
        )

        status(result) mustBe OK
      }
    }

    "redirect to home when enrolment id is present but not allowed" in {
      val user = PptTestData.newUser("123", Some(pptEnrolment("555")))

      running(application) {
        val result = runAuth(user,
                             FakeAuthConnector.createSuccessAuthConnector(user),
                             None,
                             new PptReferenceAllowedList(Seq("someOtherEnrolmentId"))
        )
        redirectLocation(result) mustBe Some(homeRoutes.UnauthorisedController.unauthorised().url)
      }
    }

    "when the user hasn't logged in" - {

      "must redirect the user to log in " in {
        running(application) {
          val result = runAuth(PptTestData.newUser(),
                               FakeAuthConnector.createFailingAuthConnector(new MissingBearerToken)
          )

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value must startWith(appConfig.loginUrl)
        }
      }
    }

    "the user's session has expired" - {

      "must redirect the user to log in " in {
        running(application) {
          val result = runAuth(PptTestData.newUser(),
                               FakeAuthConnector.createFailingAuthConnector(new BearerTokenExpired)
          )

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value must startWith(appConfig.loginUrl)
        }
      }
    }

    "the user doesn't have sufficient enrolments" - {

      "must redirect the user to the unauthorised page" in {
        running(application) {
          val result =
            runAuth(PptTestData.newUser(),
                    FakeAuthConnector.createFailingAuthConnector(new InsufficientEnrolments)
            )

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe homeRoutes.UnauthorisedController.notEnrolled.url
        }
      }
    }

    "the user doesn't have sufficient confidence level" - {

      "must redirect the user to the unauthorised page" in {
        running(application) {
          val result =
            runAuth(PptTestData.newUser(),
                    FakeAuthConnector.createFailingAuthConnector(new InsufficientConfidenceLevel)
            )

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe homeRoutes.UnauthorisedController.unauthorised.url
        }
      }
    }

    "the user used an unaccepted auth provider" - {

      "must redirect the user to the unauthorised page" in {
        running(application) {
          val result =
            runAuth(PptTestData.newUser(),
                    FakeAuthConnector.createFailingAuthConnector(new UnsupportedAuthProvider)
            )

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe homeRoutes.UnauthorisedController.unauthorised.url
        }
      }
    }

    "the user has an unsupported affinity group" - {

      "must redirect the user to the unauthorised page" in {
        running(application) {
          val result =
            runAuth(PptTestData.newUser(),
                    FakeAuthConnector.createFailingAuthConnector(new UnsupportedAffinityGroup)
            )

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(homeRoutes.UnauthorisedController.unauthorised.url)
        }
      }
    }

    "the user has an unsupported credential role" - {

      "must redirect the user to the unauthorised page" in {
        running(application) {
          val result =
            runAuth(PptTestData.newUser(),
                    FakeAuthConnector.createFailingAuthConnector(new UnsupportedCredentialRole)
            )

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(homeRoutes.UnauthorisedController.unauthorised.url)
        }
      }
    }
  }

  private def runAuth(
    user: SignedInUser,
    authConnector: AuthConnector,
    pptClient: Option[String] = None,
    pptReferenceAllowedList: PptReferenceAllowedList = new PptReferenceAllowedList(Seq.empty)
  ) = {
    val authAction = createIdentifierAction(authConnector, pptReferenceAllowedList)
    val controller = new Harness(authAction)

    controller.onPageLoad()(authRequest(Headers(), user, pptClient))
  }

  private def createIdentifierAction(
    fakeAuth: AuthConnector,
    pptReferenceAllowedList: PptReferenceAllowedList
  ) =
    new AuthenticatedIdentifierAction(fakeAuth,
                                      pptReferenceAllowedList,
                                      appConfig,
                                      metricsMock,
                                      bodyParsers
    )

}
