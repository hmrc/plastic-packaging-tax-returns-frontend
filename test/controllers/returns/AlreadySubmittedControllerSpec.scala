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

package controllers.returns

import org.apache.pekko.stream.testkit.NoMaterializer
import base.FakeIdentifierActionWithEnrolment
import models.returns.TaxReturnObligation
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.{verify, when}
import org.mockito.MockitoSugar.mock
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import play.api.http.Status.OK
import play.api.test.FakeRequest
import play.api.test.Helpers.{defaultAwaitTimeout, status, stubMessagesControllerComponents, stubPlayBodyParsers}
import play.twirl.api.HtmlFormat
import repositories.SessionRepository
import views.html.returns.AlreadySubmittedView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AlreadySubmittedControllerSpec extends PlaySpec with BeforeAndAfterEach {

  private val mockSessionRepository = mock[SessionRepository]
  private val mockView = mock[AlreadySubmittedView]

  private val sut = new AlreadySubmittedController(
    stubMessagesControllerComponents(),
    new FakeIdentifierActionWithEnrolment(stubPlayBodyParsers(NoMaterializer)),
    mockSessionRepository,
    mockView
  )

  override def beforeEach(): Unit = {
    super.beforeEach()

    when(mockView.apply(any())(any(),any())).thenReturn(HtmlFormat.empty)
  }

  "onPageLoad" should {
    "return view" in {

      val mockTaxReturn = mock[TaxReturnObligation]
      when(mockTaxReturn.toReturnQuarter(any())).thenReturn("April to June 2022")

      when(mockSessionRepository.get[Any](any(), any())(any())).thenReturn(
        Future.successful(Some(mockTaxReturn))
      )

      val result = sut.onPageLoad()(FakeRequest("GET", "/foo"))

      status(result) mustEqual OK
      verify(mockView).apply(ArgumentMatchers.eq("April to June 2022"))(any(), any())
    }
  }

}
