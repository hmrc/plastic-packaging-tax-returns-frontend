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

import base.utils.JourneyActionAnswer.{byConvertingFunctionArgumentsToAction, byConvertingFunctionArgumentsToFutureAction}
import cacheables.ReturnObligationCacheable
import connectors.CacheConnector
import controllers.BetterMockActionSyntax
import controllers.actions.JourneyAction
import forms.returns.credits.ConvertedCreditsWeightFormProvider
import models.Mode.NormalMode
import models.UserAnswers
import models.UserAnswers.SaveUserAnswerFunc
import models.requests.DataRequest
import models.returns.credits.SingleYearClaim
import models.returns.{CreditRangeOption, CreditsAnswer, TaxReturnObligation}
import navigation.ReturnsJourneyNavigator
import org.mockito.ArgumentMatchers.{eq => meq}
import org.mockito.ArgumentMatchersSugar._
import org.mockito.MockitoSugar
import org.mockito.scalatest.ResetMocksAfterEachTest
import org.mockito.stubbing.ReturnsDeepStubs
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import pages.returns.credits.ConvertedCreditsPage
import play.api.data.Form
import play.api.data.Forms.longNumber
import play.api.http.Status
import play.api.http.Status.{BAD_REQUEST, SEE_OTHER}
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.json.JsPath
import play.api.mvc.{AnyContent, Call, MessagesControllerComponents, RequestHeader}
import play.api.test.Helpers.{GET, await, defaultAwaitTimeout, redirectLocation, status}
import play.twirl.api.HtmlFormat
import views.html.returns.credits.ConvertedCreditsWeightView

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ConvertedCreditsWeightControllerSpec
    extends PlaySpec
    with MockitoSugar
    with BeforeAndAfterEach
    with ResetMocksAfterEachTest {

  private val messagesApi          = mock[MessagesApi]
  private val journeyAction        = mock[JourneyAction]
  private val controllerComponents = mock[MessagesControllerComponents]
  private val view                 = mock[ConvertedCreditsWeightView]
  private val formProvider         = mock[ConvertedCreditsWeightFormProvider]
  private val cacheConnector       = mock[CacheConnector]
  private val navigator            = mock[ReturnsJourneyNavigator]
  private val creditRangeOption    = CreditRangeOption(LocalDate.of(2023, 4, 1), LocalDate.of(2024, 3, 31))

  private val controller = new ConvertedCreditsWeightController(
    messagesApi,
    journeyAction,
    controllerComponents,
    view,
    formProvider,
    cacheConnector,
    navigator
  )

  private val request            = mock[DataRequest[AnyContent]](ReturnsDeepStubs)
  private val form               = mock[Form[Long]]
  private val messages           = mock[Messages]
  private val updatedUserAnswers = mock[UserAnswers]

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    when(journeyAction.apply(any)) thenAnswer byConvertingFunctionArgumentsToAction
    when(journeyAction.async(any)) thenAnswer byConvertingFunctionArgumentsToFutureAction

    when(formProvider.apply()) thenReturn form

    when(view.apply(any, any, any)(any, any)) thenReturn HtmlFormat.raw("a-view")
    when(request.userAnswers.fillWithFunc(eqTo(ConvertedCreditsPage("year-key")), eqTo(form), any)(any)) thenReturn form
    when(request.userAnswers.setOrFail(any, any, any)(any)) thenReturn updatedUserAnswers
    when(updatedUserAnswers.save(any)(any)) thenReturn Future.successful(updatedUserAnswers)

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

  "onPageLoad" should {

    "use the journey action" in {
      controller.onPageLoad("year-key", NormalMode)
      verify(journeyAction).async(any)
    }

    "show web page with correct submit url" in {
      when(messagesApi.preferred(any[RequestHeader])) thenReturn messages
      status {
        controller.onPageLoad("year-key", NormalMode).skippingJourneyAction(request)
      } mustBe Status.OK
      val call = routes.ConvertedCreditsWeightController.onSubmit("year-key", NormalMode)
      verify(view).apply(eqTo(form), eqTo(call), eqTo(creditRangeOption))(eqTo(request), eqTo(messages))
    }

    "use an existing user-answer if present" in {
      await {
        controller.onPageLoad("year-key", NormalMode).skippingJourneyAction(request)
      }
      verify(request.userAnswers).fillWithFunc(eqTo(ConvertedCreditsPage("year-key")), eqTo(form), any)(any)
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

  "onSubmit" should {

    "save the user's answer" in {
      val boundForm = Form("value" -> longNumber).fill(1L)
      when(form.bindFromRequest()(any, any)) thenReturn boundForm

      val saveFunction = mock[SaveUserAnswerFunc]
      when(cacheConnector.saveUserAnswerFunc(any)(any)) thenReturn saveFunction

      when(navigator.convertedCreditsWeight(any, any)).thenReturn(Call(GET, "/foo"))

      await(controller.onSubmit("year-key", NormalMode).skippingJourneyAction(request))

      verify(request.userAnswers).setOrFail(
        eqTo(ConvertedCreditsPage("year-key")),
        eqTo(CreditsAnswer.answerWeightWith(1L)),
        any
      )(any)
    }

    "redirect" in {
      when(form.bindFromRequest()(any, any)) thenReturn Form("value" -> longNumber).fill(1L)
      when(navigator.convertedCreditsWeight(any, any)).thenReturn(Call(GET, "/foo"))

      val result = controller.onSubmit("year-key", NormalMode).skippingJourneyAction(request)

      status(result) mustBe SEE_OTHER
      verify(navigator).convertedCreditsWeight("year-key", NormalMode)
    }

    "show an error page when error on form" in {
      val boundFormWithError = Form("value" -> longNumber).withError("message", "error message")
      when(form.bindFromRequest()(any, any)) thenReturn boundFormWithError

      val result = controller.onSubmit("year-key", NormalMode).skippingJourneyAction(request)

      status(result) mustBe BAD_REQUEST
      verify(view).apply(meq(boundFormWithError), any, any)(any, any)
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
