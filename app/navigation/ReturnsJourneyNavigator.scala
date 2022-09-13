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

import com.google.inject.Inject
import config.{Features, FrontendAppConfig}
import controllers.returns.credits.ClaimedCredits
import controllers.returns.routes
import models.Mode.{CheckMode, NormalMode}
import models.requests.DataRequest
import models.{Mode, UserAnswers}
import pages._
import pages.returns._
import pages.returns.credits.{ConvertedCreditsPage, ExportedCreditsPage}
import play.api.mvc.Call

import javax.inject.Singleton

@Singleton
class ReturnsJourneyNavigator @Inject()(
  appConfig: FrontendAppConfig,
) {

  private def exportedAllPlastic(answers: UserAnswers): Boolean = {
    val manufactured = answers.get(ManufacturedPlasticPackagingWeightPage).getOrElse(0L)
    val imported = answers.get(ImportedPlasticPackagingWeightPage).getOrElse(0L)
    val exported = answers.get(ExportedPlasticPackagingWeightPage).getOrElse(0L)

    exported >= (manufactured + imported)
  }

  @deprecated("Call direct route method on this class instead", since = "19th July 2022")
  val normalRoutes: PartialFunction[Page, UserAnswers => Call] = {
    // TODO - replace with direct calls
    case ManufacturedPlasticPackagingWeightPage =>
      _ => routes.ImportedPlasticPackagingController.onPageLoad(NormalMode)
    case ImportedPlasticPackagingWeightPage =>
      _ => routes.ConfirmPlasticPackagingTotalController.onPageLoad
    case DirectlyExportedComponentsPage => directlyExportedComponentsRoute(_, mode = NormalMode)
    case ExportedPlasticPackagingWeightPage => exportedPlasticPackagingWeightRoute(_, mode = NormalMode)
    case NonExportedHumanMedicinesPlasticPackagingPage => nonExportedHumanMedicinesPlasticPackagingRoute(_, mode = NormalMode)
    case NonExportedHumanMedicinesPlasticPackagingWeightPage => _ => routes.NonExportedRecycledPlasticPackagingController.onPageLoad(NormalMode)
    case NonExportedRecycledPlasticPackagingPage => nonExportedRecycledPlasticPackagingPageRoute(_, mode = NormalMode)
    case NonExportedRecycledPlasticPackagingWeightPage => _ => routes.ReturnsCheckYourAnswersController.onPageLoad()
  }

  @deprecated("Call direct route method on this class instead", since = "19th July 2022")
  val checkRoutes: PartialFunction[Page, UserAnswers => Call] = {
    // TODO - replace with direct calls
    case ManufacturedPlasticPackagingWeightPage => answers => manufacturedPlasticPackagingWeightRoute(answers)
    case ImportedPlasticPackagingWeightPage => _ => routes.ConfirmPlasticPackagingTotalController.onPageLoad
    case DirectlyExportedComponentsPage => answers => directlyExportedComponentsRoute(answers, mode = CheckMode)
    case ExportedPlasticPackagingWeightPage => answers => exportedPlasticPackagingWeightRoute(answers, mode = CheckMode)
    case NonExportedHumanMedicinesPlasticPackagingPage => answers => nonExportedHumanMedicinesPlasticPackagingRoute(answers, mode = CheckMode)
    case NonExportedHumanMedicinesPlasticPackagingWeightPage => _ => routes.NonExportedRecycledPlasticPackagingController.onPageLoad(CheckMode)
    case NonExportedRecycledPlasticPackagingPage => answers => nonExportedRecycledPlasticPackagingPageRoute(answers, mode = CheckMode)
    case _ => _ => routes.ReturnsCheckYourAnswersController.onPageLoad()
  }

  def startYourReturnRoute(doesUserWantToStartReturn: Boolean, isFirstReturn: Boolean): Call =
    if (doesUserWantToStartReturn) {
      if (appConfig.isFeatureEnabled(Features.creditsForReturnsEnabled) && !isFirstReturn)
        controllers.returns.credits.routes.WhatDoYouWantToDoController.onPageLoad(NormalMode)
      else
        routes.ManufacturedPlasticPackagingController.onPageLoad(NormalMode)
    } else
      routes.NotStartOtherReturnsController.onPageLoad()


  def whatDoYouWantDoRoute(mode: Mode, newAnswer: Boolean): Call = {
    if (mode.equals(CheckMode))
      routes.ReturnsCheckYourAnswersController.onPageLoad()
    else if (newAnswer)
      controllers.returns.credits.routes.ExportedCreditsController.onPageLoad(NormalMode)
    else
      routes.ManufacturedPlasticPackagingController.onPageLoad(NormalMode)
  }

  def exportedCreditsRoute(mode: Mode): Call = {
    if (mode.equals(CheckMode)) {
      routes.ReturnsCheckYourAnswersController.onPageLoad()
    } else {
      controllers.returns.credits.routes.ConvertedCreditsController.onPageLoad(NormalMode)
    }
  }

  def convertedCreditsRoute(mode: Mode, claimedCredits: ClaimedCredits): Call = {
    if (mode.equals(CheckMode)) {
      routes.ReturnsCheckYourAnswersController.onPageLoad()
    } else if (claimedCredits.hasMadeClaim) {
      controllers.returns.credits.routes.ConfirmPackagingCreditController.onPageLoad
    } else {
      controllers.returns.routes.NowStartYourReturnController.onPageLoad
    }
  }

  def confirmCreditRoute(mode: Mode): Call =
    if (mode.equals(CheckMode))
      routes.ReturnsCheckYourAnswersController.onPageLoad()
    else
      controllers.returns.routes.NowStartYourReturnController.onPageLoad

  def nowStartYourReturnRoute: Call =
    controllers.returns.routes.ManufacturedPlasticPackagingController.onPageLoad(NormalMode)

  def manufacturedPlasticPackagingRoute(mode: Mode, hasAnswerChanged: Boolean, usersAnswer: Boolean): Call = {
    if (mode.equals(NormalMode))
      manufacturedRouteForNormalMode(usersAnswer)
    else
      manufacturedRouteForCheckMode(hasAnswerChanged, usersAnswer)
  }

  private def manufacturedRouteForCheckMode(hasAnswerChanged: Boolean, usersAnswer: Boolean) = {
    if (usersAnswer && hasAnswerChanged)
      routes.ManufacturedPlasticPackagingWeightController.onPageLoad(CheckMode)
    else
      routes.ConfirmPlasticPackagingTotalController.onPageLoad
  }

  private def manufacturedRouteForNormalMode(usersAnswer: Boolean) = {
    if (usersAnswer)
      routes.ManufacturedPlasticPackagingWeightController.onPageLoad(NormalMode)
    else
      routes.ImportedPlasticPackagingController.onPageLoad(NormalMode)
  }

  private def manufacturedPlasticPackagingWeightRoute(answers: UserAnswers): Call =
    answers.get(ManufacturedPlasticPackagingWeightPage) match {
      case Some(_) => routes.ConfirmPlasticPackagingTotalController.onPageLoad
      case _ => throw new Exception("Unable to navigate to page")
    }

  def importedPlasticPackagingRoute(mode: Mode, hasAnswerChanged: Boolean, usersAnswer: Boolean): Call =
    if (mode.equals(NormalMode))
      importedPlasticPackagingRouteNormalMode(mode, usersAnswer)
    else
      importedPlasticPackagingRouteCheckMode(mode, hasAnswerChanged, usersAnswer)

  private def importedPlasticPackagingRouteCheckMode(mode: Mode, hasAnswerChanged: Boolean, usersAnswer: Boolean) =
    if (usersAnswer && hasAnswerChanged)
      routes.ImportedPlasticPackagingWeightController.onPageLoad(mode)
    else
      routes.ConfirmPlasticPackagingTotalController.onPageLoad

  private def importedPlasticPackagingRouteNormalMode(mode: Mode, usersAnswer: Boolean) =
    if (usersAnswer)
      routes.ImportedPlasticPackagingWeightController.onPageLoad(mode)
    else
      routes.ConfirmPlasticPackagingTotalController.onPageLoad

  private def directlyExportedComponentsRoute(answers: UserAnswers, mode: Mode): Call =
    answers.get(DirectlyExportedComponentsPage) match {
      case Some(true) => routes.ExportedPlasticPackagingWeightController.onPageLoad(mode)
      case Some(false) => routes.NonExportedHumanMedicinesPlasticPackagingController.onPageLoad(mode)
      case _ => throw new Exception("Unable to navigate to page")
    }

  private def exportedPlasticPackagingWeightRoute(answers: UserAnswers, mode: Mode): Call =
    exportedAllPlastic(answers) match {
      case true => routes.ReturnsCheckYourAnswersController.onPageLoad()
      case false => routes.NonExportedHumanMedicinesPlasticPackagingController.onPageLoad(mode)
    }

  private def nonExportedHumanMedicinesPlasticPackagingRoute(answers: UserAnswers, mode: Mode): Call =
    answers.get(NonExportedHumanMedicinesPlasticPackagingPage) match {
      case Some(true) => routes.NonExportedHumanMedicinesPlasticPackagingWeightController.onPageLoad(mode)
      case Some(false) => routes.NonExportedRecycledPlasticPackagingController.onPageLoad(mode)
      case _ => throw new Exception("Unable to navigate to page")
    }

  private def nonExportedRecycledPlasticPackagingPageRoute(answers: UserAnswers, mode: Mode): Call =
    answers.get(NonExportedRecycledPlasticPackagingPage) match {
      case Some(true) => routes.NonExportedRecycledPlasticPackagingWeightController.onPageLoad(mode)
      case Some(false) => routes.ReturnsCheckYourAnswersController.onPageLoad()
      case _ => throw new Exception("Unable to navigate to page")
    }
}