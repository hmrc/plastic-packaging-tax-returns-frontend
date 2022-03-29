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

import javax.inject.{Inject, Singleton}

import play.api.mvc.Call
import controllers.routes
import pages._
import models._

@Singleton
class Navigator @Inject() () {

  private val normalRoutes: Page => UserAnswers => Call = {
    case AmendAreYouSurePage =>
      _ => routes.AmendManufacturedPlasticPackagingController.onPageLoad(NormalMode)
    case AmendManufacturedPlasticPackagingPage =>
      _ => routes.AmendImportedPlasticPackagingController.onPageLoad(NormalMode)
    case AmendImportedPlasticPackagingPage =>
      _ => routes.AmendHumanMedicinePlasticPackagingController.onPageLoad(NormalMode)
    case AmendHumanMedicinePlasticPackagingPage =>
      _ => routes.AmendDirectExportPlasticPackagingController.onPageLoad(NormalMode)
    case AmendDirectExportPlasticPackagingPage =>
      _ => routes.AmendRecycledPlasticPackagingController.onPageLoad(NormalMode)
    case AmendRecycledPlasticPackagingPage =>
      _ => routes.CheckYourAnswersController.onPageLoad
  }

  private val checkRouteMap: Page => UserAnswers => Call = {
    case _ => _ => routes.CheckYourAnswersController.onPageLoad
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call =
    mode match {
      case NormalMode =>
        normalRoutes(page)(userAnswers)
      case CheckMode =>
        checkRouteMap(page)(userAnswers)
    }

}
