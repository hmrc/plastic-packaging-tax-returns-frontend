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

import base.{MetricsMocks, SpecBase}
import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.home.{routes => homeRoutes}
import play.api.mvc.{BodyParsers, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import support.FakeCutomRequest
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class AuthActionSpec extends SpecBase with FakeCutomRequest with MetricsMocks {

  val application = applicationBuilder(userAnswers = None).build()
  val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
  val appConfig   = application.injector.instanceOf[FrontendAppConfig]

  class Harness(authAction: IdentifierAction) {
    def onPageLoad() = authAction(_ => Results.Ok)
  }

  "Auth Action" - {

    "when the user hasn't logged in" - {

      "must redirect the user to log in " in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {

          val authAction = createAuthAction(new MissingBearerToken)
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value must startWith(appConfig.loginUrl)
        }
      }
    }

    "the user's session has expired" - {

      "must redirect the user to log in " in {
        running(application) {

          val authAction = createAuthAction(new BearerTokenExpired)
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value must startWith(appConfig.loginUrl)
        }
      }
    }

    "the user doesn't have sufficient enrolments" - {

      "must redirect the user to the unauthorised page" in {
        running(application) {
          val authAction = createAuthAction(new InsufficientEnrolments)
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe homeRoutes.UnauthorisedController.notEnrolled.url
        }
      }
    }

    "the user doesn't have sufficient confidence level" - {

      "must redirect the user to the unauthorised page" in {
        running(application) {
          val authAction = createAuthAction(new InsufficientConfidenceLevel)
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe homeRoutes.UnauthorisedController.unauthorised.url
        }
      }
    }

    "the user used an unaccepted auth provider" - {

      "must redirect the user to the unauthorised page" in {
        running(application) {
          val authAction = createAuthAction(new UnsupportedAuthProvider)
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe homeRoutes.UnauthorisedController.unauthorised.url
        }
      }
    }

    "the user has an unsupported affinity group" - {

      "must redirect the user to the unauthorised page" in {
        running(application) {
          val authAction = createAuthAction(new UnsupportedAffinityGroup)

          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(homeRoutes.UnauthorisedController.unauthorised.url)
        }
      }
    }

    "the user has an unsupported credential role" - {

      "must redirect the user to the unauthorised page" in {
        running(application) {
          val authAction = createAuthAction(new UnsupportedCredentialRole)
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(homeRoutes.UnauthorisedController.unauthorised.url)
        }
      }
    }
  }

  private def createAuthAction(
    session: AuthorisationException,
    pptReferenceAllowedList: PptReferenceAllowedList = new PptReferenceAllowedList(Seq.empty)
  ) =
    new AuthenticatedIdentifierAction(new FakeFailingAuthConnectorCopy(session),
                                      pptReferenceAllowedList,
                                      appConfig,
                                      metricsMock,
                                      bodyParsers
    )

}

class FakeFailingAuthConnector @Inject() (exceptionToReturn: Throwable) extends AuthConnector {
  val serviceUrl: String = ""

  override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[A] =
    Future.failed(exceptionToReturn)

}
