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

package controllers

import base.SpecBase
import cacheables.ObligationCacheable
import connectors.{CacheConnector, DownstreamServiceError, ExportCreditsConnector}
import forms.ConvertedPackagingCreditFormProvider
import models.returns.TaxReturnObligation
import models.{ExportCreditBalance, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.{any, eq}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.ConvertedPackagingCreditPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.http.HeaderCarrier
import views.html.ConvertedPackagingCreditView

import java.time.LocalDate
import scala.concurrent.Future

class ConvertedPackagingCreditControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  val formProvider = new ConvertedPackagingCreditFormProvider()
  val form         = formProvider()

  def onwardRoute = Call("GET", "/foo")

  val validAnswer: BigDecimal = 1.25

  lazy val convertedPackagingCreditRoute =
    routes.ConvertedPackagingCreditController.onPageLoad(NormalMode).url

  private val aDate = LocalDate.ofEpochDay(0)
  private val test = UserAnswers("1").set(ObligationCacheable, TaxReturnObligation(
    fromDate = aDate, toDate = aDate, dueDate = aDate, periodKey = "bla")).get

  private val exportCreditConnector = mock[ExportCreditsConnector]

  private val view = mock[ConvertedPackagingCreditView]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(exportCreditConnector, view)

    when(exportCreditConnector.get(any(), any(), any())(any())).thenReturn(Future.successful(Right(ExportCreditBalance(
      totalPPTCharges = 0.0, totalExportCreditClaimed = 0.0, totalExportCreditAvailable = 123.45))))

    when(view.apply(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  private def buildApplication = {
    applicationBuilder(userAnswers = Some(test)).overrides(
      bind[ExportCreditsConnector].toInstance(exportCreditConnector),
      bind[ConvertedPackagingCreditView].toInstance(view),
      bind[CacheConnector].toInstance(cacheConnector),
    ).build()
  }

  "ConvertedPackagingCredit Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = buildApplication

      running(application) {
        val request = FakeRequest(GET, convertedPackagingCreditRoute)
        val controller = application.injector.instanceOf[ConvertedPackagingCreditController]
        val result = controller.onPageLoad(NormalMode)(request)
        status(result) mustEqual OK
      }

      verify(view).apply(any(), ArgumentMatchers.eq(NormalMode), ArgumentMatchers.eq(Some("£123.45")))(any(), any())
    }

    "must handle the credit balance being unavailable" in {
      when(exportCreditConnector.get(any(), any(), any())(any())).thenReturn(Future.successful(Left(
        DownstreamServiceError("error", new Exception)
      )))
      val application = buildApplication

      running(application) {
        val request = FakeRequest(GET, convertedPackagingCreditRoute)
        val controller = application.injector.instanceOf[ConvertedPackagingCreditController]
        val result = controller.onPageLoad(NormalMode)(request)
        status(result) mustEqual OK
      }

      verify(view).apply(any(), ArgumentMatchers.eq(NormalMode), ArgumentMatchers.eq(None))(any(), any())
    }

    // TODO reword tests below...

    "must populate the view correctly on a GET when the question has previously been answered" ignore {

      val userAnswers =
        UserAnswers(userAnswersId).set(ConvertedPackagingCreditPage, validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, convertedPackagingCreditRoute)

        val view = application.injector.instanceOf[ConvertedPackagingCreditView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), NormalMode, Some("balance"))(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" ignore {

      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any(), any())(any())) thenReturn Future.successful(mockResponse)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
                     bind[CacheConnector].toInstance(mockCacheConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, convertedPackagingCreditRoute)
            .withFormUrlEncodedBody(("value", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" ignore {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, convertedPackagingCreditRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[ConvertedPackagingCreditView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, Some("balance"))(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" ignore {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, convertedPackagingCreditRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" ignore {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, convertedPackagingCreditRoute)
            .withFormUrlEncodedBody(("value", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

}
