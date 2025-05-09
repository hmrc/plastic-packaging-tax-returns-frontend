/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers

import forms.AgentsFormProvider
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import uk.gov.hmrc.auth.core.AuthConnector

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class AgentSelectPPTRefSearchControllerSpec extends PlaySpec with BeforeAndAfterEach {

  val formProvider = mock[AgentsFormProvider]

  val mockAuthConnector     = mock[AuthConnector]
  val mockSessionRepository = mock[SessionRepository]

  val sut = new AgentSelectPPTRefSearchController(
    mockAuthConnector,
    stubMessagesControllerComponents().messagesApi,
    stubMessagesControllerComponents(),
    mockSessionRepository
  )(global)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAuthConnector, mockSessionRepository)
  }

  val validInternalId   = "Int-123"
  val validPPTReference = "123456"

  "get" must {
    "return valid PPT reference" in {
      when(mockAuthConnector.authorise[Option[String]](any(), any())(any(), any()))
        .thenReturn(Future.successful(Some(validInternalId)))
      when(mockSessionRepository.get[String](any(), any())(any())).thenReturn(
        Future.successful(Some(validPPTReference))
      )
      val result: Future[Result] = sut.get()(FakeRequest())
      status(result) mustBe OK
      contentAsJson(result) mustBe Json.obj("clientPPT" -> validPPTReference)
    }

    "error" when {
      "InternalId does not exist" in {
        when(mockAuthConnector.authorise[Option[String]](any(), any())(any(), any()))
          .thenReturn(Future.successful(None))
        val result: Future[Result] = sut.get()(FakeRequest())
        status(result) mustBe NOT_FOUND
      }

      "PPT reference does not exist" in {
        when(mockAuthConnector.authorise[Option[String]](any(), any())(any(), any()))
          .thenReturn(Future.successful(Some(validInternalId)))
        when(mockSessionRepository.get[String](any(), any())(any())).thenReturn(Future.successful(None))
        val result: Future[Result] = sut.get()(FakeRequest())
        status(result) mustBe NOT_FOUND
      }

      "auth fails" in {
        object TestException extends Exception("test")
        when(mockAuthConnector.authorise[Unit](any(), any())(any(), any()))
          .thenReturn(Future.failed(TestException))

        intercept[TestException.type](await(sut.get()(FakeRequest())))
      }
    }
  }

}
