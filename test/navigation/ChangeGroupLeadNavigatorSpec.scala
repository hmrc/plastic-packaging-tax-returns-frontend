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

package navigation

import models.Mode
import models.Mode.{CheckMode, NormalMode}
import org.scalatestplus.play.PlaySpec
import play.api.mvc.Call

class ChangeGroupLeadNavigatorSpec extends PlaySpec {

  val sut = new ChangeGroupLeadNavigator()

  "mainContactName" must {
    behave like aChangeGroupLeadQuestionPage(sut.mainContactName)(
      controllers.changeGroupLead.routes.NewGroupLeadCheckYourAnswerController.onPageLoad
    )
  }

  def aChangeGroupLeadQuestionPage(method: Mode => Call)(nextPage: Call): Unit = {
    "navigate to CYA page" when {
      "in CheckMode" in {
        method(CheckMode) mustBe controllers.changeGroupLead.routes.NewGroupLeadCheckYourAnswerController.onPageLoad
      }
    }
    "navigate to the next page" when {
      "in NormalMode" in {
        method(NormalMode) mustBe nextPage
      }
    }
  }
}
