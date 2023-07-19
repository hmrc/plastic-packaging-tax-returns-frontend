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

package controllers.auth

import base.SpecBase
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar.{mock, verify, when}
import org.scalatestplus.play.PlaySpec
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.auth.SignedOutView

class SignedOutControllerSpec extends PlaySpec {

  private val view = mock[SignedOutView]
  private val sut = new SignedOutController(stubMessagesControllerComponents(), view )

  "SignedOut Controller" should {

    "must return OK and the correct view for a GET" in {
      when(view.apply()(any,any)).thenReturn(HtmlFormat.empty)
      val result = sut.onPageLoad()(FakeRequest())
      status(result) mustEqual OK
      verify(view).apply()(any,any)
    }
  }
}
