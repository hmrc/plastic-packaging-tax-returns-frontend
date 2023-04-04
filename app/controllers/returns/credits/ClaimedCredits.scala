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

package controllers.returns.credits

import models.UserAnswers
import models.returns.CreditsAnswer
import pages.returns.credits.{ConvertedCreditsPage, ExportedCreditsPage}

case class ClaimedCredits(isExported: Boolean, converted: CreditsAnswer) {

  def hasMadeClaim: Boolean =
    isExported || converted.yesNo

}

object ClaimedCredits {

  def apply(userAnswers: UserAnswers): ClaimedCredits = {
    ClaimedCredits(userAnswers.getOrFail(ExportedCreditsPage), userAnswers.getOrFail(ConvertedCreditsPage))

  }
}