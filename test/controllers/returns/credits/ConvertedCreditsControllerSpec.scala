/*
 * Copyright 2025 HM Revenue & Customs
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

import base.utils.JourneyActionAnswer._
import cacheables.ReturnObligationCacheable
import connectors.CacheConnector
import controllers.actions.JourneyAction
import forms.returns.credits.ConvertedCreditsFormProvider
import models.Mode.NormalMode
import models.UserAnswers
import models.requests.DataRequest
import models.returns.credits.SingleYearClaim
import models.returns.{CreditRangeOption, CreditsAnswer, TaxReturnObligation}
import navigation.ReturnsJourneyNavigator
import org.mockito.ArgumentMatchersSugar._
import org.mockito.MockitoSugar
import org.mockito.captor.ArgCaptor
import org.mockito.scalatest.ResetMocksAfterEachTest
import org.mockito.stubbing.ReturnsDeepStubs
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import pages.returns.credits.ConvertedCreditsPage
import play.api.data.Form
import play.api.data.Forms.boolean
import play.api.http.Status
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.json.JsPath
import play.api.mvc.{AnyContent, Call, RequestHeader}
import play.api.test.Helpers._
import play.twirl.api.Html
import views.html.returns.credits.ConvertedCreditsView

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ConvertedCreditsControllerSpec
    extends PlaySpec
    with MockitoSugar
    with BeforeAndAfterEach
    with ResetMocksAfterEachTest {

  private val mockCacheConnector: CacheConnector     = mock[CacheConnector]
  private val mockNavigator: ReturnsJourneyNavigator = mock[ReturnsJourneyNavigator]
  private val view                                   = mock[ConvertedCreditsView]
  private val formProvider                           = mock[ConvertedCreditsFormProvider]
  private val initialForm                            = mock[Form[Boolean]]("initial form")
  private val preparedForm                           = mock[Form[Boolean]]("prepared form")
  private val journeyAction                          = mock[JourneyAction]
  private val request                                = mock[DataRequest[AnyContent]](ReturnsDeepStubs)
  private val messagesApi                            = mock[MessagesApi]
  private val messages                               = mock[Messages]
  private val saveUserAnswerFunc                     = mock[UserAnswers.SaveUserAnswerFunc]
  private val creditRangeOption = CreditRangeOption(LocalDate.of(2023, 4, 1), LocalDate.of(2024, 3, 31))

  private val controllerComponents = stubMessagesControllerComponents()

  private val controller: ConvertedCreditsController = new ConvertedCreditsController(
    messagesApi,
    mockCacheConnector,
    mockNavigator,
    journeyAction,
    formProvider,
    controllerComponents,
    view
  )

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    when(formProvider.apply()).thenReturn(initialForm)
    when(initialForm.bindFromRequest()(any, any)).thenReturn(preparedForm)
    when(view.apply(any, any, any, any)(any, any)).thenReturn(Html("correct view"))

    when(mockCacheConnector.saveUserAnswerFunc(any)(any)) thenReturn saveUserAnswerFunc
    when(mockNavigator.convertedCreditsYesNo(any, any, any)).thenReturn(Call("GET", "/next/page"))

    when(journeyAction.apply(any)) thenAnswer byConvertingFunctionArgumentsToAction
    when(journeyAction.async(any)) thenAnswer byConvertingFunctionArgumentsToFutureAction
    when(messagesApi.preferred(any[RequestHeader])) thenReturn messages

    when(request.userAnswers.get(eqTo(ReturnObligationCacheable))(any)) thenReturn Some(mock[TaxReturnObligation])
    when(request.userAnswers.get[SingleYearClaim](eqTo(JsPath \ "credit" \ "year-key"))(any)) thenReturn Some(
      SingleYearClaim(
        fromDate = LocalDate.of(2023, 4, 1),
        toDate = LocalDate.of(2024, 3, 31),
        exportedCredits = None,
        convertedCredits = None
      )
    )
  }

  "onPageLoad" must {

    "use the journey action" in {
      controller.onPageLoad("year-key", NormalMode)
      verify(journeyAction).async(any)
    }

    "fill the form with user's previous answer" in {
      controller.onPageLoad("year-key", NormalMode)(request)
      val function = ArgCaptor[CreditsAnswer => Option[Boolean]]
      verify(request.userAnswers).fillWithFunc(eqTo(ConvertedCreditsPage("year-key")), eqTo(initialForm), function)(any)

      withClue("using correct function") {
        val creditsAnswer = mock[CreditsAnswer]
        function.value(creditsAnswer)
        verify(creditsAnswer).yesNo
      }
    }

    "render the page" in {
      when(request.userAnswers.fillWithFunc(any, any[Form[Boolean]], any)(any)) thenReturn preparedForm
      controller.onPageLoad("year-key", NormalMode)(request)
      verify(messagesApi).preferred(request)
      verify(view).apply(preparedForm, "year-key", NormalMode, creditRangeOption)(request, messages)
    }

    "200 ok the client" in {
      val futureResult = controller.onPageLoad("year-key", NormalMode)(request)
      status(futureResult) mustBe Status.OK
      contentAsString(futureResult) mustBe "correct view"
    }

    "redirect if obligation is missing" in {
      when(request.userAnswers.get(eqTo(ReturnObligationCacheable))(any)) thenReturn None
      val result = controller.onPageLoad("year-key", NormalMode)(request)
      await(result)

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe controllers.routes.IndexController.onPageLoad.url
    }

    "redirect if claim for year is missing" in {
      when(request.userAnswers.get[SingleYearClaim](eqTo(JsPath \ "credit" \ "year-key"))(any)) thenReturn None
      val result = controller.onPageLoad("year-key", NormalMode)(request)
      await(result)

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe
        controllers.returns.credits.routes.CreditsClaimedListController.onPageLoad(NormalMode).url
    }
  }

  "onSubmit" must {

    "remember the user's answers" in {
      when(initialForm.bindFromRequest()(any, any)) thenReturn Form("v" -> boolean).fill(true)
      await(controller.onSubmit("year-key", NormalMode)(request))

      verify(request.userAnswers).changeWithFunc(eqTo(ConvertedCreditsPage("year-key")), any, eqTo(saveUserAnswerFunc))(
        any,
        any
      )
    }

    "redirect to the next page" in {
      when(initialForm.bindFromRequest()(any, any)) thenReturn Form("v" -> boolean).fill(true)
      when(request.userAnswers.changeWithFunc(any, any, any)(any, any)) thenReturn Future.unit

      val result = await(controller.onSubmit("year-key", NormalMode)(request))
      verify(mockNavigator).convertedCreditsYesNo(eqTo(NormalMode), eqTo("year-key"), eqTo(true))

      result.header.status mustBe Status.SEE_OTHER
      redirectLocation(Future.successful(result)) mustBe Some("/next/page")
    }

    "display any errors" in {
      val formWithErrors = Form("v" -> boolean).withError("key", "message")
      when(initialForm.bindFromRequest()(any, any)) thenReturn formWithErrors

      val result = await(controller.onSubmit("year-key", NormalMode)(request))
      verify(view).apply(eqTo(formWithErrors), eqTo("year-key"), eqTo(NormalMode), eqTo(creditRangeOption))(
        eqTo(request),
        eqTo(messages)
      )

      result.header.status mustBe Status.BAD_REQUEST
      contentAsString(Future.successful(result)) mustBe "correct view"
    }

    "redirect if obligation is missing" in {
      when(request.userAnswers.get(eqTo(ReturnObligationCacheable))(any)) thenReturn None
      val result = controller.onSubmit("year-key", NormalMode)(request)
      await(result)
      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe controllers.routes.IndexController.onPageLoad.url
    }

    "redirect if claim for year is missing" in {
      when(request.userAnswers.get[SingleYearClaim](eqTo(JsPath \ "credit" \ "year-key"))(any)) thenReturn None
      val result = controller.onSubmit("year-key", NormalMode)(request)
      await(result)
      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe
        controllers.returns.credits.routes.CreditsClaimedListController.onPageLoad(NormalMode).url
    }

  }
}
