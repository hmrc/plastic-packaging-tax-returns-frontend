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

package controllers.changeGroupLead

import org.mockito.ArgumentMatchers.refEq
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar.{mock, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import play.api.http.Status.OK
import play.api.i18n.MessagesApi
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, status, stubMessagesControllerComponents}
import play.twirl.api.Html
import views.html.changeGroupLead.NewGroupLeadConfirmationView

import scala.concurrent.ExecutionContext.global

class NewGroupLeadConfirmationControllerSpec extends PlaySpec with BeforeAndAfterEach {

  private val controllerComponents = stubMessagesControllerComponents()
  private val mockMessagesApi: MessagesApi = mock[MessagesApi]
  private val mockView = mock[NewGroupLeadConfirmationView]

  val sut = new NewGroupLeadConfirmationController(
    messagesApi = mockMessagesApi,
    controllerComponents = controllerComponents,
    view = mockView)(global)

  "onPageLoad" must {
    "present the view" in {
      val req = FakeRequest()
      when(mockView.apply()(any, any)).thenReturn(Html("expected"))
      val result = sut.onPageLoad()(req)

      status(result) mustBe OK
      contentAsString(result) mustBe "expected"
      verify(mockView).apply()(any, any)
    }
  }

}
