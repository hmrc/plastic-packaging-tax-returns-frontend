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

package controllers

import base.SpecBase
import connectors.CacheConnector
import controllers.actions.JourneyAction
import controllers.actions.JourneyAction.{RequestAsyncFunction, RequestFunction}
import controllers.changeGroupLead.FeatureGuard
import controllers.returns.AnotherBusinessExportWeightController
import forms.AnotherBusinessExportWeightFormProvider
import models.Mode.NormalMode
import models.UserAnswers
import models.requests.DataRequest
import navigation.{FakeNavigator, Navigator, ReturnsJourneyNavigator}
import org.mockito.{Answers, Mockito}
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.ArgumentMatchers.{eq => meq}
import org.mockito.Mockito.reset
import org.mockito.MockitoSugar.{mock, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import pages.returns.{AnotherBusinessExportWeightPage, DirectlyExportedComponentsPage, ExportedPlasticPackagingWeightPage, ImportedPlasticPackagingPage, ImportedPlasticPackagingWeightPage, ManufacturedPlasticPackagingPage, ManufacturedPlasticPackagingWeightPage, PlasticExportedByAnotherBusinessPage}
import play.api.data.Form
import play.api.data.Forms.{longNumber, text}
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.mvc.{Action, AnyContent, Call, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import queries.Gettable
import repositories.SessionRepository
import views.html.returns.AnotherBusinessExportWeightView

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future
import scala.util.Try

// todo: change this to proper unit test
class AnotherBusinessExportWeightControllerSpec extends PlaySpec with BeforeAndAfterEach{

  private val mockMessagesApi: MessagesApi = mock[MessagesApi]
  private val controllerComponents = stubMessagesControllerComponents()
  private val mockView = mock[AnotherBusinessExportWeightView]
  private val mockFormProvider = mock[AnotherBusinessExportWeightFormProvider]
  private val mockCache = mock[CacheConnector]
  private val journeyAction = mock[JourneyAction]
  private val dataRequest = mock[DataRequest[AnyContent]](Answers.RETURNS_DEEP_STUBS)
  private val form = mock[Form[Long]]
  private val mockNavigator = mock[ReturnsJourneyNavigator]


  val sut = new AnotherBusinessExportWeightController(
    mockMessagesApi,
    mockCache,
    mockNavigator,
    journeyAction,
    mockFormProvider,
    controllerComponents,
    mockView
  )(global)

  object TestException extends Exception("test")

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockMessagesApi,
      journeyAction,
      mockCache,
      mockNavigator,
      mockFormProvider,
      mockView,
      form,
      dataRequest
    )

    when(dataRequest.userAnswers.get(meq(ManufacturedPlasticPackagingWeightPage ))(any)).thenReturn(Some(50L))
    when(dataRequest.userAnswers.get(meq(ManufacturedPlasticPackagingPage))(any)).thenReturn(Some(true))
    when(dataRequest.userAnswers.get(meq(ImportedPlasticPackagingWeightPage))(any)).thenReturn(Some(50L))
    when(dataRequest.userAnswers.get(meq(ImportedPlasticPackagingPage))(any)).thenReturn(Some(true))
    when(dataRequest.userAnswers.get(meq(ExportedPlasticPackagingWeightPage))(any)).thenReturn(Some(20L))
    when(dataRequest.userAnswers.get(meq(DirectlyExportedComponentsPage))(any)).thenReturn(Some(true))
    when(dataRequest.userAnswers.get(meq(PlasticExportedByAnotherBusinessPage))(any)).thenReturn(Some(true))



    when(mockView.apply(any, any, any)(any, any)).thenReturn(Html("correct view"))
    when(mockFormProvider.apply()).thenReturn(form)
    when(dataRequest.userAnswers.fill(any[AnotherBusinessExportWeightPage.type], any)(any)).thenReturn(form)
    when(journeyAction.apply(any)) thenAnswer byConvertingFunctionArgumentsToAction
    when(journeyAction.async(any)) thenAnswer byConvertingFunctionArgumentsToFutureAction
    when(mockNavigator.exportedByAnotherBusinessWeightRoute(any, any)).thenReturn(Call("GET", "/foo"))
  }

  def byConvertingFunctionArgumentsToAction: (RequestFunction) => Action[AnyContent] = (function: RequestFunction) =>
    when(mock[Action[AnyContent]].apply(any))
      .thenAnswer((request: DataRequest[AnyContent]) =>
        Future.successful(function(request)))
      .getMock[Action[AnyContent]]

  def byConvertingFunctionArgumentsToFutureAction: (RequestAsyncFunction) => Action[AnyContent] = (function: RequestAsyncFunction) =>
    when(mock[Action[AnyContent]].apply(any))
      .thenAnswer((request: DataRequest[AnyContent]) => function(request))
      .getMock[Action[AnyContent]]

 // def onwardRoute = Call("GET", "/foo")

  //lazy val AnotherBusinessExportWeightRoute = controllers.returns.routes.AnotherBusinessExportWeightController.onPageLoad(NormalMode).url

  "onPageLoad" should {
    "invoke the journey action" in {
      Try(await(sut.onPageLoad(NormalMode)(FakeRequest())))
      verify(journeyAction).apply(any)
    }

    "return OK and correct view" in {
      val result = sut.onPageLoad(NormalMode).skippingJourneyAction(dataRequest)
     status(result) mustBe OK
      contentAsString(result) mustBe "correct view"
      verify(mockView).apply(meq(100L), meq(form), meq(NormalMode))(any, any)
    }

    "create the form" in {
      await(sut.onPageLoad(NormalMode).skippingJourneyAction(dataRequest))
      verify(mockFormProvider).apply()
    }

    "prepopulate the form" in {
      await(sut.onPageLoad(NormalMode).skippingJourneyAction(dataRequest))
      verify(dataRequest.userAnswers).fill(meq(AnotherBusinessExportWeightPage), meq(form))(any)
    }

   "redirect to index controller when total plastic cannot be calculated" in {
     when(dataRequest.userAnswers).thenReturn(UserAnswers("123"))

     val result = sut.onPageLoad(NormalMode)(dataRequest)

     status(result) mustEqual SEE_OTHER
     redirectLocation(result).value mustEqual controllers.routes.IndexController.onPageLoad.url
   }
  }

  "onSubmit" should {
    "invoke the journey action" in {
      when(journeyAction.async(any)) thenReturn mock[Action[AnyContent]]
      Try(await(sut.onSubmit(NormalMode)(FakeRequest())))
      verify(journeyAction).async(any)
    }


  }
//  "AnotherBusinessExportWeight Controller" - {
//
//    "must return OK and the correct view for a GET" in {
//
//      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
//
//      running(application) {
//        val request = FakeRequest(GET, AnotherBusinessExportWeightRoute)
//
//        val result = route(application, request).value
//
//        val view = application.injector.instanceOf[AnotherBusinessExportWeightView]
//
//        status(result) mustEqual OK
//        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
//      }
//    }
//
//    "must populate the view correctly on a GET when the question has previously been answered" in {
//
//      val userAnswers = UserAnswers(userAnswersId).set(AnotherBusinessExportWeightPage, validAnswer).success.value
//
//      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
//
//      running(application) {
//        val request = FakeRequest(GET, anotherBusinessExportWeightRoute)
//
//        val view = application.injector.instanceOf[AnotherBusinessExportWeightView]
//
//        val result = route(application, request).value
//
//        status(result) mustEqual OK
//        contentAsString(result) mustEqual view(form.fill(validAnswer), NormalMode)(request, messages(application)).toString
//      }
//    }
//
//    "must redirect to the next page when valid data is submitted" in {
//
//      val mockSessionRepository = mock[SessionRepository]
//
//      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
//
//      val application =
//        applicationBuilder(userAnswers = Some(emptyUserAnswers))
//          .overrides(
//            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
//            bind[SessionRepository].toInstance(mockSessionRepository)
//          )
//          .build()
//
//      running(application) {
//        val request =
//          FakeRequest(POST, anotherBusinessExportWeightRoute)
//            .withFormUrlEncodedBody(("value", validAnswer.toString))
//
//        val result = route(application, request).value
//
//        status(result) mustEqual SEE_OTHER
//        redirectLocation(result).value mustEqual onwardRoute.url
//      }
//    }
//
//    "must return a Bad Request and errors when invalid data is submitted" in {
//
//      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
//
//      running(application) {
//        val request =
//          FakeRequest(POST, anotherBusinessExportWeightRoute)
//            .withFormUrlEncodedBody(("value", "invalid value"))
//
//        val boundForm = form.bind(Map("value" -> "invalid value"))
//
//        val view = application.injector.instanceOf[AnotherBusinessExportWeightView]
//
//        val result = route(application, request).value
//
//        status(result) mustEqual BAD_REQUEST
//        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
//      }
//    }
//
//    "must redirect to Journey Recovery for a GET if no existing data is found" in {
//
//      val application = applicationBuilder(userAnswers = None).build()
//
//      running(application) {
//        val request = FakeRequest(GET, anotherBusinessExportWeightRoute)
//
//        val result = route(application, request).value
//
//        status(result) mustEqual SEE_OTHER
//        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
//      }
//    }
//
//    "must redirect to Journey Recovery for a POST if no existing data is found" in {
//
//      val application = applicationBuilder(userAnswers = None).build()
//
//      running(application) {
//        val request =
//          FakeRequest(POST, anotherBusinessExportWeightRoute)
//            .withFormUrlEncodedBody(("value", validAnswer.toString))
//
//        val result = route(application, request).value
//
//        status(result) mustEqual SEE_OTHER
//
//        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
//      }
//    }
//  }
//def createUserAnswer: UserAnswers = {
//  UserAnswers("123")
//    .set(ManufacturedPlasticPackagingPage, true, cleanup = false).get
//    .set(ManufacturedPlasticPackagingWeightPage, 50L, cleanup = false).get
//    .set(ImportedPlasticPackagingPage, true, cleanup = false).get
//    .set(ImportedPlasticPackagingWeightPage, 50L, cleanup = false).get
//    .set(DirectlyExportedComponentsPage, true, cleanup = false).get
//    .set(ExportedPlasticPackagingWeightPage, 20L, cleanup = false).get
//    .set(PlasticExportedByAnotherBusinessPage, true, cleanup = false).get
//}
}
