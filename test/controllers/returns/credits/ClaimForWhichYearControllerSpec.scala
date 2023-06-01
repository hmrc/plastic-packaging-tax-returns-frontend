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
import connectors.{AvailableCreditYearsConnector, CacheConnector, DownstreamServiceError}
import controllers.BetterMockActionSyntax
import controllers.actions.JourneyAction
import forms.returns.credits.ClaimForWhichYearFormProvider
import models.Mode.NormalMode
import models.UserAnswers
import models.requests.DataRequest
import models.returns.CreditRangeOption
import navigation.ReturnsJourneyNavigator
import org.mockito.Answers
import org.mockito.ArgumentMatchers.{eq => meq}
import org.mockito.ArgumentMatchersSugar.{any, eqTo}
import org.mockito.Mockito.{atLeastOnce, verifyNoInteractions}
import org.mockito.MockitoSugar.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.data.Form
import play.api.data.Forms.ignored
import play.api.http.Status
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.json.{JsObject, JsPath, JsString}
import play.api.mvc.{Action, AnyContent, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import queries.{Gettable, Settable}
import views.html.returns.credits.ClaimForWhichYearView

import java.time.LocalDate
import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future
import scala.util.Try

class ClaimForWhichYearControllerSpec extends PlaySpec with JourneyActionAnswer with MockitoSugar with BeforeAndAfterEach  {

  private val mockMessagesApi: MessagesApi = mock[MessagesApi]
  private val messages = mock[Messages]
  private val mockNavigator: ReturnsJourneyNavigator = mock[ReturnsJourneyNavigator]
  private val mockView = mock[ClaimForWhichYearView]
  private val mockFormProvider = mock[ClaimForWhichYearFormProvider]
  private val journeyAction = mock[JourneyAction]
  private val controllerComponents = stubMessagesControllerComponents()
  private val mockCache = mock[CacheConnector]
  private val dataRequest = mock[DataRequest[AnyContent]](Answers.RETURNS_DEEP_STUBS)
  private val form = mock[Form[CreditRangeOption]]
  private val availableYearsConnector = mock[AvailableCreditYearsConnector]
  val testForm: Form[CreditRangeOption] = Form("x" -> ignored(CreditRangeOption(LocalDate.now(), LocalDate.now())))
  val saveUserAnswerFunc = mock[UserAnswers.SaveUserAnswerFunc]

  val sut: ClaimForWhichYearController = new ClaimForWhichYearController(
    mockMessagesApi,
    journeyAction,
    controllerComponents,
    mockView,
    mockFormProvider,
    mockNavigator,
    mockCache,
    availableYearsConnector
  )(global)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(
      mockMessagesApi,
      journeyAction,
      mockView,
      mockFormProvider,
      form,
      mockNavigator,
      dataRequest,
      messages,
      mockCache,
      availableYearsConnector
    )
    when(journeyAction.apply(any)).thenAnswer(byConvertingFunctionArgumentsToAction)
    when(journeyAction.async(any)).thenAnswer(byConvertingFunctionArgumentsToFutureAction)
    when(mockView.apply(any, any, any)(any, any)).thenReturn(Html("correct view"))
    when(mockFormProvider.apply(any)) thenReturn form
  }

  "onPageLoad" must {

    "use the journey action" in {
      when(journeyAction.async(any)) thenReturn mock[Action[AnyContent]]
      Try(await(sut.onPageLoad(NormalMode)(dataRequest)))
      verify(journeyAction).async(any)
    }

    "take in a value from form & return view" in {
      val availableYears = Seq(CreditRangeOption(LocalDate.now, LocalDate.now), CreditRangeOption(LocalDate.now, LocalDate.now))
      when(dataRequest.userAnswers.get(any[JsPath])(any)).thenReturn(None)
      when(availableYearsConnector.get(any)(any)).thenReturn(Future.successful(availableYears))
      when(dataRequest.userAnswers.get[Map[String, JsObject]](any[JsPath])(any)).thenReturn(None)

      val result = sut.onPageLoad(NormalMode)(dataRequest)

      status(result) mustBe OK
      verify(mockView).apply(meq(form), meq(availableYears), meq(NormalMode))(any, any)
    }

    "get the available years from backend" in {
      val availableYears = Seq(CreditRangeOption(LocalDate.now, LocalDate.now))
      when(availableYearsConnector.get(any)(any)).thenReturn(Future.successful(availableYears))
      when(dataRequest.userAnswers.get[Map[String, JsObject]](any[JsPath])(any)).thenReturn(Some(Map.empty))

      await(sut.onPageLoad(NormalMode)(dataRequest))

      verify(availableYearsConnector).get(eqTo(dataRequest.pptReference))(any)

    }

    "throw error when couldn't get available years" in {
      val error = DownstreamServiceError("error", new Exception())
      when(availableYearsConnector.get(any)(any)).thenReturn(Future.failed(error))

      val exception = intercept[DownstreamServiceError](await(sut.onSubmit(NormalMode).skippingJourneyAction(dataRequest)))
      exception mustBe error
    }

    "filter out already used options" in {
      val alreadyUsed = CreditRangeOption(LocalDate.of(2000, 1, 1), LocalDate.of(2000, 1, 2))
      when(dataRequest.userAnswers.get[Map[String, JsObject]](any[JsPath])(any)).thenReturn(Some(Map(alreadyUsed.key -> JsObject.empty)))
      val availableYears = Seq(CreditRangeOption(LocalDate.now, LocalDate.now), CreditRangeOption(LocalDate.now, LocalDate.now), alreadyUsed)
      val options = Seq(CreditRangeOption(LocalDate.now, LocalDate.now), CreditRangeOption(LocalDate.now, LocalDate.now))
      when(availableYearsConnector.get(any)(any)).thenReturn(Future.successful(availableYears))
      val result = sut.onPageLoad(NormalMode)(dataRequest)

      status(result) mustBe OK
      verify(mockView).apply(meq(form), meq(options), meq(NormalMode))(any, any)
    }

    "redirect if there are no options" in {
      val alreadyUsed = CreditRangeOption(LocalDate.of(2000, 1, 1), LocalDate.of(2000, 1, 2))
      when(dataRequest.userAnswers.get[Map[String, JsObject]](any[JsPath])(any)).thenReturn(Some(Map(alreadyUsed.key -> JsObject.empty)))
      val availableYears = Seq(alreadyUsed)
      when(availableYearsConnector.get(any)(any)).thenReturn(Future.successful(availableYears))
      val result = sut.onPageLoad(NormalMode)(dataRequest)

      redirectLocation(result) mustBe Some(controllers.returns.credits.routes.CreditsClaimedListController.onPageLoad(NormalMode).url)
    }

    "redirect if there is only ONE option" in {
      val onlyOption = CreditRangeOption(LocalDate.of(2000, 1, 1), LocalDate.of(2000, 1, 2))
      when(dataRequest.userAnswers.get[Map[String, JsObject]](any[JsPath])(any)).thenReturn(Some(Map.empty))
      when(dataRequest.userAnswers.get[Map[String, JsObject]](any[JsPath])(any)).thenReturn(None)
      when(availableYearsConnector.get(any)(any)).thenReturn(Future.successful(Seq(onlyOption)))
      when(mockNavigator.claimForWhichYear(any, any)).thenReturn(Call(GET, "/next/page"))
      val answers = dataRequest.userAnswers // avoid unfinished stubbing error
      when(mockCache.saveUserAnswerFunc(any)(any)).thenReturn(saveUserAnswerFunc)
      when(dataRequest.userAnswers.setOrFail(any[JsPath], any)(any)).thenReturn(answers)
      when(answers.save(any)(any)).thenReturn(Future.successful(answers))

      val result = sut.onPageLoad(NormalMode)(dataRequest)

      redirectLocation(result) mustBe Some("/next/page")
      verify(mockNavigator).claimForWhichYear(meq(onlyOption), meq(NormalMode))
    }
  }

  "onSubmit" must {

    "invoke the journey action" in {
      when(journeyAction.async(any)) thenReturn mock[Action[AnyContent]]
      Try(await(sut.onSubmit(NormalMode)(FakeRequest())))
      verify(journeyAction).async(any)
    }

    "set the toDate in the UserAnswers" in {
      when(dataRequest.userAnswers.get(any[JsPath])(any)).thenReturn(None)
      val ua = dataRequest.userAnswers
      val creditRangeOption = CreditRangeOption(LocalDate.of(1000, 1, 1), LocalDate.of(1996, 3, 27))
      val availableYears = Seq(creditRangeOption)
      when(availableYearsConnector.get(any)(any)).thenReturn(Future.successful(availableYears))
      when(mockNavigator.claimForWhichYear(any, any)).thenReturn(Call(GET, "/next/page"))
      when(mockCache.saveUserAnswerFunc(any)(any)).thenReturn(saveUserAnswerFunc)
      when(dataRequest.userAnswers.setOrFail(any[JsPath], any)(any)).thenReturn(ua)
      when(ua.save(any)(any)).thenReturn(Future.successful(ua))


      when(form.bindFromRequest()(any, any)) thenReturn testForm.fill(creditRangeOption)

      await(sut.onSubmit(NormalMode).skippingJourneyAction(dataRequest))

      verify(dataRequest.userAnswers, atLeastOnce()).setOrFail(JsPath \ "credit" \ "1000-01-01-1996-03-27" \ "toDate", LocalDate.of(1996, 3, 27))
      verify(mockCache).saveUserAnswerFunc(meq(dataRequest.pptReference))(any)
    }

    "redirect to the next page" in {
      when(dataRequest.userAnswers.get(any[JsPath])(any)).thenReturn(None)
      when(mockNavigator.claimForWhichYear(any, any)).thenReturn(Call(GET, "/next/page"))
      val availableYears = Seq(CreditRangeOption(LocalDate.now, LocalDate.now))
      when(availableYearsConnector.get(any)(any)).thenReturn(Future.successful(availableYears))

      when(mockCache.saveUserAnswerFunc(any)(any)).thenReturn(saveUserAnswerFunc)
      when(dataRequest.pptReference).thenReturn("hello")
      val userAnswers = dataRequest.userAnswers
      when(dataRequest.userAnswers.setOrFail(any[JsPath], any)(any)).thenReturn(userAnswers)
      when(userAnswers.save(any)(any)).thenReturn(Future.successful(UserAnswers("fin")))

      when(form.bindFromRequest()(any, any)) thenReturn testForm.fill(CreditRangeOption(LocalDate.now(), LocalDate.now()))

      val result = sut.onSubmit(NormalMode).skippingJourneyAction(dataRequest)

      status(result) mustBe Status.SEE_OTHER
      verify(userAnswers).save(any)(any)
      redirectLocation(result) mustBe Some("/next/page")
      verify(mockNavigator).claimForWhichYear(meq(CreditRangeOption(LocalDate.now(), LocalDate.now())), meq(NormalMode))
    }

    "display any form errors" in {
      when(dataRequest.userAnswers.get(any[JsPath])(any)).thenReturn(None)
      val availableYears = Seq(CreditRangeOption(LocalDate.now, LocalDate.now))
      when(availableYearsConnector.get(any)(any)).thenReturn(Future.successful(availableYears))

      val formWithErrors = testForm.withError("key", "message")
      when(form.bindFromRequest()(any, any)) thenReturn formWithErrors

      val result = await(sut.onSubmit(NormalMode).skippingJourneyAction(dataRequest))

      verify(mockView).apply(eqTo(formWithErrors), meq(availableYears), meq(NormalMode)) (eqTo(dataRequest), any)

      result.header.status mustBe Status.BAD_REQUEST
      contentAsString(Future.successful(result)) mustBe "correct view"
    }

    "display any form errors with filtered options" in {
      val alreadyUsed = CreditRangeOption(LocalDate.of(2000, 1, 1), LocalDate.of(2000, 1, 2))
      when(dataRequest.userAnswers.get[Map[String, JsObject]](any[JsPath])(any)).thenReturn(Some(Map(alreadyUsed.key -> JsObject.empty)))
      val availableYears = Seq(CreditRangeOption(LocalDate.now, LocalDate.now), alreadyUsed)
      val options = Seq(CreditRangeOption(LocalDate.now, LocalDate.now))
      when(availableYearsConnector.get(any)(any)).thenReturn(Future.successful(availableYears))

      val formWithErrors = testForm.withError("key", "message")
      when(form.bindFromRequest()(any, any)) thenReturn formWithErrors

      val result = await(sut.onSubmit(NormalMode).skippingJourneyAction(dataRequest))

      verify(mockView).apply(eqTo(formWithErrors), meq(options), meq(NormalMode)) (eqTo(dataRequest), any)

      result.header.status mustBe Status.BAD_REQUEST
      contentAsString(Future.successful(result)) mustBe "correct view"
    }

  }




}
