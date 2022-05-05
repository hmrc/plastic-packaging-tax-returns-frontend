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

package controllers.payments

import base.SpecBase
import play.api.Application
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, defaultAwaitTimeout, redirectLocation, route, writeableOf_AnyContentAsEmpty}


class DirectDebitControllerSpec extends SpecBase {


  "DirectDebitController" - {
    "redirectLink" - {
      "redirect to enter email address page" in {

        val app: Application = applicationBuilder().build()

        val request = FakeRequest(GET, routes.DirectDebitController.redirectLink().url)

        val result = route(app, request).value

        redirectLocation(result) mustBe Some("/bleach")
      }
    }
  }
}
