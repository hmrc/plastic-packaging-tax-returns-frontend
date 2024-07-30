/*
 * Copyright 2024 HM Revenue & Customs
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

import base.SpecBase
import models.{SignedInUser, UserAnswers}
import models.requests.{DataRequest, IdentifiedRequest, IdentityData, OptionalDataRequest}
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.when
import org.scalatest.EitherValues
import play.api.http.Status.SEE_OTHER
import play.api.mvc.{AnyContent, Request, Result}
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.FakeRequest
import play.api.test.Helpers.LOCATION
import support.PptTestData.pptEnrolment

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DataRequiredActionSpec extends SpecBase with EitherValues {

  val request: Request[AnyContent] = FakeRequest().withCSRFToken

  val userAnswer: UserAnswers = UserAnswers("id")
  class Harness extends DataRequiredActionImpl {

    def actionRefine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] = refine(request)

  }

  val pptLoggedInUser: SignedInUser = SignedInUser(pptEnrolment("123"), IdentityData(internalId = "SomeId"))
  val identifierRequest: IdentifiedRequest[AnyContent] =
    IdentifiedRequest(request, pptLoggedInUser, "some-ppt-ref")

  "DataRequiredAction" - {
    "must redirect to the JourneyRecoveryController when userAnswers is None and a journey has not been completed" in {

      when(mockSessionRepo.get[Boolean](any, any)(any)).thenReturn(Future.successful(Some(false)))
      val harness = new Harness
      val result  = harness.actionRefine(OptionalDataRequest(identifierRequest, None)).futureValue.left.value.header
      result.status mustBe SEE_OTHER
      result.headers.get(LOCATION) mustBe Some("/plastic-packaging-tax/problem-with-service")
    }

    "must redirect to the ApplicationCompleteController when userAnswers is None but a journey has already been completed" in {
      when(mockSessionRepo.get[Boolean](any, any)(any)).thenReturn(Future.successful(Some(true)))
      val harness = new Harness
      val result  = harness.actionRefine(OptionalDataRequest(identifierRequest, None)).futureValue.left.value.header
      result.status mustBe SEE_OTHER
      result.headers.get(LOCATION) mustBe Some("/plastic-packaging-tax/application-complete")
    }

    "must return userAnswers when UserAnswers data exist" in {
      val harness = new Harness
      val result  = harness.actionRefine(OptionalDataRequest(identifierRequest, Some(userAnswer))).futureValue
      result.isRight mustBe true
    }
  }

}
