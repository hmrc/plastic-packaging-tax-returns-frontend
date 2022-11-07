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

package controllers.changeGroupLead

import akka.stream.testkit.NoMaterializer
import base.FakeIdentifierActionWithEnrolment
import config.FrontendAppConfig
import connectors.{CacheConnector, DirectDebitConnector, SubscriptionConnector}
import controllers.actions.JourneyAction
import controllers.actions.JourneyAction.{RequestAsyncFunction, RequestFunction}
import controllers.payments.DirectDebitController
import forms.changeGroupLead.SelectNewGroupLeadForm
import models.{SignedInUser, UserAnswers}
import models.requests.{DataRequest, IdentifiedRequest, IdentityData}
import models.subscription.group.GroupPartnershipSubscription
import models.subscription.{CustomerDetails, CustomerType, LegalEntityDetails, PrincipalPlaceOfBusinessDetails}
import models.subscription.subscriptionDisplay.SubscriptionDisplayResponse
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.mock
import org.scalatestplus.play.PlaySpec
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, BodyParser, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.mvc.Action
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.auth.core.Enrolments
import views.html.changeGroupLead.ChooseNewGroupLeadView

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.global

class ChooseNewGroupLeadControllerSpec extends PlaySpec {

  private val mockMessagesApi: MessagesApi = mock[MessagesApi]
  private val controllerComponents = stubMessagesControllerComponents()
  private val mockAppConfig = mock[FrontendAppConfig]
  private val mockView = mock[ChooseNewGroupLeadView]
  private val mockFormProvider = mock[SelectNewGroupLeadForm]
  private val mockCache = mock[CacheConnector]
  private val mockSubscriptionConnector = mock[SubscriptionConnector]


//  //todo hmmmmmmmmmmmm mock? but its gross :/
//  object TestJourneyAction extends JourneyAction {
//    override def async(function: RequestAsyncFunction): Action[AnyContent] = new Action[AnyContent] {
//      override def parser: BodyParser[AnyContent] = controllerComponents.parsers.default
//      override def apply(request: Request[AnyContent]): Future[Result] =
//        function(
//          DataRequest(
//            IdentifiedRequest(FakeRequest(), SignedInUser(Enrolments(Set.empty), IdentityData("")), Some("ppt-ref")),
//            UserAnswers("useranswersId")
//          )
//        )
//      override def executionContext: ExecutionContext = global
//    }
//    override def apply(function: RequestFunction): Action[AnyContent] = ???
//  }

  object TestGuard extends FeatureGuard(mockAppConfig){
    override def check(): Unit = ()
  }

//  val sut = new ChooseNewGroupLeadController(
//    mockMessagesApi,
//    ???,
//    controllerComponents,
//    mockView,
//    mockFormProvider,
//    mockCache,
//    TestGuard,
//    ???
//  )(global)

  //todo pan wip
//  "onPageLoad" must {
//    "feature guard" in {
//      //todo
//    }
//
//    "return the view with the form populated with the returned members" in {
//      when(mockView.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.raw("test view"))
//
//      val result: Future[Result] = sut.onPageLoad()(FakeRequest())
//      status(result) mustBe OK
//      contentAsString(result) mustBe "test view"
//    }
//  }

}
