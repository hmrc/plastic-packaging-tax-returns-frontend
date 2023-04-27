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
import connectors.{CacheConnector, CalculateCreditsConnector, DownstreamServiceError}
import controllers.BetterMockActionSyntax
import controllers.actions.JourneyAction
import factories.CreditSummaryListFactory
import models.Mode.{CheckMode, NormalMode}
import models.UserAnswers.SaveUserAnswerFunc
import models.requests.DataRequest
import models.{CreditBalance, UserAnswers}
import navigation.ReturnsJourneyNavigator
import org.mockito.ArgumentMatchers.{eq => meq}
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.{ArgumentMatchers, MockitoSugar}
import org.mockito.MockitoSugar.{verify, when}
import org.mockito.integrations.scalatest.ResetMocksAfterEachTest
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import pages.returns.credits.{ConvertedCreditsPage, ExportedCreditsPage, WhatDoYouWantToDoPage}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import queries.{Gettable, Settable}
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow, Value}
import util.EdgeOfSystem
import views.html.returns.credits.ConfirmPackagingCreditView
import org.scalatest.prop.TableDrivenPropertyChecks._

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

class ConfirmPackagingCreditControllerSpec
  extends PlaySpec
    with MockitoSugar
    with JourneyActionAnswer
    with BeforeAndAfterEach
    with ResetMocksAfterEachTest {

  private val creditBalance = CreditBalance(10, 5, 500, true, 0.30)
  private val saveAnsFun = mock[SaveUserAnswerFunc]
  private val answer = mock[UserAnswers]
  private val dataRequest = mock[DataRequest[AnyContent]]
  private val mockCalculateCreditConnector = mock[CalculateCreditsConnector]
  private val mockMessagesApi: MessagesApi = mock[MessagesApi]
  private val controllerComponents = stubMessagesControllerComponents()
  private val mockView = mock[ConfirmPackagingCreditView]
  private val cacheConnector = mock[CacheConnector]
  private val returnsJourneyNavigator = mock[ReturnsJourneyNavigator]
  private val edgeOfSystem = mock[EdgeOfSystem]
  private val journeyAction = mock[JourneyAction]
  private val creditSummaryListFactory = mock[CreditSummaryListFactory]

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

    reset(dataRequest, mockView, creditSummaryListFactory)

    when(dataRequest.userAnswers).thenReturn(answer)
    when(dataRequest.pptReference).thenReturn("123")
    when(journeyAction.async(any)).thenAnswer(byConvertingFunctionArgumentsToFutureAction)
    when(edgeOfSystem.localDateTimeNow) thenReturn LocalDateTime.of(2022, 4, 1, 12, 1, 0)
  }


  "onPageLoad" should {

    "use the journey action" in {
      sut.onPageLoad(NormalMode)
      verify(journeyAction).async(any)
    }

    "return OK" in {

      setUpMockForConfirmCreditsView()

      val result = sut.onPageLoad(NormalMode)(dataRequest)

      status(result) mustBe OK
    }

    "display a view" in {
      setUpMockForConfirmCreditsView()

      val summaryList = Seq(SummaryListRow(Key(Text("key")), Value(Text("value"))))

      when(creditSummaryListFactory.createSummaryList(any, any)(any)).thenReturn(summaryList)
      when(edgeOfSystem.localDateTimeNow) thenReturn LocalDateTime.of(2023, 3, 31, 23, 59, 59) // One sec before midnight
      await(sut.onPageLoad(NormalMode)(dataRequest))

      verify(mockView).apply(meq(BigDecimal(5)), any, meq(summaryList), any, any)(any, any)
    }

    "pass a summary list to view with the data" in {
      setUpMockForConfirmCreditsView()

      val summaryList = Seq(SummaryListRow(Key(Text("key")), Value(Text("value"))))

      when(creditSummaryListFactory.createSummaryList(any, any)(any)).thenReturn(summaryList)
      when(edgeOfSystem.localDateTimeNow) thenReturn LocalDateTime.of(2023, 3, 31, 23, 59, 59) // One sec before midnight
      await(sut.onPageLoad(NormalMode)(dataRequest))

      verify(creditSummaryListFactory).createSummaryList(meq(creditBalance), any)(any)
      verify(creditSummaryListFactory).createSummaryList(meq(creditBalance), meq(answer))(any)
    }


    "return the ConfirmPackagingCreditView with the credit amount on page loading" when {
      "total requested credit is less than available credit - (NormalMode)" in {
        when(returnsJourneyNavigator.confirmCreditRoute(any, any)) thenReturn Call("Hi", "You")
        setUpMockForConfirmCreditsView()
        await(sut.onPageLoad(NormalMode)(dataRequest))
        verify(mockView).apply( meq(BigDecimal(5)), any, any, meq(Call("Hi", "You")), meq(NormalMode))(any,any)
      }

      "total requested credit is less than available credit - (CheckMode)" in {
        when(returnsJourneyNavigator.confirmCreditRoute(any, any)) thenReturn Call("get", "cheese")
        setUpMockForConfirmCreditsView()
        await(sut.onPageLoad(CheckMode)(dataRequest))
        verify(mockView).apply(meq(BigDecimal(5)), any, any, meq(Call("get", "cheese")), meq(CheckMode))(any,any)
      }
    }

    "return an error page" in {
      when(dataRequest.userAnswers.get(any[Gettable[Boolean]])(any)).thenReturn(Some(true))
      when(mockCalculateCreditConnector.get(any)(any))
        .thenReturn(Future.successful(Left(DownstreamServiceError("Error", new Exception("error")))))

      val result = sut.onPageLoad(NormalMode)(dataRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad.url
    }

    val table = Table(
      ("description", "ExportedCreditsPage", "ConvertedCreditsPage"),
      ("exported credit", None, Some(true)),
      ("converted credit", Some(true), None),
      ("exported and converted", None, None),
    )
    forAll(table){
      (description, exportedCreditsPage, convertedCreditsPage) =>
        s"redirect to submit-return-or-claim-credit page when ${description} hasn't been answered" in {
          when(dataRequest.userAnswers.get(ArgumentMatchers.eq(ExportedCreditsPage))(any)).thenReturn(exportedCreditsPage)
          when(dataRequest.userAnswers.get(ArgumentMatchers.eq(ConvertedCreditsPage))(any)).thenReturn(convertedCreditsPage)
          val result = sut.onPageLoad(NormalMode)(dataRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustEqual controllers.returns.credits.routes.WhatDoYouWantToDoController.onPageLoad(NormalMode).url
        }
    }

  }

  "onCancelClaim" should {
    "invoke the journey action" in {
      when(journeyAction.async(any)) thenReturn mock[Action[AnyContent]]
      Try(await(sut.onCancelClaim(NormalMode)(dataRequest)))
      verify(journeyAction).async(any)
    }

    "set userAnswer" in {
      setUpMockForCancelCredit(mock[UserAnswers])

      await(sut.onCancelClaim(NormalMode).skippingJourneyAction(dataRequest))

      verify(dataRequest.userAnswers).setOrFail(meq(WhatDoYouWantToDoPage), meq(false), any)(any)
    }

    "save user answer to cache" in {
      val ans = mock[UserAnswers]
      setUpMockForCancelCredit(ans)

      await(sut.onCancelClaim(NormalMode).skippingJourneyAction(dataRequest))

      verify(ans).save(meq(saveAnsFun))(any)
      verify(cacheConnector).saveUserAnswerFunc(meq("123"))(any)
    }

    "navigate to a new page" in {
      setUpMockForCancelCredit(mock[UserAnswers])

      await(sut.onCancelClaim(NormalMode).skippingJourneyAction(dataRequest))

      verify(returnsJourneyNavigator).confirmCreditRoute(NormalMode, dataRequest.userAnswers)
    }
  }

  private def setUpMockForConfirmCreditsView(): Unit = {
    when(dataRequest.userAnswers.get(any[Gettable[Boolean]])(any)).thenReturn(Some(true))
    when(mockView.apply(any, any, any, any, any)(any,any)).thenReturn(Html("correct view"))
    when(mockCalculateCreditConnector.get(any)(any))
      .thenReturn(Future.successful(Right(creditBalance)))
  }

  private def setUpMockForCancelCredit(ans: UserAnswers): Unit = {
    when(ans.save(any)(any)).thenReturn(Future.successful(mock[UserAnswers]))
    when(dataRequest.userAnswers.setOrFail(any[Settable[Boolean]], any, any)(any)).thenReturn(ans)
    when(cacheConnector.saveUserAnswerFunc(any)(any)).thenReturn(saveAnsFun)
    when(returnsJourneyNavigator.confirmCreditRoute(NormalMode, dataRequest.userAnswers)).thenReturn(Call("GET", "/foo"))
  }
}
