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

import cacheables.AmendSelectedPeriodKey
import models.UserAnswers
import pages._
import pages.amends._
import play.api.mvc.Call

import javax.inject.Singleton

@Singleton
class AmendsJourneyNavigator {

  val normalRoutes: PartialFunction[Page, UserAnswers => Call] = {
    case AmendAreYouSurePage => amendAreYouSureRoute
    case AmendManufacturedPlasticPackagingPage =>
      _ => controllers.amends.routes.AmendImportedPlasticPackagingController.onPageLoad()
    case AmendImportedPlasticPackagingPage =>
      _ => controllers.amends.routes.AmendHumanMedicinePlasticPackagingController.onPageLoad()
    case AmendHumanMedicinePlasticPackagingPage =>
      _ => controllers.amends.routes.AmendDirectExportPlasticPackagingController.onPageLoad()
    case AmendDirectExportPlasticPackagingPage =>
      _ => controllers.amends.routes.AmendRecycledPlasticPackagingController.onPageLoad()
    case AmendRecycledPlasticPackagingPage =>
      _ => controllers.amends.routes.CheckYourAnswersController.onPageLoad()
  }

  val checkRoutes: PartialFunction[Page, UserAnswers => Call] = {
    case AmendAreYouSurePage => _ => controllers.amends.routes.CheckYourAnswersController.onPageLoad()
    case AmendManufacturedPlasticPackagingPage => _ => controllers.amends.routes.CheckYourAnswersController.onPageLoad()
    case AmendImportedPlasticPackagingPage => _ => controllers.amends.routes.CheckYourAnswersController.onPageLoad()
    case AmendHumanMedicinePlasticPackagingPage => _ => controllers.amends.routes.CheckYourAnswersController.onPageLoad()
    case AmendDirectExportPlasticPackagingPage => _ => controllers.amends.routes.CheckYourAnswersController.onPageLoad()
    case AmendRecycledPlasticPackagingPage => _ => controllers.amends.routes.CheckYourAnswersController.onPageLoad()
  }

  private def amendAreYouSureRoute(answers: UserAnswers): Call =
    (answers.get(AmendAreYouSurePage), answers.get(AmendSelectedPeriodKey)) match {
      case (Some(true), _)  => controllers.amends.routes.AmendManufacturedPlasticPackagingController.onPageLoad()
      case (Some(false), Some(key)) => controllers.amends.routes.ViewReturnSummaryController.onPageLoad(key)
      case _        => throw new Exception("Unable to navigate to page")
    }


  val x:Call= controllers.amends.routes.AmendDirectExportPlasticPackagingController.onPageLoad()
}
