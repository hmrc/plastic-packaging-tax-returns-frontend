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

import akka.stream.testkit.NoMaterializer
import base.FakeIdentifierActionWithEnrolment
import base.utils.NonExportedPlasticTestHelper
import connectors.CacheConnector
import controllers.actions.{DataRequiredActionImpl, FakeDataRetrievalAction}
import controllers.{routes => appRoutes}
import forms.returns.NonExportedRecycledPlasticPackagingWeightFormProvider
import models.Mode.NormalMode
import models.UserAnswers
import navigation.Navigator
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import pages.returns.{DirectlyExportedComponentsPage, NonExportedRecycledPlasticPackagingWeightPage}
import play.api.i18n.MessagesApi
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.http.HttpResponse
import views.html.returns.NonExportedRecycledPlasticPackagingWeightView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class NonExportedRecycledPlasticPackagingWeightControllerSpec
  extends PlaySpec
    with MockitoSugar
    with BeforeAndAfterEach {

  private val formProvider = new NonExportedRecycledPlasticPackagingWeightFormProvider()
  private def onwardRoute = Call("GET", "/foo")

  private val validAnswer: Long = 10L
  private val manufacturedAmount = 200L
  private val importedAmount = 100L
  private val exportedAmount = 50L
  private val nonExportedAmount = manufacturedAmount + importedAmount - exportedAmount
  private val nonExportedAnswer = NonExportedPlasticTestHelper.createUserAnswer(exportedAmount, manufacturedAmount, importedAmount)

  private val recycledPlasticPackagingWeightRoute =
    controllers.returns.routes.NonExportedRecycledPlasticPackagingWeightController.onPageLoad(NormalMode).url

  private val mockMessagesApi= mock[MessagesApi]
  private val mockCacheConnector= mock[CacheConnector]
  private val mockNavigator =  mock[Navigator]
  private val mockView = mock[NonExportedRecycledPlasticPackagingWeightView]

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(mockView, mockCacheConnector)
    when(mockView.apply(any(), any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  "onPageLoad" should {

    "return OK and correct View" when {
      "directly exported answer is No" in {

        val ans = nonExportedAnswer.set(DirectlyExportedComponentsPage, false).get
          .set(NonExportedRecycledPlasticPackagingWeightPage, validAnswer).get

        val result = createSut(Some(ans)).onPageLoad(NormalMode)(
          FakeRequest(GET, recycledPlasticPackagingWeightRoute)
        )

        status(result) mustEqual OK
        verify(mockView).apply(
          ArgumentMatchers.eq(formProvider().fill(validAnswer)),
          ArgumentMatchers.eq(NormalMode),
          ArgumentMatchers.eq(manufacturedAmount + importedAmount),
          ArgumentMatchers.eq(false)
        )(any(), any())
      }

      "directly exported answer is Yes" in {
        val ans = nonExportedAnswer
          .set(NonExportedRecycledPlasticPackagingWeightPage, validAnswer).get

        val result = createSut(Some(ans)).onPageLoad(NormalMode)(
          FakeRequest(GET, recycledPlasticPackagingWeightRoute)
        )

        status(result) mustEqual OK
        verify(mockView).apply(
          ArgumentMatchers.eq(formProvider().fill(validAnswer)),
          ArgumentMatchers.eq(NormalMode),
          ArgumentMatchers.eq(nonExportedAmount),
          ArgumentMatchers.eq(true)
        )(any(), any())
      }
    }

    "redirect GET to home page when exported amount not found" in {

      val ans = UserAnswers("123").set(NonExportedRecycledPlasticPackagingWeightPage, validAnswer).get

      val result = createSut(Some(ans)).onPageLoad(NormalMode)(
        FakeRequest(GET, recycledPlasticPackagingWeightRoute)
      )

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual appRoutes.IndexController.onPageLoad.url

    }

    "redirect to Journey Recovery for a GET if no existing data is found" in {

      val result = createSut(None).onPageLoad(NormalMode)(
        FakeRequest(GET, recycledPlasticPackagingWeightRoute)
      )

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad.url
    }
  }

  "onSubmit" should {

    "redirect to the next page when valid data is submitted" in {

      when(mockCacheConnector.set(any(), any())(any())) thenReturn Future.successful(mock[HttpResponse])
      when(mockNavigator.nextPage(any(), any(), any())).thenReturn(onwardRoute)

      val result = createSut(Some(UserAnswers("123")))
        .onSubmit(NormalMode)(fakePostRequestWithBody(("value", validAnswer.toString)))

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "set up the cache" in {
      when(mockCacheConnector.set(any(), any())(any())) thenReturn Future.successful(mock[HttpResponse])
      when(mockNavigator.nextPage(any(), any(), any())).thenReturn(onwardRoute)

      await(
        createSut(Some(nonExportedAnswer))
        .onSubmit(NormalMode)(fakePostRequestWithBody(("value", validAnswer.toString)))
      )

      val expectedUserAnswer = nonExportedAnswer.set(NonExportedRecycledPlasticPackagingWeightPage, validAnswer).get
      verify(mockCacheConnector).set(any(), ArgumentMatchers.eq(expectedUserAnswer))(any())
    }
    "return a Bad Request and the corrected view" when {
      "directly exported answer is Yes" in {

        val result = createSut(Some(nonExportedAnswer))
          .onSubmit(NormalMode)(fakePostRequestWithBody(("value", "invalid value")))

        status(result) mustEqual BAD_REQUEST
        verify(mockView).apply(
          ArgumentMatchers.eq(formProvider().bind(Map("value" -> "invalid value"))),
          ArgumentMatchers.eq(NormalMode),
          ArgumentMatchers.eq(nonExportedAmount),
          ArgumentMatchers.eq(true)
        )(any(), any())

      }

      "directly exported answer is No" in {
        val userAnswer = nonExportedAnswer.set(DirectlyExportedComponentsPage, false).get

        val result = createSut(Some(userAnswer))
          .onSubmit(NormalMode)(fakePostRequestWithBody(("value", "invalid value")))

        status(result) mustEqual BAD_REQUEST
        verify(mockView).apply(
          ArgumentMatchers.eq(formProvider().bind(Map("value" -> "invalid value"))),
          ArgumentMatchers.eq(NormalMode),
          ArgumentMatchers.eq(manufacturedAmount + importedAmount),
          ArgumentMatchers.eq(false)
        )(any(), any())
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val result = createSut(None)
        .onSubmit(NormalMode)(fakePostRequestWithBody(("value", validAnswer.toString)))

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad.url
    }
  }

  private def fakePostRequestWithBody(body: (String, String)) = {
    FakeRequest(POST, recycledPlasticPackagingWeightRoute)
      .withFormUrlEncodedBody(body)
  }

  private def createSut(userAnswer: Option[UserAnswers]) =
    new NonExportedRecycledPlasticPackagingWeightController(
      mockMessagesApi,
      mockCacheConnector,
      mockNavigator,
      new FakeIdentifierActionWithEnrolment(stubPlayBodyParsers(NoMaterializer)),
      new FakeDataRetrievalAction(userAnswer),
      new DataRequiredActionImpl(),
      formProvider,
      stubMessagesControllerComponents(),
      mockView
  )
}
