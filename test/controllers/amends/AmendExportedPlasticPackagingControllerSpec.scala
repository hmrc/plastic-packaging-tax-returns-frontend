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

package controllers.amends

import org.apache.pekko.stream.testkit.NoMaterializer
import base.FakeIdentifierActionWithEnrolment
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar.{mock, reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import play.api.i18n.MessagesApi
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.amends.AmendExportedPlasticPackagingView

class AmendExportedPlasticPackagingControllerSpec extends PlaySpec with BeforeAndAfterEach{

  private val messagesApi = mock[MessagesApi]
  private val view = mock[AmendExportedPlasticPackagingView]
  private val sut = new AmendExportedPlasticPackagingController(
    messagesApi,
    new FakeIdentifierActionWithEnrolment(stubPlayBodyParsers(NoMaterializer)),
    stubMessagesControllerComponents(),
    view
  )

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(messagesApi, view)
    when(view.apply()(any,any)).thenReturn(HtmlFormat.empty)
  }

  "onPageLoad" should {
    "return OK and the correct view" in {
      val result = sut.onPageLoad(FakeRequest(GET, ""))

      status(result) mustEqual OK
      verify(view).apply()(any, any)
    }
  }

  "onnSubmit" should {
    "redirect to amend-exported-weight page" in {

      val result = sut.onSubmit(FakeRequest(POST, ""))

      status(result) mustEqual SEE_OTHER
      redirectLocation(result) mustEqual Some(controllers.amends.routes.AmendDirectExportPlasticPackagingController.onPageLoad.url)
    }
  }
}
