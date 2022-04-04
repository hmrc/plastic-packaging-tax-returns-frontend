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
import models.SignedInUser
import models.requests.{IdentifiedRequest, IdentityData}
import org.joda.time.DateTimeZone.UTC
import org.joda.time.{DateTime, LocalDate}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json._
import play.api.mvc.{BodyParsers, Headers, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import support.AuthHelper.{
  nrsCredentialRole,
  nrsCredentialStrength,
  nrsDateOfBirth,
  nrsGroupIdentifierValue,
  nrsItmpAddress,
  nrsItmpName,
  nrsLoginTimes,
  nrsMdtpInformation
}
import support.PptTestData.pptEnrolment
import support.{AuthHelper, FakeCutomRequest, PptTestData}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{
  ~,
  ItmpAddress,
  ItmpName,
  LoginTimes,
  MdtpInformation,
  Retrieval,
  SimpleRetrieval
}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals._
import uk.gov.hmrc.auth.core.syntax.retrieved.authSyntaxForRetrieved

import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}

class AuthActionOldSpec
    extends SpecBase with GuiceOneAppPerSuite with FakeCutomRequest with MetricsMocks {

  implicit val ec: ExecutionContext = ExecutionContext.global

  private val okResponseGenerator = (_: IdentifiedRequest[_]) => Future(Results.Ok)
  val application                 = applicationBuilder(userAnswers = None).build()
  val bodyParsers                 = application.injector.instanceOf[BodyParsers.Default]
  val appConfig                   = application.injector.instanceOf[FrontendAppConfig]

  val nrsGroupIdentifierValue = Some("groupIdentifierValue")
  val nrsCredentialRole       = Some(User)
  val nrsMdtpInformation      = MdtpInformation("deviceId", "sessionId")
  val nrsItmpName             = ItmpName(Some("givenName"), Some("middleName"), Some("familyName"))
  val nrsDateOfBirth          = Some(LocalDate.now().minusYears(25))

  val nrsItmpAddress =
    ItmpAddress(Some("line1"),
                Some("line2"),
                Some("line3"),
                Some("line4"),
                Some("line5"),
                Some("postCode"),
                Some("countryName"),
                Some("countryCode")
    )

  val nrsCredentialStrength       = Some("STRONG")
  val currentLoginTime: DateTime  = new DateTime(1530442800000L, UTC)
  val previousLoginTime: DateTime = new DateTime(1530464400000L, UTC)
  val nrsLoginTimes               = LoginTimes(currentLoginTime, Some(previousLoginTime))

  val data = SignedInUser.getClass.getDeclaredFields

  class Harness(authAction: IdentifierAction) {
    def onPageLoad() = authAction(_ => Results.Ok)
  }

  "Auth Action 1" - {

    "redirect to not enrolled page when enrolment id is missing" in {

      val user = PptTestData.newUser("123", Some(pptEnrolment("")))

      val t = AuthHelper.createCredentialForUser(user)

      running(application) {

        val authAction = createAuthAction(new FakeSuccessfulAuthConnectorCopy)
        val controller = new Harness(authAction)

        val result = controller.onPageLoad()(authRequest(Headers(), user))
        println("-2-")

        status(result) mustBe SEE_OTHER
        println(s"-3- =>  $result")
        redirectLocation(result) mustBe Some(homeRoutes.UnauthorisedController.notEnrolled.url)
      }
    }
  }

  "Auth Action" - {

    "when the user hasn't logged in" - {

      "must redirect the user to log in " in {
        running(application) {
          val authAction =
            createAuthAction(new FakeFailingAuthConnectorCopy(new MissingBearerToken))
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
            createAuthAction(new FakeFailingAuthConnectorCopy(new BearerTokenExpired))
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
            createAuthAction(new FakeFailingAuthConnectorCopy(new InsufficientEnrolments))
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe homeRoutes.UnauthorisedController.unauthorised.url
        }
      }
    }

    "the user doesn't have sufficient confidence level" - {

      "must redirect the user to the unauthorised page" in {
        running(application) {
          val authAction =
            createAuthAction(new FakeFailingAuthConnectorCopy(new InsufficientConfidenceLevel))
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
            createAuthAction(new FakeFailingAuthConnectorCopy(new UnsupportedAuthProvider))
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
            createAuthAction(new FakeFailingAuthConnectorCopy(new UnsupportedAffinityGroup))
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
            createAuthAction(new FakeFailingAuthConnectorCopy(new UnsupportedCredentialRole))
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(homeRoutes.UnauthorisedController.unauthorised.url)
        }
      }
    }
  }

  private def createAuthAction(
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

class FakeFailingAuthConnectorCopy @Inject() (exceptionToReturn: Throwable) extends AuthConnector {
  val serviceUrl: String = ""

  override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[A] =
    Future.failed(exceptionToReturn)

}

class FakeSuccessfulAuthConnectorCopy extends AuthConnector {

  override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[A] = {
    val user = PptTestData.newUser("123", Some(pptEnrolment("")))
    AuthHelper.createCredentialForUser(user).asInstanceOf[Future[A]]
  }

}
