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

import org.apache.pekko.stream.testkit.NoMaterializer
import base.FakeIdentifierActionWithEnrolment
import base.utils.NonExportedPlasticTestHelper
import connectors.CacheConnector
import controllers.actions.{DataRequiredActionImpl, FakeDataRetrievalAction}
import controllers.helpers.NonExportedAmountHelper
import forms.returns.NonExportedHumanMedicinesPlasticPackagingWeightFormProvider
import models.Mode.NormalMode
import models.UserAnswers
import navigation.ReturnsJourneyNavigator
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import pages.returns.NonExportedHumanMedicinesPlasticPackagingWeightPage
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import repositories.SessionRepository
import uk.gov.hmrc.http.HttpResponse
import views.html.returns.NonExportedHumanMedicinesPlasticPackagingWeightView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class NonExportedHumanMedicinesPlasticPackagingWeightControllerSpec
    extends PlaySpec
    with MockitoSugar
    with BeforeAndAfterEach {

  private val userAnswers = UserAnswers("123")
  private val validAnswer = 0L

  private val manufacturedAmount              = 200L
  private val importedAmount                  = 100L
  private val exportedAmount                  = 50L
  private val exportedByAnotherBusinessAmount = 50L
  private val nonExportedAmount =
    manufacturedAmount + importedAmount - (exportedAmount + exportedByAnotherBusinessAmount)
  private val nonExportedAnswer = NonExportedPlasticTestHelper.createUserAnswer(
    exportedAmount,
    exportedByAnotherBusinessAmount,
    manufacturedAmount,
    importedAmount
  )
  private val mockCacheConnector      = mock[CacheConnector]
  private val mockNavigator           = mock[ReturnsJourneyNavigator]
  private val formProvider            = new NonExportedHumanMedicinesPlasticPackagingWeightFormProvider()
  private val mockView                = mock[NonExportedHumanMedicinesPlasticPackagingWeightView]
  private val nonExportedAmountHelper = mock[NonExportedAmountHelper]
  private implicit val mockSessionRepository: SessionRepository = mock[SessionRepository]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockView, mockCacheConnector, mockNavigator, nonExportedAmountHelper, mockSessionRepository)

    when(mockView.apply(any(), any(), any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(nonExportedAmountHelper.getAmountAndDirectlyExportedAnswer(any())).thenReturn(Some((200L, true, true)))
    when(mockSessionRepository.get[Boolean](any, any)(any)).thenReturn(Future.successful(Some(false)))
  }

  "onPageLoad" should {
    "return OK and the correct view" in {
      val result = createSut(Some(nonExportedAnswer))
        .onPageLoad(NormalMode)(
          FakeRequest(GET, "")
        )

      status(result) mustEqual OK
      verifyView(formProvider())
    }

    "populate the view correctly when the question has previously been answered" in {

      val ans = nonExportedAnswer.set(NonExportedHumanMedicinesPlasticPackagingWeightPage, validAnswer).get

      val result = createSut(Some(ans)).onPageLoad(NormalMode)(FakeRequest(GET, ""))

      status(result) mustEqual OK
      verifyView(formProvider().bind(Map("value" -> "0")))
    }

    "redirect if required user answers are missing" in {
      when(nonExportedAmountHelper.getAmountAndDirectlyExportedAnswer(any())).thenReturn(None)
      val result = createSut(Some(userAnswers)).onPageLoad(NormalMode)(FakeRequest(GET, ""))
      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.IndexController.onPageLoad.url
    }

    "redirect if user answers are missing" in {
      val result = createSut(None).onPageLoad(NormalMode)(FakeRequest(GET, ""))
      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad.url
    }
  }

  "onSubmit" should {

    "redirect to the next page" in {
      def onwardRoute = Call("GET", "/foo")
      val userAnswersWithExportAmount =
        userAnswers.set(NonExportedHumanMedicinesPlasticPackagingWeightPage, value = 8L).get
      when(mockNavigator.nonExportedHumanMedicinesPlasticPackagingWeightPage(any())) thenReturn onwardRoute
      when(mockCacheConnector.set(any(), any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val result = createSut(Some(userAnswersWithExportAmount)).onSubmit(NormalMode)(
        FakeRequest(POST, "").withFormUrlEncodedBody(("value", validAnswer.toString))
      )

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "return a Bad Request and errors" when {
      "DirectlyExportedPage answer is yes" in {

        val result = createSut(Some(nonExportedAnswer)).onSubmit(NormalMode)(
          FakeRequest(POST, "").withFormUrlEncodedBody(("value", "invalid value"))
        )

        status(result) mustEqual BAD_REQUEST
        verifyView(formProvider().bind(Map("value" -> "invalid value")))
      }
    }

    "redirect to Journey Recovery if no existing data is found" in {

      val result = createSut(None).onSubmit(NormalMode)(
        FakeRequest(POST, "").withFormUrlEncodedBody(("value", validAnswer.toString))
      )

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad.url

    }

    "set the cache" in {
      when(mockNavigator.nonExportedHumanMedicinesPlasticPackagingWeightPage(any())).thenReturn(Call(GET, "foo"))
      when(mockCacheConnector.set(any(), any())(any())).thenReturn(Future.successful(mock[HttpResponse]))

      await(
        createSut(Some(nonExportedAnswer)).onSubmit(NormalMode)(
          FakeRequest(POST, "").withFormUrlEncodedBody(("value", "10"))
        )
      )

      val expectedUserAnswer = nonExportedAnswer.set(NonExportedHumanMedicinesPlasticPackagingWeightPage, 10L).get
      verify(mockCacheConnector).set(any(), ArgumentMatchers.eq(expectedUserAnswer))(any())

    }
  }

  private def createSut(userAnswer: Option[UserAnswers]): NonExportedHumanMedicinesPlasticPackagingWeightController = {
    new NonExportedHumanMedicinesPlasticPackagingWeightController(
      mock[MessagesApi],
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

  private def verifyView(
    expectedForm: Form[Long],
    expectedAmount: Long = nonExportedAmount,
    expectedDirectlyExportedAnswer: Boolean = true,
    expectedAnotherBusinessExportedAnswer: Boolean = true
  ): HtmlFormat.Appendable = {
    verify(mockView).apply(
      ArgumentMatchers.eq(expectedAmount),
      ArgumentMatchers.eq(expectedForm),
      ArgumentMatchers.eq(NormalMode),
      ArgumentMatchers.eq(expectedDirectlyExportedAnswer),
      ArgumentMatchers.eq(expectedAnotherBusinessExportedAnswer)
    )(any(), any())
  }
}
