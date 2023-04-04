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

import com.google.inject.Inject
import config.{Features, FrontendAppConfig}
import controllers.helpers.NonExportedAmountHelper
import controllers.returns.credits.ClaimedCredits
import controllers.returns.routes
import models.Mode.{CheckMode, NormalMode}
import models.returns.CreditsAnswer
import models.{Mode, UserAnswers}
import pages._
import pages.returns._
import play.api.mvc.Call

import javax.inject.Singleton

@Singleton
class ReturnsJourneyNavigator @Inject()(
  appConfig: FrontendAppConfig,
  nonExportedAmountHelper: NonExportedAmountHelper
) {

  @deprecated("Call direct route method on this class instead", since = "19th July 2022")
  val normalRoutes: PartialFunction[Page, UserAnswers => Call] = {
    // TODO - replace with direct calls
    case ManufacturedPlasticPackagingWeightPage =>
      _ => routes.ImportedPlasticPackagingController.onPageLoad(NormalMode)
    case ImportedPlasticPackagingWeightPage            =>
      _ => routes.ConfirmPlasticPackagingTotalController.onPageLoad
    case NonExportedHumanMedicinesPlasticPackagingPage => nonExportedHumanMedicinesPlasticPackagingRoute(_, mode = NormalMode)
    case NonExportedHumanMedicinesPlasticPackagingWeightPage => _ => routes.NonExportedRecycledPlasticPackagingController.onPageLoad(NormalMode)
    case NonExportedRecycledPlasticPackagingPage => nonExportedRecycledPlasticPackagingPageRoute(_, mode = NormalMode)
    case NonExportedRecycledPlasticPackagingWeightPage => _ => routes.ReturnsCheckYourAnswersController.onPageLoad()
    case x => throw new IllegalStateException(s"Navigation for '$x' not found (normal mode)")
  }

  @deprecated("Call direct route method on this class instead", since = "19th July 2022")
  val checkRoutes: PartialFunction[Page, UserAnswers => Call] = {
    // TODO - replace with direct calls
    case ManufacturedPlasticPackagingWeightPage => answers => manufacturedPlasticPackagingWeightRoute(answers)
    case ImportedPlasticPackagingWeightPage            => _ => routes.ConfirmPlasticPackagingTotalController.onPageLoad
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
      controllers.returns.credits.routes.ClaimForWhichYearController.onSubmit
    else
      routes.ManufacturedPlasticPackagingController.onPageLoad(NormalMode)
  }

  def claimForWhichYear: Call =
    controllers.returns.credits.routes.ExportedCreditsController.onPageLoad(NormalMode)

  def exportedCreditsYesNo(mode: Mode, isYes: Boolean): Call = {
    if (isYes)
      controllers.returns.credits.routes.ExportedCreditsWeightController.onPageLoad(mode)
    else
      controllers.returns.credits.routes.ConvertedCreditsController.onPageLoad(mode)
  }

  def exportedCreditsWeight(mode: Mode): Call = {
      controllers.returns.credits.routes.ConvertedCreditsController.onPageLoad(mode)
  }
  
  def convertedCreditsYesNo(mode: Mode, isAnswerYes: Boolean): Call = {
    if (isAnswerYes)
      controllers.returns.credits.routes.ConvertedCreditsWeightController.onPageLoad(mode)
    else if (mode == NormalMode)
      controllers.returns.credits.routes.ConfirmPackagingCreditController.onPageLoad(mode)
    else
      routes.ReturnsCheckYourAnswersController.onPageLoad()
  }

  def convertedCreditsWeight(mode: Mode, claimedCredits: ClaimedCredits): Call = {
    if (claimedCredits.hasMadeClaim)
      controllers.returns.credits.routes.ConfirmPackagingCreditController.onPageLoad(mode)
    else if (mode == NormalMode)
      controllers.returns.routes.NowStartYourReturnController.onPageLoad
    else
      controllers.returns.routes.ReturnsCheckYourAnswersController.onPageLoad()
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

  def manufacturedPlasticPackagingWeightRoute(answers: UserAnswers): Call =
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

  def confirmTotalPlasticPackagingRoute(answers: UserAnswers): Call = {
    nonExportedAmountHelper.totalPlasticAdditions(answers) match {
      case Some(amount) if amount > 0 => controllers.returns.routes.DirectlyExportedComponentsController.onPageLoad(NormalMode)
      case Some(amount) if amount <= 0 => controllers.returns.routes.ReturnsCheckYourAnswersController.onPageLoad()
      case _ => controllers.routes.IndexController.onPageLoad
    }
  }

  def directlyExportedComponentsRoute(userAnsweredYes: Boolean, mode: Mode): Call =
    if (userAnsweredYes) {
      routes.ExportedPlasticPackagingWeightController.onPageLoad(mode)
    } else {
      routes.PlasticExportedByAnotherBusinessController.onPageLoad(mode)
    }

  def exportedPlasticPackagingWeightRoute(isAllPlasticExported: Boolean, mode: Mode): Call =
    if (mode.equals(CheckMode)) 
      routes.PlasticExportedByAnotherBusinessController.onPageLoad(mode)
    else if (isAllPlasticExported) 
      routes.ReturnsCheckYourAnswersController.onPageLoad()
    else 
      routes.PlasticExportedByAnotherBusinessController.onPageLoad(mode)

  def exportedByAnotherBusinessRoute(answers: UserAnswers, mode: Mode): Call =
    (answers.get(AnotherBusinessExportedPage), mode) match {
      case (Some(true), _) => routes.AnotherBusinessExportWeightController.onPageLoad(mode)
      case (Some(false), NormalMode) => routes.NonExportedHumanMedicinesPlasticPackagingController.onPageLoad(mode)
      case (Some(false), CheckMode) => routes.ReturnsCheckYourAnswersController.onPageLoad()
      case _ => throw new Exception("Unable to navigate to page")
    }

  def exportedByAnotherBusinessWeightRoute(isAllPlasticExported: Boolean, mode: Mode): Call = {
    if (mode.equals(CheckMode)) 
      routes.ReturnsCheckYourAnswersController.onPageLoad()
    else if (isAllPlasticExported)
      routes.ReturnsCheckYourAnswersController.onPageLoad() 
    else 
      routes.NonExportedHumanMedicinesPlasticPackagingController.onPageLoad(mode)
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