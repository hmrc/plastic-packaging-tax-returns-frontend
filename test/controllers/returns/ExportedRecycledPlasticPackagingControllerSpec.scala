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

import base.SpecBase
import connectors.CacheConnector
import controllers.returns.{routes => returnsRoutes}
import controllers.{routes => appRoutes}
import forms.returns.ExportedRecycledPlasticPackagingFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.returns.{ExportedPlasticPackagingWeightPage, ExportedRecycledPlasticPackagingPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.auth.core.InsufficientEnrolments
import uk.gov.hmrc.http.HttpResponse
import views.html.returns.{ExportedRecycledPlasticPackagingView, ExportedRecycledPlasticPackagingWeightView}

import scala.concurrent.Future

class ExportedRecycledPlasticPackagingControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new ExportedRecycledPlasticPackagingFormProvider()
  val form = formProvider()

  lazy val exportedRecycledPlasticPackagingRoute = returnsRoutes.ExportedRecycledPlasticPackagingController.onPageLoad(NormalMode).url
  val view = mock[ExportedRecycledPlasticPackagingView]

  val exportedAmount: Long = 200L

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(view)
    when(view.apply(any(), any(), any())(any(),any())).thenReturn(HtmlFormat.empty)
  }
  "ExportedRecycledPlasticPackaging Controller" - {

    "onLoadPage" - {
      "must return OK and display the exported plastic packaging " in {

        val application = applicationBuilder(userAnswers = createUserAnswer(true))
          .overrides(bind[ExportedRecycledPlasticPackagingView].toInstance(view))
          .build()

        running(application) {
          val request = FakeRequest(GET, exportedRecycledPlasticPackagingRoute)

          val result = route(application, request).value

          status(result) mustEqual OK

          val captor: ArgumentCaptor[Long] = ArgumentCaptor.forClass(classOf[Long])
          verify(view).apply(any(), any(), captor.capture())(any(), any())
          captor.getValue mustBe exportedAmount
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered YES" in {
        val application = applicationBuilder(userAnswers = createUserAnswer(true))
          .overrides(bind[ExportedRecycledPlasticPackagingView].toInstance(view))
          .build()

        running(application) {
          val request = FakeRequest(GET, exportedRecycledPlasticPackagingRoute)

          val result = route(application, request).value

          status(result) mustEqual OK
          val captor: ArgumentCaptor[Form[ExportedRecycledPlasticPackagingFormProvider]] = ArgumentCaptor.forClass(classOf[Form[ExportedRecycledPlasticPackagingFormProvider]])
          verify(view).apply(captor.capture(),any(), any())(any(), any())

          captor.getValue.value mustBe Some(true)
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered No" in {
        val application = applicationBuilder(userAnswers = createUserAnswer(false))
          .overrides(bind[ExportedRecycledPlasticPackagingView].toInstance(view))
          .build()

        running(application) {
          val request = FakeRequest(GET, exportedRecycledPlasticPackagingRoute)

          val result = route(application, request).value

          status(result) mustEqual OK
          val captor: ArgumentCaptor[Form[ExportedRecycledPlasticPackagingFormProvider]] = ArgumentCaptor.forClass(classOf[Form[ExportedRecycledPlasticPackagingFormProvider]])
          verify(view).apply(captor.capture(),any(), any())(any(), any())

          captor.getValue.value mustBe Some(false)
        }
      }

      "must redirect to home page on GET when cannot find weight" in {
        val application = applicationBuilder(Some(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, exportedRecycledPlasticPackagingRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual appRoutes.IndexController.onPageLoad.url
        }
      }

      "raise and error" - {
        "when not authorised" in {
          val application = applicationBuilderFailedAuth(userAnswers = None).build()

          running(application) {
            val request = FakeRequest(GET, exportedRecycledPlasticPackagingRoute)

            val result = route(application, request).value

            intercept[InsufficientEnrolments](status(result))
          }
        }

        "when invalid data is submitted" in {
          val application = applicationBuilder(userAnswers = createUserAnswer(true)).build()

          running(application) {
            val request =
              FakeRequest(POST, exportedRecycledPlasticPackagingRoute)
                .withFormUrlEncodedBody(("value", "invalid value"))

            val boundForm = form.bind(Map("value" -> "invalid value"))

            val view = application.injector.instanceOf[ExportedRecycledPlasticPackagingView]

            val result = route(application, request).value

            status(result) mustEqual BAD_REQUEST
            contentAsString(result) mustEqual view(boundForm, NormalMode, exportedAmount)(request, messages(application)).toString
          }
        }
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[CacheConnector].toInstance(cacheConnector)
          )
          .build()

      when(cacheConnector.set(any(),any())(any())).thenReturn(Future.successful(HttpResponse(exportedAmount.toInt, "test")))

      running(application) {
        val request =
          FakeRequest(POST, exportedRecycledPlasticPackagingRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to home page when no amount is found" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, exportedRecycledPlasticPackagingRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual appRoutes.IndexController.onPageLoad.url
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, exportedRecycledPlasticPackagingRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual appRoutes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, exportedRecycledPlasticPackagingRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual appRoutes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

  private def createUserAnswer(isAnswerYes: Boolean): Option[UserAnswers] = {
    Some(UserAnswers("123")
      .set(ExportedRecycledPlasticPackagingPage, isAnswerYes).get
      .set(ExportedPlasticPackagingWeightPage, exportedAmount).get)
  }
}
