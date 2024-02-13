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

package controllers.amends

import org.apache.pekko.stream.testkit.NoMaterializer
import base.FakeIdentifierActionWithEnrolment
import org.mockito.ArgumentMatchers.refEq
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play.PlaySpec
import play.api.http.Status.OK
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, contentAsString, defaultAwaitTimeout, status, stubMessagesControllerComponents, stubPlayBodyParsers}
import play.twirl.api.Html
import repositories.SessionRepository
import repositories.SessionRepository.Paths
import views.html.amends.AmendConfirmation

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class AmendConfirmationControllerSpec extends PlaySpec
  with BeforeAndAfterEach{

  private val sessionRepository = mock[SessionRepository]
  private val view = mock[AmendConfirmation]

  private val sut = new AmendConfirmationController(
    stubMessagesControllerComponents(),
    new FakeIdentifierActionWithEnrolment(stubPlayBodyParsers(NoMaterializer)),
    sessionRepository,
    view
  )(global)

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(view, sessionRepository)

    when(view.apply(any)(any, any)).thenReturn(Html("correct view"))
  }

  "onPageLoad" should {
    "return 200" in {
      when(sessionRepository.get[String](any, any)(any)).thenReturn(Future.successful(Some("charge-ref")))

      val result = sut.onPageLoad()(FakeRequest())

      status(result) mustEqual OK
      contentAsString(result) mustBe "correct view"
      verify(view).apply(refEq(Some("charge-ref")))(any, any)
      verify(sessionRepository).get(refEq("SomeId-123"), refEq(Paths.AmendChargeRef))(any)
    }

    "error" in {
      object TestEx extends Exception("boom")

      when(sessionRepository.get[String](any, any)(any)).thenReturn(Future.failed(TestEx))

      intercept[TestEx.type](await(sut.onPageLoad()(FakeRequest())))
      verify(sessionRepository).get(refEq("SomeId-123"), refEq(Paths.AmendChargeRef))(any)
    }
  }


}