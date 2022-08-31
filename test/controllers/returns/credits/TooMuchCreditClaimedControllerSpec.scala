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

package controllers.returns.credits


import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.{SignedInUser, UserAnswers}
import models.requests.{DataRequest, IdentifiedRequest, OptionalDataRequest}
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.mvc.{AnyContent, BodyParser, Request, Result}
import play.api.test.{FakeRequest, Helpers}
import play.api.test.Helpers.stubMessagesApi
import views.html.returns.credits.TooMuchCreditClaimedView
import play.api.test.Helpers._
import play.twirl.api.Html

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.global

class TooMuchCreditClaimedControllerSpec extends PlaySpec with MockitoSugar {

  val mockView: TooMuchCreditClaimedView = mock[TooMuchCreditClaimedView]
  val expectedHtml: Html = Html.apply("<p>expected</p>")
  when(mockView.apply()(any(), any())).thenReturn(expectedHtml)

  val sut = new TooMuchCreditClaimedController(
    stubMessagesApi(),
    new FakeIDAction(mock[SignedInUser]),
    new FakeDataRetrievalAction(UserAnswers("id")),
    FakeDataRequiredAction,
    Helpers.stubMessagesControllerComponents(),
    mockView
  )(global)

  "onPageLoad" must {
    "return the view" in {
      val result =  sut.onPageLoad()(FakeRequest())

      status(result) mustBe OK
      contentAsString(result) mustBe expectedHtml.toString()
      verify(mockView).apply()(any(), any())
    }
  }


  //todo move this let other controllers use it
  //this way controllers dont have to be IT spec like

  class FakeIDAction(signedInUser: SignedInUser) extends IdentifierAction {
    override def parser: BodyParser[AnyContent] = Helpers.stubBodyParser()

    override def invokeBlock[A](request: Request[A], block: IdentifiedRequest[A] => Future[Result]): Future[Result] =
      block(IdentifiedRequest(request, signedInUser, Some("pptReference")))

    override protected def executionContext: ExecutionContext = global
  }

  class FakeDataRetrievalAction(userAnswers: UserAnswers) extends DataRetrievalAction {
    override protected def transform[A](request: IdentifiedRequest[A]): Future[OptionalDataRequest[A]] =
      Future.successful(OptionalDataRequest(request, Some(userAnswers)))

    override protected def executionContext: ExecutionContext = global
  }

  object FakeDataRequiredAction extends DataRequiredAction {
    override protected def executionContext: ExecutionContext = global

    override protected def refine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] =
      Future.successful(Right(DataRequest(request.request, request.userAnswers)))
  }
}
