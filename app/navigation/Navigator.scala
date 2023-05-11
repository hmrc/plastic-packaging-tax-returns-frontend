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

package navigation

import models.Mode.{CheckMode, NormalMode}
import models._
import pages._
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@deprecated("Use navigator for the corresponding journey", since = "1.0")
@Singleton
class Navigator @Inject()(
                           returns: ReturnsJourneyNavigator
                         ) {

  private val normalRoutes: PartialFunction[Page, UserAnswers => Call] =
    returns.normalRoutes

  private val checkRouteMap: PartialFunction[Page, UserAnswers => Call] =
    returns.checkRoutes

  @deprecated("Just call the method direct, dont come through nextPage", since = "1.0")
  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call =
    mode match {
      case NormalMode =>
        normalRoutes(page)(userAnswers)
      case CheckMode =>
        checkRouteMap(page)(userAnswers)
    }

}
