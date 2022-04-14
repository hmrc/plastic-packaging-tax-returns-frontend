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
import models.returns.TaxReturnObligation
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.SubmittedReturnsView

import java.time.LocalDate

class SubmittedReturnsControllerSpec extends SpecBase {

  "SubmittedReturns Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.SubmittedReturnsController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SubmittedReturnsView]

        val ob: Seq[TaxReturnObligation] = {
          Seq.empty
        }

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(ob)(request, messages(application)).toString
      }
    }
  }
//TODO: id these using indexing and test rendered view for no. id's
  "returnsLine" - {
    "should handle empty sequence of obligations" in {
      val obligations0: Seq[TaxReturnObligation] = {
        Seq.empty
      }


    }
    "should handle 1 obligation" in {
      val obligations1: Seq[TaxReturnObligation] = {
        Seq(TaxReturnObligation(LocalDate.now(),
          LocalDate.now(),
          LocalDate.now(),
          "PK1"))
      }


    }
    "should handle multiple obligations" in {
      val obligations2: Seq[TaxReturnObligation] = {
        Seq(TaxReturnObligation(LocalDate.now(),
          LocalDate.now(),
          LocalDate.now().plusWeeks(4),
          "PK1"),
          TaxReturnObligation(LocalDate.now(),
            LocalDate.now().plusWeeks(4),
            LocalDate.now().plusWeeks(8),
            "PK2")
        )
      }


    }
  }

}
