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
import controllers.{routes => appRoutes}
import forms.returns.NonExportedHumanMedicinesPlasticPackagingFormProvider
import models.Mode.NormalMode
import models.UserAnswers
import navigation.Navigator
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.mockito.{ArgumentCaptor, ArgumentMatchers}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import pages.returns.{AnotherBusinessExportedPage, DirectlyExportedPage, NonExportedHumanMedicinesPlasticPackagingPage}
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.http.HttpResponse
import views.html.returns.NonExportedHumanMedicinesPlasticPackagingView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class NonExportedHumanMedicinesPlasticPackagingControllerSpec extends PlaySpec with MockitoSugar with BeforeAndAfterEach{

  def onwardRoute = Call("GET", "/foo")

  private val nonExportedHumanMedicinesPlasticPackagingRoute = routes.NonExportedHumanMedicinesPlasticPackagingController.onPageLoad(NormalMode).url

  val manufacturedAmount = 200L
  val importedAmount = 100L
  val exportedAmount = 50L
  val exportedByAnotherBusinessAmount = 50L
  val nonExportedAmount = (manufacturedAmount + importedAmount) - (exportedAmount + exportedByAnotherBusinessAmount)

  private val mockMessagesApi: MessagesApi = mock[MessagesApi]
  private val mockCacheConnector = mock[CacheConnector]
  private val mockNavigator = mock[Navigator]
  private val mockView = mock[NonExportedHumanMedicinesPlasticPackagingView]
  private val nonExportedAmountHelper = mock[NonExportedAmountHelper]

  private val nonExportedAnswer = NonExportedPlasticTestHelper.createUserAnswer(exportedAmount, exportedByAnotherBusinessAmount, manufacturedAmount, importedAmount)

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(mockView, mockNavigator, mockCacheConnector, nonExportedAmountHelper)
    when(mockView.apply(any(),any(),any(), any(), any())(any(),any())).thenReturn(HtmlFormat.empty)
    when(nonExportedAmountHelper.getAmountAndDirectlyExportedAnswer(any())).thenReturn(Some((200L,true, true)))
  }


  "NonExportedHumanMedicinesPlasticPackaging Controller" should {

    "return OK and the correct view for a GET" in {
      val result = createSut(userAnswer = Some(nonExportedAnswer))
        .onPageLoad(NormalMode)(FakeRequest(GET, nonExportedHumanMedicinesPlasticPackagingRoute))

      status(result) mustEqual OK
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = nonExportedAnswer
        .set(NonExportedHumanMedicinesPlasticPackagingPage, true)
        .success
        .value

      val result = createSut(userAnswer = Some(userAnswers)).onPageLoad(NormalMode)(FakeRequest(GET, nonExportedHumanMedicinesPlasticPackagingRoute))

      status(result) mustEqual OK

      val captor: ArgumentCaptor[Form[Boolean]] = ArgumentCaptor.forClass(classOf[Form[Boolean]])
      verify(mockView).apply(
        ArgumentMatchers.eq(nonExportedAmount),
        captor.capture(),
        ArgumentMatchers.eq(NormalMode),
        ArgumentMatchers.eq(true),
        ArgumentMatchers.eq(true)
      )(any(),any())

      captor.getValue.value mustBe Some(true)
    }

    "redirect GET to home page when DirectlyExportedComponentsPage and PlasticExportedByAnotherBusinessPage amount not found" in {
      when(nonExportedAmountHelper.getAmountAndDirectlyExportedAnswer(any())).thenReturn(None)
      val result = createSut(userAnswer = Some(nonExportedAnswer.remove(DirectlyExportedPage).success.value
        .remove(AnotherBusinessExportedPage).success.value
      ))
        .onPageLoad(NormalMode)(FakeRequest(GET, nonExportedHumanMedicinesPlasticPackagingRoute))

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual appRoutes.IndexController.onPageLoad.url
    }

    "redirect to the next page when valid data is submitted" in {

      when(mockCacheConnector.set(any(), any())(any())) thenReturn Future.successful(mock[HttpResponse])
      when(mockNavigator.nextPage(any(),any(),any())).thenReturn(Call(GET, "/faa"))

      val result = createSut(userAnswer = Some(nonExportedAnswer))
        .onSubmit(NormalMode)(FakeRequest(POST, nonExportedHumanMedicinesPlasticPackagingRoute)
        .withFormUrlEncodedBody(("value", "true")))

        status(result) mustEqual SEE_OTHER

      val expectedAnswer = nonExportedAnswer.set(NonExportedHumanMedicinesPlasticPackagingPage, true).get
        verify(mockNavigator).nextPage(
          ArgumentMatchers.eq(NonExportedHumanMedicinesPlasticPackagingPage),
            ArgumentMatchers.eq(NormalMode),
            ArgumentMatchers.eq(expectedAnswer)
      )

      verify(mockCacheConnector).set(any(), ArgumentMatchers.eq(expectedAnswer))(any())
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val result = createSut(userAnswer = Some(nonExportedAnswer))
        .onSubmit(NormalMode)(FakeRequest(POST, nonExportedHumanMedicinesPlasticPackagingRoute)
          .withFormUrlEncodedBody(("value", "")))

        status(result) mustEqual BAD_REQUEST
    }

    "redirect Post to the home page is DirectlyExportedComponentsPage and PlasticExportedByAnotherBusinessPage question is not answered" in {
      when(nonExportedAmountHelper.getAmountAndDirectlyExportedAnswer(any())).thenReturn(None)
      val result = createSut(userAnswer = Some(nonExportedAnswer.remove(DirectlyExportedPage).success.value
        .remove(AnotherBusinessExportedPage).success.value
      ))
        .onSubmit(NormalMode)(FakeRequest(POST, nonExportedHumanMedicinesPlasticPackagingRoute)
          .withFormUrlEncodedBody(("value", "")))

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.IndexController.onPageLoad.url
    }
    "redirect to Journey Recovery for a GET if no existing data is found" in {

      val result = createSut()
        .onPageLoad(NormalMode)(FakeRequest(GET, nonExportedHumanMedicinesPlasticPackagingRoute))

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad.url
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val result = createSut()
        .onSubmit(NormalMode)(FakeRequest(POST, nonExportedHumanMedicinesPlasticPackagingRoute)
          .withFormUrlEncodedBody(("value", "true")))

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad.url
    }
  }

  private def createSut(
    formProvider: NonExportedHumanMedicinesPlasticPackagingFormProvider = new NonExportedHumanMedicinesPlasticPackagingFormProvider(),
    userAnswer: Option[UserAnswers] = None
  ): NonExportedHumanMedicinesPlasticPackagingController = {
    new NonExportedHumanMedicinesPlasticPackagingController(
      mockMessagesApi,
      mockCacheConnector,
      mockNavigator,
      new FakeIdentifierActionWithEnrolment(stubPlayBodyParsers(NoMaterializer)),
      new FakeDataRetrievalAction(userAnswer),
      new DataRequiredActionImpl(),
      formProvider,
      stubMessagesControllerComponents(),
      mockView,
      nonExportedAmountHelper
    )
  }
}
