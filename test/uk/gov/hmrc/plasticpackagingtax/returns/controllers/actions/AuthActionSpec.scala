/*
 * Copyright 2021 HM Revenue & Customs
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

import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.mvc.{Headers, Results}
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.InsufficientEnrolments
import uk.gov.hmrc.plasticpackagingtax.returns.base.PptTestData.{
  newEnrolment,
  newEnrolments,
  pptEnrolment
}
import uk.gov.hmrc.plasticpackagingtax.returns.base.{MetricsMocks, PptTestData}
import uk.gov.hmrc.plasticpackagingtax.returns.base.unit.ControllerSpec
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.actions.AuthAction.{
  pptEnrolmentIdentifierName,
  pptEnrolmentKey
}
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.home.{routes => homeRoutes}
import uk.gov.hmrc.plasticpackagingtax.returns.models.request.AuthenticatedRequest

import scala.concurrent.Future

class AuthActionSpec extends ControllerSpec with MetricsMocks {

  private def createAuthAction(
    pptReferenceAllowedList: PptReferenceAllowedList = new PptReferenceAllowedList(Seq.empty)
  ): AuthAction =
    new AuthActionImpl(mockAuthConnector,
                       pptReferenceAllowedList,
                       metricsMock,
                       stubMessagesControllerComponents()
    )

  private val okResponseGenerator = (_: AuthenticatedRequest[_]) => Future(Results.Ok)

  "Auth Action" should {

    "return InsufficientEnrolments when enrolment id is missing" in {
      val user = PptTestData.newUser("123", Some(pptEnrolment("")))
      authorizedUser(user)

      intercept[InsufficientEnrolments] {
        await(createAuthAction().invokeBlock(authRequest(Headers(), user), okResponseGenerator))
      }
    }

    "process request when enrolment id is present" in {
      val user = PptTestData.newUser("123", Some(pptEnrolment("555")))
      authorizedUser(user)

      await(
        createAuthAction().invokeBlock(authRequest(Headers(), user), okResponseGenerator)
      ) mustBe Results.Ok
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
      authorizedUser(user)

      await(
        createAuthAction().invokeBlock(authRequest(Headers(), user), okResponseGenerator)
      ) mustBe Results.Ok
    }

    "return InsufficientEnrolments when enrolment id is not present and multiple identifier exist for same key" in {
      val user = PptTestData.newUser(
        "123",
        Some(
          newEnrolments(newEnrolment(AuthAction.pptEnrolmentKey, "DEF-NAME", "555"),
                        newEnrolment(AuthAction.pptEnrolmentKey, "ABC-Name", "999")
          )
        )
      )
      authorizedUser(user)

      intercept[InsufficientEnrolments] {
        await(createAuthAction().invokeBlock(authRequest(Headers(), user), okResponseGenerator))
      }
    }

    "return InsufficientEnrolments when enrolment id is present but no ppt enrolment key found" in {
      val user = PptTestData.newUser("123",
                                     Some(
                                       newEnrolments(
                                         newEnrolment("SOME-OTHER-KEY",
                                                      AuthAction.pptEnrolmentIdentifierName,
                                                      "555"
                                         ),
                                         newEnrolment(AuthAction.pptEnrolmentKey, "ABC-Name", "999")
                                       )
                                     )
      )
      authorizedUser(user)

      intercept[InsufficientEnrolments] {
        await(createAuthAction().invokeBlock(authRequest(Headers(), user), okResponseGenerator))
      }
    }

    "time calls to authorisation" in {
      val user = PptTestData.newUser("123", Some(pptEnrolment("555")))
      authorizedUser(user)

      await(createAuthAction().invokeBlock(authRequest(Headers(), user), okResponseGenerator))
      metricsMock.defaultRegistry.timer("ppt.returns.upstream.auth.timer").getCount should be > 1L
    }

    "process request when enrolment id is present and allowed" in {
      val enrolmentId = "555"
      val user        = PptTestData.newUser("123", Some(pptEnrolment(enrolmentId)))
      authorizedUser(user)

      await(
        createAuthAction(new PptReferenceAllowedList(Seq(enrolmentId))).invokeBlock(
          authRequest(Headers(), user),
          okResponseGenerator
        )
      ) mustBe Results.Ok
    }

    "redirect to home when enrolment id is present but not allowed" in {
      val user = PptTestData.newUser("123", Some(pptEnrolment("555")))
      authorizedUser(user)

      val result =
        createAuthAction(new PptReferenceAllowedList(Seq("someOtherEnrolmentId"))).invokeBlock(
          authRequest(Headers(), user),
          okResponseGenerator
        )

      redirectLocation(result) mustBe Some(homeRoutes.UnauthorisedController.onPageLoad().url)
    }
  }
}
