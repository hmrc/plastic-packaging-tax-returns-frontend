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

import controllers.changeGroupLead._
import models.Mode
import models.Mode.{CheckMode, NormalMode}
import org.scalatestplus.play.PlaySpec
import play.api.mvc.Call

class ChangeGroupLeadNavigatorSpec extends PlaySpec {

  private val sut = new ChangeGroupLeadNavigator()
  private val checkYourAnswersPage: Call = routes.NewGroupLeadCheckYourAnswerController.onPageLoad
  
  "mainContactName" must {
    behave like aChangeGroupLeadQuestionPage(mode => sut.mainContactName(mode))(
      checkYourAnswersPage
    )
  }
  
  "enterContactAddressNextPage" in {
    sut.enterContactAddressNextPage(NormalMode) mustBe routes.MainContactNameController.onPageLoad(NormalMode)
    sut.enterContactAddressNextPage(CheckMode) mustBe checkYourAnswersPage
  }
  
  "selectNewGroupRepNextPage" in {
    sut.selectNewGroupRepNextPage(NormalMode) mustBe routes.MainContactNameController.onPageLoad(NormalMode) // todo wrong
    sut.selectNewGroupRepNextPage(CheckMode) mustBe checkYourAnswersPage
  }

  def aChangeGroupLeadQuestionPage(method: Mode => Call)(nextPage: Call): Unit = {
    "navigate to CYA page" when {
      "in CheckMode" in {
        method(CheckMode) mustBe checkYourAnswersPage
      }
    }
    "navigate to the next page" when {
      "in NormalMode" in {
        method(NormalMode) mustBe nextPage
      }
    }
  }
}
