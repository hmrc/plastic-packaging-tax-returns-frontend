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

package controllers.changeGroupLead

import base.SpecBase
import forms.changeGroupLead.NewGroupLeadEnterContactAddressFormProvider
import models.Mode.NormalMode
import models.changeGroupLead.NewGroupLeadAddressDetails
import navigation.{FakeNavigator, Navigator}
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.changeGroupLead.NewGroupLeadEnterContactAddressView

class NewGroupLeadEnterContactAddressControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new NewGroupLeadEnterContactAddressFormProvider()
  val form = formProvider()

  lazy val newGroupLeadEnterContactAddressRoute = routes.NewGroupLeadEnterContactAddressController.onPageLoad(NormalMode).url

//  val userAnswers = UserAnswers(
//    userAnswersId,
//    Json.obj(
//      NewGroupLeadEnterContactAddressPage.toString -> Json.obj(
//        "AddressLine1" -> "value 1",
//        "AddressLine2" -> "value 2"
//      )
//    )
//  )

  "NewGroupLeadEnterContactAddress Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, newGroupLeadEnterContactAddressRoute)

        val view = application.injector.instanceOf[NewGroupLeadEnterContactAddressView]

        val result = route(application, request).value

        status(result) mustEqual OK
     //   contentAsString(result) mustEqual view(form, "name", NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, newGroupLeadEnterContactAddressRoute)

        val view = application.injector.instanceOf[NewGroupLeadEnterContactAddressView]

        val result = route(application, request).value

        status(result) mustEqual OK
       // contentAsString(result) mustEqual view(form.fill(NewGroupLeadAddressDetails("value 1", "value 2", None, "any town", None, "IT")), "name", NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

   //   when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, newGroupLeadEnterContactAddressRoute)
            .withFormUrlEncodedBody(("AddressLine1", "value 1"), ("AddressLine2", "value 2"))

        val result = route(application, request).value

        //status(result) mustEqual SEE_OTHER
     //   redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, newGroupLeadEnterContactAddressRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[NewGroupLeadEnterContactAddressView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, "name", NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" ignore {
      val application = applicationBuilder(userAnswers = None).build()
      running(application) {
        val request = FakeRequest(GET, newGroupLeadEnterContactAddressRoute)
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
//        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" ignore {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, newGroupLeadEnterContactAddressRoute)
            .withFormUrlEncodedBody(("AddressLine1", "value 1"), ("AddressLine2", "value 2"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
//        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
