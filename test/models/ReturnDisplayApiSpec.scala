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

package models

import models.returns.{IdDetails, ReturnDisplayApi, ReturnDisplayChargeDetails, ReturnDisplayDetails}
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.mock
import org.scalatestplus.play.PlaySpec
import play.api.i18n.Messages

class ReturnDisplayApiSpec extends PlaySpec {

  val messages: Messages = mock[Messages]

  "calculatePeriodString" must {

    "complain if the charge details are missing" in {
      val chargeDetails = None
      //noinspection ScalaStyle
      val returnDisplayApi = ReturnDisplayApi("", IdDetails("", ""), chargeDetails, ReturnDisplayDetails(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))
      intercept[Exception] {
        returnDisplayApi.calculatePeriodString()(messages)
      }
    }

    "render the first period correctly" in {
      val chargeDetails = ReturnDisplayChargeDetails("00XX", None, "2022-04-01", "2022-06-30", "", "")
      val returnDisplayApi = ReturnDisplayApi("", IdDetails("", ""), Some(chargeDetails), ReturnDisplayDetails(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))

      val periodString = returnDisplayApi.calculatePeriodString()(messages)
    }

  }
}
