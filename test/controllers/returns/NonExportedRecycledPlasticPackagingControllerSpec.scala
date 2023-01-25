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

package controllers.returns

import akka.stream.testkit.NoMaterializer
import base.FakeIdentifierActionWithEnrolment
import base.utils.NonExportedPlasticTestHelper
import connectors.CacheConnector
import controllers.actions.{DataRequiredActionImpl, FakeDataRetrievalAction}
import controllers.helpers.NonExportedAmountHelper
import forms.returns.NonExportedRecycledPlasticPackagingFormProvider
import models.Mode.NormalMode
import models.UserAnswers
import navigation.Navigator
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.mockito.{ArgumentCaptor, ArgumentMatchers}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import pages.returns.{DirectlyExportedPage, NonExportedHumanMedicinesPlasticPackagingWeightPage, NonExportedRecycledPlasticPackagingPage, AnotherBusinessExportedPage}
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.http.HttpResponse
import views.html.returns.NonExportedRecycledPlasticPackagingView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class NonExportedRecycledPlasticPackagingControllerSpec extends PlaySpec with MockitoSugar with BeforeAndAfterEach {

  def onwardRoute = Call("GET", "/foo")
  private val mockMessageApi = mock[MessagesApi]
  private val mockCacheConnector = mock[CacheConnector]
  private val mockNavigator = mock[Navigator]
  private val mockView = mock[NonExportedRecycledPlasticPackagingView]
  private val mockNonExportedAmountHelper = mock[NonExportedAmountHelper]

  private val validAnswer = 0L
  private val manufacturedAmount = 200L
  private val importedAmount = 100L
  private val exportedAmount = 50L
  private val exportedByAnotherBusinessAmount = 50L
  private val nonExportedAmount = manufacturedAmount + importedAmount - (exportedAmount + exportedByAnotherBusinessAmount)
  private val nonExportedAnswer = NonExportedPlasticTestHelper.createUserAnswer(exportedAmount, exportedByAnotherBusinessAmount, manufacturedAmount, importedAmount)

  private val recycledPlasticPackagingRoute = controllers.returns.routes.NonExportedRecycledPlasticPackagingController.onPageLoad(NormalMode).url

  override def beforeEach() = {
    super.beforeEach()
    reset(mockView, mockCacheConnector, mockNavigator, mockNonExportedAmountHelper)

    when(mockView.apply(any(), any(), any(), any())(any(),any())).thenReturn(HtmlFormat.empty)
    when(mockNonExportedAmountHelper.getAmountAndDirectlyExportedAnswer(any())).thenReturn(Some((200L, true, true)))
  }

  "onPageLoad" should {

    "return OK with an empty form" in {
      val ans = nonExportedAnswer
        .set(NonExportedHumanMedicinesPlasticPackagingWeightPage, validAnswer).get

      val result = createSut(userAnswer = Some(ans)).onPageLoad(NormalMode)(
        FakeRequest(GET, recycledPlasticPackagingRoute)
      )

      status(result) mustEqual OK
      verifyView(None, nonExportedAmount)
    }

    "populate the view correctly when the question has previously been answered" in {

      val userAnswers = nonExportedAnswer.set(NonExportedRecycledPlasticPackagingPage, true).get

      val result = createSut(userAnswer = Some(userAnswers)).onPageLoad(NormalMode)(
        FakeRequest(GET, recycledPlasticPackagingRoute)
      )

      status(result) mustEqual OK
      verifyView(Some(true), nonExportedAmount)
    }

    "redirect to the home page if directly exported answer is missing" in {
      when(mockNonExportedAmountHelper.getAmountAndDirectlyExportedAnswer(any())).thenReturn(None)
      val ans = nonExportedAnswer.remove(DirectlyExportedPage).get

      val result = createSut(userAnswer = Some(ans)).onPageLoad(NormalMode)(
        FakeRequest(GET, recycledPlasticPackagingRoute)
      )

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.IndexController.onPageLoad.url
    }
  }

  "onSubmit" should {
    "redirect to the next page when valid data is submitted" in {

      when(mockCacheConnector.set(any(), any())(any())) thenReturn Future.successful(mock[HttpResponse])
      when(mockNavigator.nextPage(any(),any(),any())).thenReturn(onwardRoute)

      val result = createSut(userAnswer = Some(UserAnswers("123"))).onSubmit(NormalMode)(
        FakeRequest(POST, recycledPlasticPackagingRoute)
          .withFormUrlEncodedBody(("value", "true"))
      )

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val result = createSut(userAnswer = Some(nonExportedAnswer)).onSubmit(NormalMode)(
        FakeRequest(POST, recycledPlasticPackagingRoute)
          .withFormUrlEncodedBody(("value", ""))
      )

      status(result) mustEqual BAD_REQUEST
      verifyView(None, nonExportedAmount)
    }

    "redirect to home page is directly exported not answered" in {
      when(mockNonExportedAmountHelper.getAmountAndDirectlyExportedAnswer(any())).thenReturn(None)
      val ans = nonExportedAnswer.remove(DirectlyExportedPage).get

      val result = createSut(userAnswer = Some(ans)).onSubmit(NormalMode)(
        FakeRequest(POST, recycledPlasticPackagingRoute)
          .withFormUrlEncodedBody(("value", ""))
      )

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.IndexController.onPageLoad.url
    }

    "redirect to Journey Recovery for a GET if no existing data is found" in {

      val result = createSut(userAnswer = None).onSubmit(NormalMode)(
        FakeRequest(POST, recycledPlasticPackagingRoute)
          .withFormUrlEncodedBody(("value", "true"))
      )

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad.url
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val result = createSut(userAnswer = None).onSubmit(NormalMode)(
        FakeRequest(POST, recycledPlasticPackagingRoute)
          .withFormUrlEncodedBody(("value", "true"))
      )

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad.url
    }
  }

  private def verifyView(expectedIsYesNo: Option[Boolean], amount: Long, expectedDirectlyExportedYesNoAnswer: Boolean = true): Unit = {
    val captor: ArgumentCaptor[Form[Boolean]] = ArgumentCaptor.forClass(classOf[Form[Boolean]])

    verify(mockView).apply(
      captor.capture(),
      ArgumentMatchers.eq(NormalMode),
      ArgumentMatchers.eq(amount),
      ArgumentMatchers.eq(expectedDirectlyExportedYesNoAnswer)
    )(any(),any())

    captor.getValue.value mustBe expectedIsYesNo
  }
  private def createSut(
                         formProvider: NonExportedRecycledPlasticPackagingFormProvider = new NonExportedRecycledPlasticPackagingFormProvider(),
                         userAnswer: Option[UserAnswers]
                       ): NonExportedRecycledPlasticPackagingController = {
    new NonExportedRecycledPlasticPackagingController(
      mockMessageApi,
      mockCacheConnector,
      mockNavigator,
      new FakeIdentifierActionWithEnrolment(stubPlayBodyParsers(NoMaterializer)),
      new FakeDataRetrievalAction(userAnswer),
      new DataRequiredActionImpl(),
      formProvider,
      stubMessagesControllerComponents(),
      mockView,
      mockNonExportedAmountHelper
    )
  }
}
