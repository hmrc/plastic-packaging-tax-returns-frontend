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

package controllers

import akka.stream.testkit.NoMaterializer
import base.FakeAuthAction
import controllers.actions.AuthenticatedIdentifierAction.IdentifierAction.{pptEnrolmentIdentifierName, pptEnrolmentKey}
import forms.AgentsFormProvider
import models.Mode.NormalMode
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.MockitoSugar.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play.PlaySpec
import play.api.data.format.Formats
import play.api.data.{Form, Forms}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import repositories.SessionRepository
import repositories.SessionRepository.Paths.AgentSelectedPPTRef
import uk.gov.hmrc.auth.core.retrieve.EmptyRetrieval
import uk.gov.hmrc.auth.core.{AuthConnector, Enrolment, InsufficientEnrolments}
import views.html.AgentsView

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class AgentsControllerSpec extends PlaySpec with BeforeAndAfterEach {

  val formProvider = mock[AgentsFormProvider]
  val mockForm = mock[Form[String]]
  val view = mock[AgentsView]

  val mockAuthConnector = mock[AuthConnector]
  val mockSessionRepository = mock[SessionRepository]

  val fakeForm: Form[String] = Form[String]("value" -> Forms.of[String](Formats.stringFormat))

  val sut = new AgentsController(
    stubMessagesControllerComponents().messagesApi,
    mockAuthConnector,
    mockSessionRepository,
    new FakeAuthAction(stubPlayBodyParsers(NoMaterializer)),
    formProvider,
    stubMessagesControllerComponents(),
    view
  )(global)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAuthConnector, mockSessionRepository, formProvider, view, mockForm)
    when(view.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.raw("Test View"))
    when(formProvider.apply()).thenReturn(mockForm)
  }

  "onPageLoad" must {
    "display the form" in {
      when(mockSessionRepository.get[Any](any(), any())(any())).thenReturn(Future.successful(None))
      when(mockForm.fill("")).thenReturn(fakeForm)

      val result: Future[Result] = sut.onPageLoad(NormalMode)(FakeRequest())

      status(result) mustBe OK
      contentAsString(result) mustBe "Test View"
      verify(view).apply(refEq(fakeForm), any())(any(), any())
    }

    "display the form pre-populated" in {
      val filledForm = fakeForm.fill("selected-ref")
      when(mockSessionRepository.get[Any](any(), any())(any())).thenReturn(Future.successful(Some("selected-ref")))
      when(mockForm.fill("selected-ref")).thenReturn(filledForm)

      val result: Future[Result] = sut.onPageLoad(NormalMode)(FakeRequest())

      status(result) mustBe OK
      contentAsString(result) mustBe "Test View"
      verify(view).apply(refEq(filledForm), any())(any(), any())
    }
  }

  "onSubmit" must {
    "return BadRequest" when {
      "the form is filled incorrectly" in {
        val erroredForm = fakeForm.withError("key", "message")
        when(mockForm.bindFromRequest()(any(), any())).thenReturn(erroredForm)

        val result: Future[Result] = sut.onSubmit(NormalMode)(FakeRequest())

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe "Test View"
        verify(view).apply(refEq(erroredForm), any())(any(), any())
      }

      "auth rejects the selected identifier" in {
        val filledForm = fakeForm.fill("selected")
        when(mockForm.bindFromRequest()(any(), any())).thenReturn(filledForm)
        when(mockForm.fill(any())).thenReturn(filledForm)
        when(mockAuthConnector.authorise[Unit](any(), any())(any(), any()))
          .thenReturn(Future.failed(InsufficientEnrolments("")))

        val result: Future[Result] = sut.onSubmit(NormalMode)(FakeRequest())

        val customErrorFrom = filledForm.withError("value", "agents.client.identifier.auth.error")

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe "Test View"
        verify(view).apply(refEq(customErrorFrom), any())(any(), any())

        verifyNoInteractions(mockSessionRepository)
        verify(mockAuthConnector).authorise(
          refEq(Enrolment(pptEnrolmentKey)
            .withIdentifier(pptEnrolmentIdentifierName, "selected")
            .withDelegatedAuthRule("ppt-auth")),
          refEq(EmptyRetrieval))(any(), any())
      }
    }

    "redirect to home page" when{
      "The form is filled, auth validates and the ref is cached" in {
        val filledForm = fakeForm.fill("selected")
        when(mockForm.bindFromRequest()(any(), any())).thenReturn(filledForm)
        when(mockAuthConnector.authorise[Unit](any(), any())(any(), any())).thenReturn(Future.unit)
        when(mockSessionRepository.set[Any](any(), refEq(AgentSelectedPPTRef), refEq("selected"))(any())).thenReturn(Future.successful(true))

        val result: Future[Result] = sut.onSubmit(NormalMode)(FakeRequest())

        redirectLocation(result) mustBe Some(controllers.routes.IndexController.onPageLoad.url)
      }
    }

    "error" when {
      "caching fails" in {
        object TestException extends Exception("test")
        val filledForm = fakeForm.fill("selected")
        when(mockForm.bindFromRequest()(any(), any())).thenReturn(filledForm)
        when(mockAuthConnector.authorise[Unit](any(), any())(any(), any())).thenReturn(Future.unit)
        when(mockSessionRepository.set[Any](any(), refEq(AgentSelectedPPTRef), refEq("selected"))(any()))
          .thenReturn(Future.failed(TestException))

        intercept[TestException.type](await(sut.onSubmit(NormalMode)(FakeRequest())))
      }

      "auth fails" in {
        object TestException extends Exception("test")
        val filledForm = fakeForm.fill("selected")
        when(mockForm.bindFromRequest()(any(), any())).thenReturn(filledForm)
        when(mockAuthConnector.authorise[Unit](any(), any())(any(), any()))
          .thenReturn(Future.failed(TestException))

        intercept[TestException.type](await(sut.onSubmit(NormalMode)(FakeRequest())))
      }
    }
  }

}
