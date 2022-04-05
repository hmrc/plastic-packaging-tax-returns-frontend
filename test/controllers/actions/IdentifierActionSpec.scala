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
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.{BodyParsers, Headers, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import support.PptTestData.pptEnrolment
import support.{FakeCustomRequest, PptTestData}
import uk.gov.hmrc.auth.core._

import scala.concurrent.ExecutionContext

class IdentifierActionSpec
      extends SpecBase with GuiceOneAppPerSuite with FakeCustomRequest with MetricsMocks {

  implicit val ec: ExecutionContext = ExecutionContext.global

  val application                 = applicationBuilder(userAnswers = None).build()
  val bodyParsers                 = application.injector.instanceOf[BodyParsers.Default]
  val appConfig                   = application.injector.instanceOf[FrontendAppConfig]

  class Harness(authAction: IdentifierAction) {
    def onPageLoad() = authAction(_ => Results.Ok)
  }

  "Auth Action 1" - {

    "redirect to not enrolled page when enrolment id is missing" in {

      val user = PptTestData.newUser("123", Some(pptEnrolment("")))

      running(application) {

        val authAction = createIdentifierAction(FakeAuthConnector.createSuccessAuthConnector)
        val controller = new Harness(authAction)

        val result = controller.onPageLoad()(authRequest(Headers(), user))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(homeRoutes.UnauthorisedController.notEnrolled.url)

      }
    }
  }

  "Auth Action" - {

    "when the user hasn't logged in" - {

      "must redirect the user to log in " in {
        running(application) {
          val authAction =
            createIdentifierAction(FakeAuthConnector.createFailingAuthConnector(new MissingBearerToken))
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

          val authAction =
            createIdentifierAction(FakeAuthConnector.createFailingAuthConnector(new BearerTokenExpired))
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
          val authAction =
            createIdentifierAction(FakeAuthConnector.createFailingAuthConnector(new InsufficientEnrolments))
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
          val authAction =
            createIdentifierAction(FakeAuthConnector.createFailingAuthConnector(new InsufficientConfidenceLevel))
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
          val authAction =
            createIdentifierAction(FakeAuthConnector.createFailingAuthConnector(new UnsupportedAuthProvider))
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
          val authAction =
            createIdentifierAction(FakeAuthConnector.createFailingAuthConnector(new UnsupportedAffinityGroup))
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
          val authAction =
            createIdentifierAction(FakeAuthConnector.createFailingAuthConnector(new UnsupportedCredentialRole))
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(homeRoutes.UnauthorisedController.unauthorised.url)
        }
      }
    }
  }

  private def createIdentifierAction(
    fakeAuth: AuthConnector,
    pptReferenceAllowedList: PptReferenceAllowedList = new PptReferenceAllowedList(Seq.empty)
  ) =
    new AuthenticatedIdentifierAction(fakeAuth,
                                      pptReferenceAllowedList,
                                      appConfig,
                                      metricsMock,
                                      bodyParsers
    )
}

