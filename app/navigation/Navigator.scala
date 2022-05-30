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

import javax.inject.{Inject, Singleton}
import play.api.mvc.Call
import controllers.routes
import pages._
import models._

@Singleton
class Navigator @Inject() () {

  private val normalRoutes: PartialFunction[Page, UserAnswers => Call] = {

    // return routes
    case StartYourReturnPage => startYourReturnRoute
    case ManufacturedPlasticPackagingPage => manufacturedPlasticPackagingRoute(_, mode = NormalMode)
    case ImportedPlasticPackagingPage => importedPlasticPackagingRoute(_, mode = NormalMode)
    case ManufacturedPlasticPackagingWeightPage =>
      _ => routes.ImportedPlasticPackagingController.onPageLoad(NormalMode)
    case ImportedPlasticPackagingWeightPage =>
      _ => routes.HumanMedicinesPlasticPackagingController.onPageLoad(NormalMode)
    case HumanMedicinesPlasticPackagingPage => humanMedicinesPlasticPackagingRoute(_, mode = NormalMode)
    case HumanMedicinesPlasticPackagingWeightPage =>
      _ => routes.ExportedPlasticPackagingWeightController.onPageLoad(NormalMode)
    case ExportedPlasticPackagingWeightPage =>
      _ => routes.RecycledPlasticPackagingWeightController.onPageLoad(NormalMode)
    case RecycledPlasticPackagingWeightPage => _ => routes.ConvertedPackagingCreditController.onPageLoad(NormalMode)
    case ConvertedPackagingCreditPage => _ => routes.ReturnsCheckYourAnswersController.onPageLoad


        //amend return routes
    case AmendAreYouSurePage => amendAreYouSureRoute
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

  private val checkRouteMap: PartialFunction[Page, UserAnswers => Call] = {
        //amend return routes
    case AmendAreYouSurePage => _ => routes.CheckYourAnswersController.onPageLoad
    case AmendManufacturedPlasticPackagingPage => _ => routes.CheckYourAnswersController.onPageLoad
    case AmendImportedPlasticPackagingPage => _ => routes.CheckYourAnswersController.onPageLoad
    case AmendHumanMedicinePlasticPackagingPage => _ => routes.CheckYourAnswersController.onPageLoad
    case AmendDirectExportPlasticPackagingPage => _ => routes.CheckYourAnswersController.onPageLoad
    case AmendRecycledPlasticPackagingPage => _ => routes.CheckYourAnswersController.onPageLoad

    // return routes
    case ManufacturedPlasticPackagingPage => manufacturedPlasticPackagingRoute(_, mode = CheckMode)
    case ImportedPlasticPackagingPage => importedPlasticPackagingRoute(_, mode = CheckMode)
    case HumanMedicinesPlasticPackagingPage => humanMedicinesPlasticPackagingRoute(_, mode = CheckMode)
    case _ => _ => routes.ReturnsCheckYourAnswersController.onPageLoad
  }

  private def amendAreYouSureRoute(answers: UserAnswers): Call =
    (answers.get(AmendAreYouSurePage), answers.get(AmendSelectedPeriodKey)) match {
      case (Some(true), _)  => routes.AmendManufacturedPlasticPackagingController.onPageLoad(NormalMode)
      case (Some(false), Some(key)) => routes.ViewReturnSummaryController.onPageLoad(key)
      case _        => throw new Exception("Unable to navigate to page")
    }

  private def startYourReturnRoute(answers: UserAnswers): Call =
    answers.get(StartYourReturnPage) match {
      case Some(true)  => routes.ManufacturedPlasticPackagingController.onPageLoad(NormalMode)
      case Some(false) => routes.NotStartOtherReturnsController.onPageLoad
      case _           => throw new Exception("Unable to navigate to page")
    }

  private def manufacturedPlasticPackagingRoute(answers: UserAnswers, mode: Mode = NormalMode): Call =
    answers.get(ManufacturedPlasticPackagingPage) match {
      case Some(true)  => routes.ManufacturedPlasticPackagingWeightController.onPageLoad(mode)
      case Some(false) =>
        if(mode == CheckMode) {
          routes.ReturnsCheckYourAnswersController.onPageLoad
        } else {
          routes.ImportedPlasticPackagingController.onPageLoad(mode)
        }
      case _           => throw new Exception("Unable to navigate to page")
    }

  private def importedPlasticPackagingRoute(answers: UserAnswers, mode: Mode = NormalMode): Call =
    answers.get(ImportedPlasticPackagingPage) match {
      case Some(true)  => routes.ImportedPlasticPackagingWeightController.onPageLoad(mode)
      case Some(false) =>
        if(mode == CheckMode) {
          routes.ReturnsCheckYourAnswersController.onPageLoad
        } else {
          routes.HumanMedicinesPlasticPackagingWeightController.onPageLoad(mode)
        }
      case _           => throw new Exception("Unable to navigate to page")
    }

  private def humanMedicinesPlasticPackagingRoute(answers: UserAnswers, mode: Mode): Call =
    answers.get(HumanMedicinesPlasticPackagingPage) match {
      case Some(true) => routes.HumanMedicinesPlasticPackagingWeightController.onPageLoad(mode)
      case Some(false) =>
        if (mode == CheckMode) {routes.ReturnsCheckYourAnswersController.onPageLoad()}
        else {routes.ExportedPlasticPackagingWeightController.onPageLoad(mode)}
      case _           => throw new Exception("Unable to navigate to page")
    }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call =
    mode match {
      case NormalMode =>
        normalRoutes(page)(userAnswers)
      case CheckMode =>
        checkRouteMap(page)(userAnswers)
    }

}
