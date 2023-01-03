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

package controllers.payments

import akka.stream.testkit.NoMaterializer
import base.FakeIdentifierActionWithEnrolment
import config.FrontendAppConfig
import connectors.DirectDebitConnector
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.mockito.MockitoSugar.mock
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import play.api.http.Status.SEE_OTHER
import play.api.i18n.MessagesApi
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, _}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class DirectDebitControllerSpec extends PlaySpec with BeforeAndAfterEach {

  private val mockMessagesApi: MessagesApi = mock[MessagesApi]
  private val mockDirectDebitConnector: DirectDebitConnector = mock[DirectDebitConnector]
  private val controllerComponents = stubMessagesControllerComponents()
  private val mockAppConfig = mock[FrontendAppConfig]

  private val sut = new DirectDebitController(
    mockMessagesApi,
    mockDirectDebitConnector,
    new FakeIdentifierActionWithEnrolment(stubPlayBodyParsers(NoMaterializer)),
    controllerComponents,
    mockAppConfig
  )

  override def beforeEach(): Unit = {
    super.beforeEach()
    when(mockDirectDebitConnector.getDirectDebitLink(any(), any())(any()))
      .thenReturn(Future.successful("something"))
  }

  "DirectDebitController" should {
    "redirectLink" when {
      "redirect to enter email address page" in {
        val result = sut.redirectLink(FakeRequest(GET, "/foo"))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("something")
        }
      }

      "call the direct debit connector" in {
        when(mockAppConfig.returnUrl(any())).thenReturn("any-url")
        await(sut.redirectLink(FakeRequest(GET, "/foo")))

        verify(mockDirectDebitConnector).getDirectDebitLink(
          ArgumentMatchers.eq("123"),
          ArgumentMatchers.eq("any-url"))(any())
      }
  }
}
