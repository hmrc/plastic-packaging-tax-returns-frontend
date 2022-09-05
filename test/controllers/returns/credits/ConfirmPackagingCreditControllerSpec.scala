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

package controllers.returns.credits

import base.SpecBase
import connectors.{CacheConnector, DownstreamServiceError, ExportCreditsConnector}
import models.ExportCreditBalance
import models.Mode.NormalMode
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.returns.credits.ConfirmPackagingCreditView

import scala.concurrent.Future

class ConfirmPackagingCreditControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {


  private def onwardRoute = Call("GET", "/foo")

  val validAnswer: BigDecimal = 1.25

  private lazy val convertedPackagingCreditRoute =
    controllers.returns.credits.routes.ConfirmPackagingCreditController.onPageLoad().url

  private val exportCreditConnector = mock[ExportCreditsConnector]

  private val view = mock[ConfirmPackagingCreditView]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(exportCreditConnector, view)

    when(exportCreditConnector.get(any(), any(), any())(any())).thenReturn(Future.successful(Right(ExportCreditBalance(
      totalPPTCharges = 0.0, totalExportCreditClaimed = 0.0, totalExportCreditAvailable = 123.45))))

    when(view.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  private def buildApplication = {
    applicationBuilder(userAnswers = None).overrides(
      bind[ExportCreditsConnector].toInstance(exportCreditConnector),
      bind[ConfirmPackagingCreditView].toInstance(view),
      bind[CacheConnector].toInstance(cacheConnector),
    ).build()
  }

  "ConvertedPackagingCredit Controller" - {

    "must return OK and the correct view for a GET" ignore {

      val application = buildApplication

      running(application) {
        val request = FakeRequest(GET, convertedPackagingCreditRoute)
        val controller = application.injector.instanceOf[ConfirmPackagingCreditController]
        val result = controller.onPageLoad(NormalMode)(request)
        status(result) mustEqual OK
      }

      verify(view).apply(ArgumentMatchers.eq(Some("200")), any())(any(), any())
    }

    "must handle the credit balance being unavailable" ignore {
      when(exportCreditConnector.get(any(), any(), any())(any())).thenReturn(Future.successful(Left(
        DownstreamServiceError("error", new Exception)
      )))
      val application = buildApplication

      running(application) {
        val request = FakeRequest(GET, convertedPackagingCreditRoute)
        val controller = application.injector.instanceOf[ConfirmPackagingCreditController]
        val result = controller.onPageLoad(NormalMode)(request)
        status(result) mustEqual OK
      }

      verify(view).apply(ArgumentMatchers.eq(Some("200")), any())(any(), any())
    }

    // TODO reword tests below...

    "must redirect to the next page when valid data is submitted" ignore {

      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any(), any())(any())) thenReturn Future.successful(mockResponse)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
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
        redirectLocation(result).value mustEqual controllers.returns.routes.ManufacturedPlasticPackagingController.onPageLoad(NormalMode).url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" ignore {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, convertedPackagingCreditRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))


        val view = application.injector.instanceOf[ConfirmPackagingCreditView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(meq(Some("balance")), any())(request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" ignore {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, convertedPackagingCreditRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad.url
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

        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad.url
      }
    }
  }

}
