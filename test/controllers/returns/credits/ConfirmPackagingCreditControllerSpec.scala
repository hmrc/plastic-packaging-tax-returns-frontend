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
import controllers.actions.JourneyAction.RequestAsyncFunction
import controllers.actions.{CheckCreditKeyAction, JourneyAction}
import factories.CreditSummaryListFactory
import models.Mode.{CheckMode, NormalMode}
import models.UserAnswers.SaveUserAnswerFunc
import models.requests.DataRequest
import models.returns.CreditsAnswer
import models.{CreditBalance, TaxablePlastic, UserAnswers}
import navigation.ReturnsJourneyNavigator
import org.mockito.ArgumentMatchersSugar.{any, eqTo}
import org.mockito.integrations.scalatest.ResetMocksAfterEachTest
import org.mockito.MockitoSugar
import org.scalatest.BeforeAndAfterEach
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatestplus.play.PlaySpec
import pages.returns.credits.{ConvertedCreditsPage, ExportedCreditsPage}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, ActionBuilder, ActionFilter, AnyContent, Call, Result}
import play.api.test.Helpers._
import play.twirl.api.Html
import queries.{Gettable, Settable}
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow, Value}
import util.EdgeOfSystem
import views.html.returns.credits.ConfirmPackagingCreditView

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ConfirmPackagingCreditControllerSpec
  extends PlaySpec
    with MockitoSugar
    with JourneyActionAnswer
    with BeforeAndAfterEach
    with ResetMocksAfterEachTest {

  private val creditBalance = CreditBalance(10, 5, 500, true, Map("year-key" -> TaxablePlastic(1, 2, 0.30)))
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
  private val creditAction = mock[CheckCreditKeyAction]
  private val creditSummaryListFactory = mock[CreditSummaryListFactory]

  private val sut = new ConfirmPackagingCreditController(
    mockMessagesApi,
    mockCalculateCreditConnector,
    journeyAction,
    creditAction,
    controllerComponents,
    mockView,
    cacheConnector, 
    returnsJourneyNavigator,
    creditSummaryListFactory
  )

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    when(dataRequest.userAnswers).thenReturn(answer)
    when(dataRequest.pptReference).thenReturn("123")
    val mockBuild = mock[ActionBuilder[DataRequest, AnyContent]]
    when(journeyAction.build).thenReturn(mockBuild)
    val x = mock[ActionBuilder[DataRequest, AnyContent]]
    when(mockBuild.andThen[DataRequest](any)).thenReturn(x)
    val y = mock[ActionFilter[DataRequest]]
    when(creditAction.apply(any)).thenReturn(y)

    when(x.async(any[RequestAsyncFunction])).thenAnswer(byConvertingFunctionArgumentsToFutureAction)
    when(edgeOfSystem.localDateTimeNow) thenReturn LocalDateTime.of(2022, 4, 1, 12, 1, 0)
    when(creditSummaryListFactory.createSummaryList(any, any, any)(any)) thenReturn Seq()
  }


  "onPageLoad" should {

    "use the journey action" in {
      sut.onPageLoad("year-key", NormalMode)
      verify(creditAction).apply("year-key")
      verify(journeyAction).build
    }

    "return OK" in {

      setUpMockForConfirmCreditsView()

      val result = sut.onPageLoad("year-key", NormalMode)(dataRequest)

      status(result) mustBe OK
    }

    "display a view" in {
      setUpMockForConfirmCreditsView()

      val summaryList = Seq(SummaryListRow(Key(Text("key")), Value(Text("value"))))
      when(creditSummaryListFactory.createSummaryList(any, any, any)(any)).thenReturn(summaryList)
      when(edgeOfSystem.localDateTimeNow) thenReturn LocalDateTime.of(2023, 3, 31, 23, 59, 59) // One sec before midnight

      await(sut.onPageLoad("year-key", NormalMode)(dataRequest))
      verify(mockView).apply(eqTo("year-key"), eqTo(BigDecimal(2)), any, eqTo(summaryList), any, any)(any, any)
    }

    "pass a summary list to view with the data" in {
      setUpMockForConfirmCreditsView()

      val summaryList = Seq(SummaryListRow(Key(Text("key")), Value(Text("value"))))

      when(creditSummaryListFactory.createSummaryList(any, any, any)(any)).thenReturn(summaryList)
      when(edgeOfSystem.localDateTimeNow) thenReturn LocalDateTime.of(2023, 3, 31, 23, 59, 59) // One sec before midnight
      await(sut.onPageLoad("year-key", NormalMode)(dataRequest))

      verify(creditSummaryListFactory).createSummaryList(eqTo(TaxablePlastic(1, 2, 0.30)), eqTo("year-key"), any)(any)
      verify(creditSummaryListFactory).createSummaryList(eqTo(TaxablePlastic(1, 2, 0.30)), eqTo("year-key"), eqTo(answer))(any)
    }


    "return the ConfirmPackagingCreditView with the credit amount on page loading" when {

      val call = Call("Hi", "You")

      "total requested credit is less than available credit - (NormalMode)" in {
        when(returnsJourneyNavigator.confirmCredit(any)) thenReturn call
        setUpMockForConfirmCreditsView()
        await(sut.onPageLoad("year-key", NormalMode)(dataRequest))
        verify(returnsJourneyNavigator).confirmCredit(NormalMode)
        verify(mockView).apply(eqTo("year-key"), eqTo(BigDecimal(2)), any, any, eqTo(call), eqTo(NormalMode))(any,any)
      }

      "total requested credit is less than available credit - (CheckMode)" in {
        when(returnsJourneyNavigator.confirmCredit(any)) thenReturn call
        setUpMockForConfirmCreditsView()
        await(sut.onPageLoad("year-key", CheckMode)(dataRequest))
        verify(returnsJourneyNavigator).confirmCredit(CheckMode)
        verify(mockView).apply(eqTo("year-key"), eqTo(BigDecimal(2)), any, any, eqTo(call), eqTo(CheckMode))(any,any)
      }
    }

    "return an error page" in {
      when(dataRequest.userAnswers.get(any[Gettable[Boolean]])(any)).thenReturn(Some(true))
      when(mockCalculateCreditConnector.get(any)(any))
        .thenReturn(Future.successful(Left(DownstreamServiceError("Error", new Exception("error")))))

      val result = sut.onPageLoad("year-key", NormalMode)(dataRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad.url
    }

    val table = Table(
      ("description", "ExportedCreditsPage", "ConvertedCreditsPage"),
      ("exported credit", None, Some(CreditsAnswer.answerWeightWith(1L))),
      ("converted credit", Some(CreditsAnswer.answerWeightWith(2L)), None),
      ("exported and converted", None, None),
    )
    forAll(table){
      (description, exportedCreditsPage, convertedCreditsPage) =>
        s"redirect to submit-return-or-claim-credit page when ${description} hasn't been answered" in {
          when(dataRequest.userAnswers.get(eqTo(ExportedCreditsPage("year-key")))(any)).thenReturn(exportedCreditsPage)
          when(dataRequest.userAnswers.get(eqTo(ConvertedCreditsPage("year-key")))(any)).thenReturn(convertedCreditsPage)
          val result = sut.onPageLoad("year-key", NormalMode)(dataRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustEqual controllers.returns.credits.routes.WhatDoYouWantToDoController.onPageLoad(NormalMode).url
        }
    }

  }

  private def setUpMockForConfirmCreditsView(): Unit = {
    when(dataRequest.userAnswers.get(any[Gettable[Boolean]])(any)).thenReturn(Some(true))
    when(mockView.apply(any, any, any, any, any, any)(any,any)).thenReturn(Html("correct view"))
    when(mockCalculateCreditConnector.get(any)(any))
      .thenReturn(Future.successful(Right(creditBalance)))
  }

  // TODO are we missing a test for cancelling?
  
  private def setUpMockForCancelCredit(ans: UserAnswers): Unit = {
    when(ans.save(any)(any)).thenReturn(Future.successful(ans))
    when(dataRequest.userAnswers.setOrFail(any[Settable[Boolean]], any, any)(any)).thenReturn(ans)
    when(dataRequest.userAnswers.setOrFail(any[Settable[CreditsAnswer]], any, any)(any)).thenReturn(ans)
    when(cacheConnector.saveUserAnswerFunc(any)(any)).thenReturn(saveAnsFun)
    when(returnsJourneyNavigator.confirmCredit(NormalMode)).thenReturn(Call("GET", "/foo"))
  }
}
