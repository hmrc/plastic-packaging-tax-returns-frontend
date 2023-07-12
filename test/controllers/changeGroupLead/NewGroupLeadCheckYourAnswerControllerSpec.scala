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

import connectors.SubscriptionConnector
import controllers.BetterMockActionSyntax
import controllers.actions.JourneyAction
import controllers.actions.JourneyAction.{RequestAsyncFunction, RequestFunction}
import models.UserAnswers
import models.changeGroupLead.NewGroupLeadAddressDetails
import models.requests.DataRequest
import models.subscription.Member
import navigation.ChangeGroupLeadNavigator
import org.mockito.ArgumentMatchers.refEq
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar.{mock, reset, verify, when}
import org.mockito.{Answers, ArgumentCaptor, ArgumentMatchers}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import pages.changeGroupLead.{ChooseNewGroupLeadPage, NewGroupLeadEnterContactAddressPage}
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, RequestHeader}
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, defaultAwaitTimeout, redirectLocation, status, stubMessagesControllerComponents}
import play.twirl.api.HtmlFormat
import queries.Gettable
import services.CountryService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.http.HttpResponse
import viewmodels.checkAnswers.changeGroupLead.ChooseNewGroupLeadSummary
import views.html.changeGroupLead.NewGroupLeadCheckYourAnswerView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class NewGroupLeadCheckYourAnswerControllerSpec extends PlaySpec with BeforeAndAfterEach{

  private val view: NewGroupLeadCheckYourAnswerView = mock[NewGroupLeadCheckYourAnswerView]
  private val journeyAction = mock[JourneyAction]
  private val dataRequest = mock[DataRequest[AnyContent]](Answers.RETURNS_DEEP_STUBS)
  private val messagesApi = mock[MessagesApi]
  private val messages = mock[Messages]
  private val navigator = mock[ChangeGroupLeadNavigator]
  private val subscriptionConnector = mock[SubscriptionConnector]
  private val countryService = mock[CountryService]

  private val sut = new NewGroupLeadCheckYourAnswerController(
    messagesApi,
    journeyAction,
    countryService,
    subscriptionConnector,
    stubMessagesControllerComponents(),
    view,
    navigator
  )


  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(view, journeyAction, dataRequest, navigator, subscriptionConnector)

    when(view.apply(any)(any, any)).thenReturn(HtmlFormat.empty)
    when(journeyAction.apply(any)) thenAnswer byConvertingFunctionArgumentsToAction
    when(journeyAction.async(any)) thenAnswer byConvertingFunctionArgumentsToFutureAction
    when(messagesApi.preferred(any[RequestHeader])) thenReturn messages
    when(navigator.checkYourAnswers) thenReturn Call("go", "over-there")
    when(subscriptionConnector.changeGroupLead(any)(any)).thenReturn(Future.successful(HttpResponse(OK, "done")))
  }

  def byConvertingFunctionArgumentsToAction: (RequestFunction) => Action[AnyContent] = (function: RequestFunction) =>
    when(mock[Action[AnyContent]].apply(any))
      .thenAnswer((request: DataRequest[AnyContent]) =>
        Future.successful(function(request)))
      .getMock[Action[AnyContent]]

  def byConvertingFunctionArgumentsToFutureAction: (RequestAsyncFunction) => Action[AnyContent] = (function: RequestAsyncFunction) =>
    when(mock[Action[AnyContent]].apply(any))
      .thenAnswer((request: DataRequest[AnyContent]) =>
        function(request))
      .getMock[Action[AnyContent]]

  
  "onPageLoad" should {
    "return OK" in {
      when(dataRequest.userAnswers.get(ArgumentMatchers.eq(ChooseNewGroupLeadPage))(any))
        .thenReturn(None)
      val result = sut.onPageLoad.skippingJourneyAction(dataRequest)

      status(result) mustBe OK
    }

    "use the journey action" in {
      when(journeyAction.apply(any)) thenReturn mock[Action[AnyContent]]

      sut.onPageLoad(FakeRequest())

      verify(journeyAction).apply(any)
    }

    "construct the summary list and pass it to the view" in {
      when(dataRequest.userAnswers.get(any[Gettable[Any]])(any)).thenReturn(Some(Member("Blah", "1")), None)

      val definedRow = ChooseNewGroupLeadSummary.row(UserAnswers("").setOrFail(ChooseNewGroupLeadPage, Member("Blah", "1"))
      )(messages).get

      await(sut.onPageLoad.skippingJourneyAction(dataRequest))

      verifyAndCaptureValue mustBe Seq(definedRow)
    }

    "view display address containing countryCode as value" in {
      val countryCode = "GB"
      when(countryService.tryLookupCountryName(ArgumentMatchers.eq(countryCode))(any)).thenReturn("United Kingdom")
      when(dataRequest.userAnswers.get(ArgumentMatchers.eq(ChooseNewGroupLeadPage))(any))
        .thenReturn(Some(Member("Blah", "1")))

      when(dataRequest.userAnswers.get(ArgumentMatchers.eq(NewGroupLeadEnterContactAddressPage))(any))
        .thenReturn(Some(createAddressDetails(countryCode)))

      await(sut.onPageLoad.skippingJourneyAction(dataRequest))

      verifyAndCaptureValue.last.value.content.asHtml.body must include("United Kingdom")
    }
  }

  private def createAddressDetails(countryCode: String) = {
    NewGroupLeadAddressDetails(
      addressLine1 = "line1",
      addressLine2 = "line1",
      addressLine3 = None,
      addressLine4 = Some("Line4"),
      postalCode = Some("NE5 4DL"),
      countryCode = countryCode
    )

  }

  "onSubmit" should {
    
    "use the journey action" in {
      when(journeyAction.async(any)) thenReturn mock[Action[AnyContent]]
      sut.onSubmit(FakeRequest())
      verify(journeyAction).async(any)
    }
    
    "redirect via the navigator" in {
      val result = sut.onSubmit.skippingJourneyAction(dataRequest)
      await(result)
      verify(navigator).checkYourAnswers
      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual "over-there"
    }

    "call the subscription change group lead" in {
      await(sut.onSubmit.skippingJourneyAction(dataRequest))
      verify(subscriptionConnector).changeGroupLead(refEq(dataRequest.pptReference))(any)
    }
  }
  
  
  private def verifyAndCaptureValue: Seq[SummaryListRow] = {
    val captor = ArgumentCaptor.forClass(classOf[Seq[SummaryListRow]])

    verify(view).apply(captor.capture())(any, any)
    captor.getValue
  }

}
