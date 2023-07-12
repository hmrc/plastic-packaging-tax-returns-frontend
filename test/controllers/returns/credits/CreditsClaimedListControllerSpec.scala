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

package controllers.returns.credits

import base.utils.JourneyActionAnswer
import cacheables.ReturnObligationCacheable
import connectors.{AvailableCreditYearsConnector, CalculateCreditsConnector, DownstreamServiceError}
import controllers.BetterMockActionSyntax
import controllers.actions.JourneyAction
import forms.returns.credits.CreditsClaimedListFormProvider
import models.Mode.NormalMode
import models.requests.DataRequest
import models.returns.{CreditRangeOption, TaxReturnObligation}
import models.returns.credits.CreditSummaryRow
import models.{CreditBalance, TaxablePlastic}
import navigation.ReturnsJourneyNavigator
import org.mockito.Answers
import org.mockito.ArgumentMatchersSugar.{any, eqTo}
import org.mockito.MockitoSugar.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.enablers.Messaging
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.data.Form
import play.api.data.Forms.boolean
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.json.{JsObject, JsPath}
import play.api.mvc.{AnyContent, Call, RequestHeader}
import play.api.test.Helpers._
import play.twirl.api.Html
import queries.Gettable
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.ActionItem
import views.html.returns.credits.CreditsClaimedListView

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreditsClaimedListControllerSpec
  extends PlaySpec
    with JourneyActionAnswer
    with MockitoSugar
    with BeforeAndAfterEach {

  private val messages = mock[Messages]
  private val request = mock[DataRequest[AnyContent]](Answers.RETURNS_DEEP_STUBS)
  private val messagesApi = mock[MessagesApi]
  private val navigator = mock[ReturnsJourneyNavigator]
  private val journeyAction = mock[JourneyAction]
  private val formProvider = mock[CreditsClaimedListFormProvider]
  private val view = mock[CreditsClaimedListView]
  private val calcCreditsConnector = mock[CalculateCreditsConnector]
  private val mockAvailableCreditYearsConnector = mock[AvailableCreditYearsConnector]

  private val creditBalance = CreditBalance(10, 20, 5L, true, Map(
    s"${LocalDate.now()}-${LocalDate.now()}" -> TaxablePlastic(0, 20, 0)
  ))

  private val availableOptions = Seq(CreditRangeOption(LocalDate.now(), LocalDate.now()))
  
  // Resolves an ambiguity in implicits for Messaging used by test framework
  private implicit val resolveImplicitAmbiguity: Messaging[DownstreamServiceError] = Messaging.messagingNatureOfThrowable

  private val sut = new CreditsClaimedListController(
    messagesApi,
    calcCreditsConnector,
    mockAvailableCreditYearsConnector,
    navigator,
    journeyAction,
    formProvider,
    stubMessagesControllerComponents(),
    view
  )

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(view, request, navigator, journeyAction, calcCreditsConnector)

    when(view.apply(any, any, any, any, any, any)(any, any)).thenReturn(Html("correct view"))
    when(journeyAction.apply(any)).thenAnswer(byConvertingFunctionArgumentsToAction)
    when(journeyAction.async(any)).thenAnswer(byConvertingFunctionArgumentsToFutureAction)

    when(request.pptReference).thenReturn("123")
    when(request.userAnswers.getOrFail[String](any[JsPath])(any, any)).thenReturn("2023-04-01")
    when(request.userAnswers.get[Map[String, JsObject]](any[JsPath])(any)).thenReturn(None)
    
    val aDate = LocalDate.of(2000, 1, 2)
    when(request.userAnswers.get(eqTo(ReturnObligationCacheable))(any)) thenReturn Some(
      TaxReturnObligation(aDate, aDate, aDate, "period-key")
    )

    when(calcCreditsConnector.getEventually(any)(any)) thenReturn Future.successful(creditBalance)
    when(calcCreditsConnector.getEventually(any)(any)) thenReturn Future.successful(creditBalance)

    when(mockAvailableCreditYearsConnector.get(any)(any)).thenReturn(Future.successful(availableOptions))
  }

  "onPageLoad" should {

    "use the journey action" in {
      sut.onPageLoad(NormalMode)
      verify(journeyAction).async(any)
    }

    "return 200" in {
      setUpMock()

      val result = sut.onPageLoad(NormalMode)(request)

      status(result) mustBe OK
    }

    "return a view" in {
      val boundForm = mock[Form[Boolean]]
      when(formProvider.apply(any)(any)).thenReturn(boundForm)
      setUpMock()

      await(sut.onPageLoad(NormalMode)(request))

      verify(view).apply(eqTo(boundForm), eqTo(creditBalance), any, eqTo(availableOptions), eqTo(expectedCreditSummary), eqTo(NormalMode))(any,any)
    }

    "getting the total weight in pound from API" in {
      setUpMock()
      await(sut.onPageLoad(NormalMode)(request))
      verify(calcCreditsConnector).getEventually(eqTo("123"))(any)
    }

    "should throw if API return an error" in {
      setUpMock()
      when(calcCreditsConnector.getEventually(any)(any)) thenReturn Future.failed(
        DownstreamServiceError("error", new Exception("inner"))
      )
      the [DownstreamServiceError] thrownBy await(sut.onPageLoad(NormalMode)(request)) must have message "error"
    }

    "calculate if all years are being claimed" in {
      setUpMock()
      val alreadyUsed = CreditRangeOption(LocalDate.now(), LocalDate.now())
      when(request.userAnswers.getOrFail(any[Gettable[Any]])(any, any)).thenReturn(Seq(alreadyUsed))
      when(request.userAnswers.get[Map[String, JsObject]](any[JsPath])(any))
        .thenReturn(Some(Map(alreadyUsed.key -> JsObject.empty)))

      await(sut.onPageLoad(NormalMode)(request))

      verify(view).apply(any, eqTo(creditBalance), any, eqTo(Seq.empty), any, any)(any, any)
    }
  }

  "onSubmit" should {
    "invoke the journey action" in {
      sut.onSubmit(NormalMode)
      verify(journeyAction).async(any)
    }

    "redirect" in {
      when(mockAvailableCreditYearsConnector.get(any)(any)).thenReturn(Future.successful(availableOptions))
      when(request.userAnswers.get[Map[String, JsObject]](any[JsPath])(any)).thenReturn(None)
      val form = mock[Form[Boolean]]
      val boundForm = Form("value" -> boolean).fill(true)
      when(formProvider.apply(any)(any)).thenReturn(form)
      when(form.bindFromRequest()(any, any)).thenReturn(boundForm)
      when(navigator.creditClaimedList(any, any,any)).thenAnswer(Call(GET, "/foo"))

      val result = sut.onSubmit(NormalMode).skippingJourneyAction(request)

      status(result) mustBe SEE_OTHER

      withClue("call navigator") {
        verify(navigator).creditClaimedList(NormalMode, true, request.userAnswers)
      }
    }

    "error" when {
      "error on form" in {
        setUpMock()
        val form = mock[Form[Boolean]]
        val boundForm = Form("value" -> boolean).withError("error", "error message")
        when(formProvider.apply(any)(any)).thenReturn(form)
        when(form.bindFromRequest()(any, any)).thenReturn(boundForm)

        val result = sut.onSubmit(NormalMode).skippingJourneyAction(request)

        status(result) mustBe BAD_REQUEST
        verify(view).apply(eqTo(boundForm), any, any, any, eqTo(expectedCreditSummary), eqTo(NormalMode))(any, any)
      }
    }
  }

  private def expectedCreditSummary = {
    Seq(
      CreditSummaryRow("return.quarter", "£20.00", Seq(
        ActionItem("/change", Text("site.change"), visuallyHiddenText = Some("creditSummary.for")),
        ActionItem("/remove", Text("site.remove"), visuallyHiddenText = Some("creditSummary.for"))
      )),
      CreditSummaryRow("creditsSummary.table.total", "£20.00")
    )
  }

  private def setUpMock(): Unit = {
    when(navigator.creditSummaryChange(any)).thenReturn("/change")
    when(navigator.creditSummaryRemove(any)).thenReturn("/remove")
    when(messagesApi.preferred(any[RequestHeader])).thenReturn(messages)
   // when(messages.apply(any[String])).thenAnswer((s: String) => s)
    when(messages.apply(any[String], any)).thenAnswer((s: String) => s)

  }
}
