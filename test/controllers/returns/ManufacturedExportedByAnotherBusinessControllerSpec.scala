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

package controllers.returns

import connectors.CacheConnector
import controllers.BetterMockActionSyntax
import controllers.actions.JourneyAction
import controllers.actions.JourneyAction.{RequestAsyncFunction, RequestFunction}
import forms.returns.ManufacturedExportedByAnotherBusinessFormProvider
import models.Mode.NormalMode
import models.UserAnswers
import models.requests.DataRequest
import navigation.Navigator
import org.mockito.{Answers, ArgumentCaptor, ArgumentMatchers, ArgumentMatchersSugar}
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import pages.returns.{DirectlyExportedComponentsPage, ExportedPlasticPackagingWeightPage, ImportedPlasticPackagingPage, ImportedPlasticPackagingWeightPage, ManufacturedExportedByAnotherBusinessPage, ManufacturedPlasticPackagingPage, ManufacturedPlasticPackagingWeightPage}
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, RequestHeader}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.returns.ManufacturedExportedByAnotherBusinessView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ManufacturedExportedByAnotherBusinessControllerSpec
  extends PlaySpec
    with MockitoSugar
    with BeforeAndAfterEach {

  def onwardRoute = Call("GET", "/foo")

  private val formProvider = mock[ManufacturedExportedByAnotherBusinessFormProvider]
  private val bindForm = new ManufacturedExportedByAnotherBusinessFormProvider()()
  private val messagesApi = mock[MessagesApi]
  private val cacheConnector = mock[CacheConnector]
  private val navigator = mock[Navigator]
  private val journeyAction = mock[JourneyAction]
  private val view = mock[ManufacturedExportedByAnotherBusinessView]
  private val dataRequest = mock[DataRequest[AnyContent]](Answers.RETURNS_DEEP_STUBS)

  private val sut = new ManufacturedExportedByAnotherBusinessController(
    messagesApi,
    cacheConnector,
    navigator,
    journeyAction,
    formProvider,
    stubMessagesControllerComponents(),
    view
  )

  def byConvertingFunctionArgumentsToAction: (RequestFunction) => Action[AnyContent] = (function: RequestFunction) =>
    when(mock[Action[AnyContent]].apply(any))
      .thenAnswer((request: DataRequest[AnyContent]) => Future.successful(function(request)))
      .getMock[Action[AnyContent]]

  def byConvertingFunctionArgumentsToFutureAction: (RequestAsyncFunction) => Action[AnyContent] = (function: RequestAsyncFunction) =>
    when(mock[Action[AnyContent]].apply(any))
      .thenAnswer((request: DataRequest[AnyContent]) => function(request))
      .getMock[Action[AnyContent]]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(dataRequest, journeyAction, messagesApi, view)

    when(formProvider.apply()).thenReturn(bindForm)
    when(journeyAction.apply(any)) thenAnswer byConvertingFunctionArgumentsToAction
    when(journeyAction.async(any)) thenAnswer byConvertingFunctionArgumentsToFutureAction
    when(messagesApi.preferred(any[RequestHeader])) thenReturn mock[Messages]
    when(view.apply(any, any, any)(any,any)).thenReturn(HtmlFormat.empty)
  }

  "onPageLoad" should {

    "use the journey action" in {
      sut.onPageLoad(NormalMode)
      verify(journeyAction).apply(any)
    }

    "return OK" in {
      when(dataRequest.userAnswers) thenReturn createUserAnswer.set(ManufacturedExportedByAnotherBusinessPage, true).get

      val result = sut.onPageLoad(NormalMode)(dataRequest)

      status(result) mustBe OK
    }

    "return a view which answer is Yes" in {
      when(dataRequest.userAnswers) thenReturn createUserAnswer.set(ManufacturedExportedByAnotherBusinessPage, true).get

      await(sut.onPageLoad(NormalMode)(dataRequest))

      verifyAndCaptorForm.value mustBe Some(true)
    }

    "return a view which answer is No" in {
      when(dataRequest.userAnswers) thenReturn createUserAnswer.set(ManufacturedExportedByAnotherBusinessPage, false).get

      await(sut.onPageLoad(NormalMode)(dataRequest))

      verifyAndCaptorForm.value mustBe Some(false)
    }

    "return a view with no answer" in {
      when(dataRequest.userAnswers) thenReturn createUserAnswer

      await(sut.onPageLoad(NormalMode)(dataRequest))

      verifyAndCaptorForm.value mustBe None
    }

    "view should show total plastic packaging amount" in {
      when(dataRequest.userAnswers) thenReturn createUserAnswer

      await(sut.onPageLoad(NormalMode)(dataRequest))

      verify(view).apply(any, any, ArgumentMatchers.eq(300L))(any, any)
    }

    "redirect to the account page if cannot calculate total plastic" in {
      val userAnswer = UserAnswers("123")
        .set(ManufacturedPlasticPackagingWeightPage, 100L).get
        .set(ImportedPlasticPackagingPage, true).get
        .set(ImportedPlasticPackagingWeightPage, 200L).get

      when(dataRequest.userAnswers) thenReturn createUserAnswer.remove(ImportedPlasticPackagingPage).get

      val result = sut.onPageLoad(NormalMode)(dataRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.IndexController.onPageLoad.url
    }
  }

  "onSubmit" should {
    val form = mock[Form[Boolean]]

    "redirect to account page" in {
      when(form.bindFromRequest()(any,any)).thenReturn(bindForm.bind(Map("value" -> "true")))
      when(formProvider.apply()).thenReturn(form)
      when(dataRequest.userAnswers) thenReturn createUserAnswer.set(ManufacturedExportedByAnotherBusinessPage, true).get
      when(cacheConnector.saveUserAnswerFunc(any)(any)).thenReturn((_, _) => Future.successful(true))

      val result = sut.onSubmit(NormalMode).skippingJourneyAction(dataRequest)

      status(result) mustBe SEE_OTHER
    }

    "should save answer to the cache" in {
      when(form.bindFromRequest()(any,any)).thenReturn(bindForm.bind(Map("value" -> "true")))
      when(formProvider.apply()).thenReturn(form)
      when(cacheConnector.saveUserAnswerFunc(any)(any)).thenReturn((_, _) => Future.successful(true))
      when(dataRequest.pptReference).thenReturn("123")

      await(sut.onSubmit(NormalMode).skippingJourneyAction(dataRequest))

      verify(cacheConnector).saveUserAnswerFunc(ArgumentMatchers.eq("123"))(any)
    }
  }


//    "must populate the view correctly on a GET when the question has previously been answered" in {
//
//      val userAnswers = UserAnswers(userAnswersId).set(ManufacturedExportedByAnotherBusinessPage, true).success.value
//
//      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
//
//      running(application) {
//        val request = FakeRequest(GET, manufacturedExportedByAnotherBusinessRoute)
//
//        val view = application.injector.instanceOf[ManufacturedExportedByAnotherBusinessView]
//
//        val result = route(application, request).value
//
//        status(result) mustEqual OK
//        contentAsString(result) mustEqual view(form.fill(true), NormalMode, 200L)(request, messages(application)).toString
//      }
//    }

//    "must redirect to the next page when valid data is submitted" in {
//
//      val mockCacheConnector = mock[CacheConnector]
//
//      when(mockCacheConnector.set(any(), any())(any())) thenReturn Future.successful(mockResponse)
//
//      val application =
//        applicationBuilder(userAnswers = Some(emptyUserAnswers))
//          .overrides(
//            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
//            bind[CacheConnector].toInstance(mockCacheConnector)
//          )
//          .build()
//
//      running(application) {
//        val request =
//          FakeRequest(POST, manufacturedExportedByAnotherBusinessRoute)
//            .withFormUrlEncodedBody(("value", "true"))
//
//        val result = route(application, request).value
//
//        status(result) mustEqual SEE_OTHER
//        redirectLocation(result).value mustEqual onwardRoute.url
//      }
//    }

//    "must return a Bad Request and errors when invalid data is submitted" in {
//
//      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
//
//      running(application) {
//        val request =
//          FakeRequest(POST, manufacturedExportedByAnotherBusinessRoute)
//            .withFormUrlEncodedBody(("value", ""))
//
//        val boundForm = form.bind(Map("value" -> ""))
//
//        val view = application.injector.instanceOf[ManufacturedExportedByAnotherBusinessView]
//
//        val result = route(application, request).value
//
//        status(result) mustEqual BAD_REQUEST
//        contentAsString(result) mustEqual view(boundForm, NormalMode, 200L)(request, messages(application)).toString
//      }
//    }

//    "must redirect to Journey Recovery for a GET if no existing data is found" in {
//
//      val application = applicationBuilder(userAnswers = None).build()
//
//      running(application) {
//        val request = FakeRequest(GET, manufacturedExportedByAnotherBusinessRoute)
//
//        val result = route(application, request).value
//
//        status(result) mustEqual SEE_OTHER
//        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad.url
//      }
//    }

//    "must redirect to Journey Recovery for a POST if no existing data is found" in {
//
//      val application = applicationBuilder(userAnswers = None).build()
//
//      running(application) {
//        val request =
//          FakeRequest(POST, manufacturedExportedByAnotherBusinessRoute)
//            .withFormUrlEncodedBody(("value", "true"))
//
//        val result = route(application, request).value
//
//        status(result) mustEqual SEE_OTHER
//        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad.url
//      }
//    }

  private def createUserAnswer: UserAnswers = {
    UserAnswers("123")
      .set(ManufacturedPlasticPackagingPage, true).get
      .set(ManufacturedPlasticPackagingWeightPage, 100L).get
      .set(ImportedPlasticPackagingPage, true).get
      .set(ImportedPlasticPackagingWeightPage, 200L).get
  }

  private def verifyAndCaptorForm = {
    val captor: ArgumentCaptor[Form[Boolean]] = ArgumentCaptor.forClass(classOf[Form[Boolean]])
    verify(view).apply(captor.capture(), any, any)(any, any)
    captor.getValue
  }
}
