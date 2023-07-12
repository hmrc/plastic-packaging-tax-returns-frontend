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
import connectors.{CacheConnector, CalculateCreditsConnector, DownstreamServiceError}
import controllers.actions.JourneyAction
import factories.CreditSummaryListFactory
import models.Mode.{CheckMode, NormalMode}
import models.requests.DataRequest
import models.returns.{CreditRangeOption, TaxReturnObligation}
import models.{CreditBalance, TaxablePlastic, UserAnswers}
import navigation.ReturnsJourneyNavigator
import org.mockito.ArgumentMatchersSugar.{any, eqTo}
import org.mockito.MockitoSugar
import org.mockito.integrations.scalatest.ResetMocksAfterEachTest
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import play.api.i18n.MessagesApi
import play.api.libs.json.JsPath
import play.api.mvc.{AnyContent, Call}
import play.api.test.Helpers._
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow, Value}
import util.EdgeOfSystem
import views.html.returns.credits.ConfirmPackagingCreditView

import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ConfirmPackagingCreditControllerSpec
  extends PlaySpec
    with MockitoSugar
    with JourneyActionAnswer
    with BeforeAndAfterEach
    with ResetMocksAfterEachTest {

  private val creditBalance = CreditBalance(10, 5, 500, true, Map("year-key" -> TaxablePlastic(1, 2, 0.30)))
  private val answer = mock[UserAnswers]
  private val request = mock[DataRequest[AnyContent]]
  private val mockCalculateCreditConnector = mock[CalculateCreditsConnector]
  private val mockMessagesApi: MessagesApi = mock[MessagesApi]
  private val controllerComponents = stubMessagesControllerComponents()
  private val mockView = mock[ConfirmPackagingCreditView]
  private val cacheConnector = mock[CacheConnector]
  private val returnsJourneyNavigator = mock[ReturnsJourneyNavigator]
  private val edgeOfSystem = mock[EdgeOfSystem]
  private val journeyAction = mock[JourneyAction]
  private val creditSummaryListFactory = mock[CreditSummaryListFactory]
  private val creditRangeOption = CreditRangeOption(LocalDate.of(2023, 4, 1), LocalDate.of(2024, 3, 31))

  private val sut = new ConfirmPackagingCreditController(
    mockMessagesApi,
    mockCalculateCreditConnector,
    journeyAction,
    controllerComponents,
    mockView,
    cacheConnector, 
    returnsJourneyNavigator,
    creditSummaryListFactory
  )

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    when(request.userAnswers).thenReturn(answer)
    when(request.pptReference).thenReturn("123")
    when(journeyAction.async(any)).thenAnswer(byConvertingFunctionArgumentsToFutureAction)
    when(edgeOfSystem.localDateTimeNow) thenReturn LocalDateTime.of(2022, 4, 1, 12, 1, 0)
    when(creditSummaryListFactory.createSummaryList(any, any, any)(any)) thenReturn Seq()
    when(request.userAnswers.getOrFail[String](eqTo(JsPath \ "credit" \ "year-key" \ "fromDate"))(any, any)).thenReturn("2023-04-01")
    when(request.userAnswers.getOrFail[String](eqTo(JsPath \ "credit" \ "year-key" \ "toDate"))(any, any)).thenReturn("2024-03-31")

    val aDate = LocalDate.of(2000, 1, 2)
    when(request.userAnswers.get(eqTo(ReturnObligationCacheable))(any)) thenReturn Some(
      TaxReturnObligation(aDate, aDate, aDate, "period-key")
    )

    when(mockView.apply(any, any, any, any, any, any)(any,any)).thenReturn(Html("correct view"))
    when(mockCalculateCreditConnector.getEventually(any)(any)) thenReturn Future.successful(creditBalance)
  }


  "onPageLoad" should {

    "use the journey action" in {
      sut.onPageLoad("year-key", NormalMode)
      verify(journeyAction).async(any)
    }

    "return OK" in {
      val result = sut.onPageLoad("year-key", NormalMode)(request)
      status(result) mustBe OK
    }

    "display a view" in {
      val summaryList = Seq(SummaryListRow(Key(Text("key")), Value(Text("value"))))
      when(creditSummaryListFactory.createSummaryList(any, any, any)(any)).thenReturn(summaryList)
      when(edgeOfSystem.localDateTimeNow) thenReturn LocalDateTime.of(2023, 3, 31, 23, 59, 59) // One sec before midnight

      await(sut.onPageLoad("year-key", NormalMode)(request))
      verify(mockView).apply(eqTo("year-key"), eqTo(BigDecimal(2)), eqTo(summaryList), any, any, eqTo(creditRangeOption))(any, any)
    }

    "pass a summary list to view with the data" in {
      val summaryList = Seq(SummaryListRow(Key(Text("key")), Value(Text("value"))))

      when(creditSummaryListFactory.createSummaryList(any, any, any)(any)).thenReturn(summaryList)
      when(edgeOfSystem.localDateTimeNow) thenReturn LocalDateTime.of(2023, 3, 31, 23, 59, 59) // One sec before midnight
      await(sut.onPageLoad("year-key", NormalMode)(request))

      verify(creditSummaryListFactory).createSummaryList(eqTo(TaxablePlastic(1, 2, 0.30)), eqTo("year-key"), any)(any)
      verify(creditSummaryListFactory).createSummaryList(eqTo(TaxablePlastic(1, 2, 0.30)), eqTo("year-key"), eqTo(answer))(any)
    }


    "return the ConfirmPackagingCreditView with the credit amount on page loading" when {

      val call = Call("Hi", "You")

      "total requested credit is less than available credit - (NormalMode)" in {
        when(returnsJourneyNavigator.confirmCredit(any)) thenReturn call
        await(sut.onPageLoad("year-key", NormalMode)(request))
        verify(returnsJourneyNavigator).confirmCredit(NormalMode)
        verify(mockView).apply(eqTo("year-key"), eqTo(BigDecimal(2)), any, eqTo(call), eqTo(NormalMode), eqTo(creditRangeOption))(any,any)
      }

      "total requested credit is less than available credit - (CheckMode)" in {
        when(returnsJourneyNavigator.confirmCredit(any)) thenReturn call
        await(sut.onPageLoad("year-key", CheckMode)(request))
        verify(returnsJourneyNavigator).confirmCredit(CheckMode)
        verify(mockView).apply(eqTo("year-key"), eqTo(BigDecimal(2)), any, eqTo(call), eqTo(CheckMode), eqTo(creditRangeOption))(any,any)
      }
    }

    "return an error page" in {
      when(mockCalculateCreditConnector.getEventually(any)(any)) thenReturn Future.failed(
        DownstreamServiceError("Error", new Exception("error"))
      )
      val result = sut.onPageLoad("year-key", NormalMode)(request)
      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad.url
    }
  }
}
