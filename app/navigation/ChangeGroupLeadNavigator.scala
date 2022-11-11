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
import models.Mode.CheckMode
import play.api.mvc.Call

class ChangeGroupLeadNavigator {

  private def returnToCheckYourAnswersOr(call: Call)(mode: Mode): Call =
    if (mode == CheckMode) controllers.changeGroupLead.routes.NewGroupLeadCheckYourAnswerController.onPageLoad
    else call

  val mainContactName: Mode => Call =
    returnToCheckYourAnswersOr(
      controllers.changeGroupLead.routes.NewGroupLeadCheckYourAnswerController.onPageLoad //todo this will be job title page
    )


}
