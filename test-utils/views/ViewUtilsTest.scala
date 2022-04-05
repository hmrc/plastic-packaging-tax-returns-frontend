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

package views

import models.returns.Obligation
import org.mockito.{Mockito, MockitoSugar}
import org.scalatestplus.play.PlaySpec
import play.api.i18n.Messages
import views.ViewUtils._


class ViewUtilsTest extends PlaySpec with MockitoSugar {
//TODO
  "getMonthName" ignore {
    "give january when given number 1" in {
      val mockMessages = mock[Messages]
      val x = getMonthName(1)(mockMessages)
      x mustBe "January"
    }
  }
  "displayReturnQuarter" ignore {
    "give breakfast" when {
      "given any obligation" in {???
      }
    }
  }
}
