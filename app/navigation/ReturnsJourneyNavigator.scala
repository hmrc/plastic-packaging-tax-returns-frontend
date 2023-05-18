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
import controllers.returns.{routes => returnRoutes}
import controllers.returns.credits.{routes => creditRoutes}
import models.returns.CreditRangeOption
import models.Mode.{CheckMode, NormalMode}
import models.{Mode, UserAnswers}
import pages._
import pages.returns._
import pages.returns.credits.ConvertedCreditsPage
import play.api.mvc.Call

import javax.inject.Singleton

@Singleton
class ReturnsJourneyNavigator @Inject()(
  appConfig: FrontendAppConfig,
  nonExportedAmountHelper: NonExportedAmountHelper
) {

  @deprecated("Call direct route method on this class instead", since = "19th July 2022")
  val normalRoutes: PartialFunction[Page, UserAnswers => Call] = {
    case ManufacturedPlasticPackagingWeightPage =>
      _ => returnRoutes.ImportedPlasticPackagingController.onPageLoad(NormalMode)
    case ImportedPlasticPackagingWeightPage            =>
      _ => returnRoutes.ConfirmPlasticPackagingTotalController.onPageLoad
    case NonExportedHumanMedicinesPlasticPackagingPage => nonExportedHumanMedicinesPlasticPackagingRoute(_, mode = NormalMode)
    case NonExportedHumanMedicinesPlasticPackagingWeightPage => _ => returnRoutes.NonExportedRecycledPlasticPackagingController.onPageLoad(NormalMode)
    case NonExportedRecycledPlasticPackagingPage => nonExportedRecycledPlasticPackagingPageRoute(_, mode = NormalMode)
    case NonExportedRecycledPlasticPackagingWeightPage => _ => returnRoutes.ReturnsCheckYourAnswersController.onPageLoad()
    case x => throw new IllegalStateException(s"Navigation for '$x' not found (normal mode)")
  }

  @deprecated("Call direct route method on this class instead", since = "19th July 2022")
  val checkRoutes: PartialFunction[Page, UserAnswers => Call] = {
    case ManufacturedPlasticPackagingWeightPage => answers => manufacturedPlasticPackagingWeight(answers)
    case ImportedPlasticPackagingWeightPage            => _ => returnRoutes.ConfirmPlasticPackagingTotalController.onPageLoad
    case NonExportedHumanMedicinesPlasticPackagingPage => answers => nonExportedHumanMedicinesPlasticPackagingRoute(answers, mode = CheckMode)
    case NonExportedHumanMedicinesPlasticPackagingWeightPage => _ => returnRoutes.NonExportedRecycledPlasticPackagingController.onPageLoad(CheckMode)
    case NonExportedRecycledPlasticPackagingPage => answers => nonExportedRecycledPlasticPackagingPageRoute(answers, mode = CheckMode)
    case _ => _ => returnRoutes.ReturnsCheckYourAnswersController.onPageLoad()
  }

  def startYourReturn(doesUserWantToStartReturn: Boolean, isFirstReturn: Boolean): Call =
    if (doesUserWantToStartReturn) {
      if (appConfig.isFeatureEnabled(Features.creditsForReturnsEnabled) && !isFirstReturn)
        creditRoutes.WhatDoYouWantToDoController.onPageLoad(NormalMode)
      else
        firstPageOfReturnSection
    } else
      returnRoutes.NotStartOtherReturnsController.onPageLoad()

  private def firstPageOfReturnSection =
    returnRoutes.ManufacturedPlasticPackagingController.onPageLoad(NormalMode)

  def whatDoYouWantDo(mode: Mode, isClaimingCredit: Boolean): Call = {
    (mode, isClaimingCredit) match {
      case (_, true) => creditRoutes.ClaimForWhichYearController.onPageLoad(mode)
      case (NormalMode, false) => firstPageOfReturnSection
      case (CheckMode, false) => returnRoutes.ReturnsCheckYourAnswersController.onPageLoad()
    }
  }

  def claimForWhichYear(year: CreditRangeOption, mode: Mode): Call =
    creditRoutes.ExportedCreditsController.onPageLoad(year.key, mode)

  def exportedCreditsYesNo(key: String, mode: Mode, isYes: Boolean, userAnswers: UserAnswers): Call = {
    val isCheckMode = mode == CheckMode && userAnswers.get(ConvertedCreditsPage(key)).isDefined
    (isCheckMode, isYes) match {
      case (_, true) => creditRoutes.ExportedCreditsWeightController.onPageLoad(key, mode)
      case (false, false) => creditRoutes.ConvertedCreditsController.onPageLoad(key, mode)
      case (true, false) =>  creditRoutes.ConfirmPackagingCreditController.onPageLoad(key, mode)
    }
  }

  def exportedCreditsWeight(key: String, mode: Mode, userAnswers: UserAnswers): Call = {
    val isCheckMode = mode == CheckMode && userAnswers.get(ConvertedCreditsPage(key)).isDefined
    if(isCheckMode)
      creditRoutes.ConfirmPackagingCreditController.onPageLoad(key, mode)
    else
      creditRoutes.ConvertedCreditsController.onPageLoad(key, mode)
  }
  
  def convertedCreditsYesNo(mode: Mode, key: String, isAnswerYes: Boolean): Call = {
    if (isAnswerYes)
      creditRoutes.ConvertedCreditsWeightController.onPageLoad(key, mode)
    else
      creditRoutes.ConfirmPackagingCreditController.onPageLoad(key, mode)
  }

  def convertedCreditsWeight(key: String, mode: Mode) =
    creditRoutes.ConfirmPackagingCreditController.onPageLoad(key, mode)

  def confirmCredit(mode: Mode): Call =
      creditRoutes.CreditsClaimedListController.onPageLoad(mode)

  def creditClaimedList(mode: Mode, isAddingAnotherYear: Boolean, userAnswers: UserAnswers) =
    if (isAddingAnotherYear)
      creditRoutes.ClaimForWhichYearController.onPageLoad(mode)
    else if (mode.equals(CheckMode) && nonExportedAmountHelper.returnsQuestionsAnswered(userAnswers))
      returnRoutes.ReturnsCheckYourAnswersController.onPageLoad()
    else
      returnRoutes.NowStartYourReturnController.onPageLoad

  def creditSummaryChange(yearKey: String): String =
    creditRoutes.ConfirmPackagingCreditController.onPageLoad(yearKey, CheckMode).url

  def creditSummaryRemove(yearKey: String): String =
    creditRoutes.CancelCreditsClaimController.onPageLoad(yearKey).url

  def cancelCredit(): Call = {
    creditRoutes.CreditsClaimedListController.onPageLoad(NormalMode)
  }

  def startYourReturn: Call =
    firstPageOfReturnSection

  def manufacturedPlasticPackaging(mode: Mode, hasAnswerChanged: Boolean, usersAnswer: Boolean): Call = {
    if (mode.equals(NormalMode))
      manufacturedRouteForNormalMode(usersAnswer)
    else
      manufacturedRouteForCheckMode(hasAnswerChanged, usersAnswer)
  }

  private def manufacturedRouteForCheckMode(hasAnswerChanged: Boolean, usersAnswer: Boolean) = {
    if (usersAnswer && hasAnswerChanged)
      returnRoutes.ManufacturedPlasticPackagingWeightController.onPageLoad(CheckMode)
    else
      returnRoutes.ConfirmPlasticPackagingTotalController.onPageLoad
  }

  private def manufacturedRouteForNormalMode(usersAnswer: Boolean) = {
    if (usersAnswer)
      returnRoutes.ManufacturedPlasticPackagingWeightController.onPageLoad(NormalMode)
    else
      returnRoutes.ImportedPlasticPackagingController.onPageLoad(NormalMode)
  }

  def manufacturedPlasticPackagingWeight(answers: UserAnswers): Call =
    answers.get(ManufacturedPlasticPackagingWeightPage) match {
      case Some(_) => returnRoutes.ConfirmPlasticPackagingTotalController.onPageLoad
      case _ => throw new Exception("Unable to navigate to page")
    }

  def importedPlasticPackaging(mode: Mode, hasAnswerChanged: Boolean, usersAnswer: Boolean): Call =
    if (mode.equals(NormalMode))
      importedPlasticPackagingRouteNormalMode(mode, usersAnswer)
    else
      importedPlasticPackagingRouteCheckMode(mode, hasAnswerChanged, usersAnswer)

  private def importedPlasticPackagingRouteCheckMode(mode: Mode, hasAnswerChanged: Boolean, usersAnswer: Boolean) =
    if (usersAnswer && hasAnswerChanged)
      returnRoutes.ImportedPlasticPackagingWeightController.onPageLoad(mode)
    else
      returnRoutes.ConfirmPlasticPackagingTotalController.onPageLoad

  private def importedPlasticPackagingRouteNormalMode(mode: Mode, usersAnswer: Boolean) =
    if (usersAnswer)
      returnRoutes.ImportedPlasticPackagingWeightController.onPageLoad(mode)
    else
      returnRoutes.ConfirmPlasticPackagingTotalController.onPageLoad

  def confirmTotalPlasticPackagingRoute(answers: UserAnswers): Call = {
    nonExportedAmountHelper.totalPlasticAdditions(answers) match {
      case Some(amount) if amount > 0 => returnRoutes.DirectlyExportedComponentsController.onPageLoad(NormalMode)
      case Some(amount) if amount <= 0 => returnRoutes.ReturnsCheckYourAnswersController.onPageLoad()
      case _ => controllers.routes.IndexController.onPageLoad
    }
  }

  def directlyExportedComponentsRoute(userAnsweredYes: Boolean, mode: Mode): Call =
    if (userAnsweredYes) {
      returnRoutes.ExportedPlasticPackagingWeightController.onPageLoad(mode)
    } else {
      returnRoutes.PlasticExportedByAnotherBusinessController.onPageLoad(mode)
    }

  def exportedPlasticPackagingWeightRoute(isAllPlasticExported: Boolean, mode: Mode): Call =
    if (mode.equals(CheckMode)) 
      returnRoutes.PlasticExportedByAnotherBusinessController.onPageLoad(mode)
    else if (isAllPlasticExported) 
      returnRoutes.ReturnsCheckYourAnswersController.onPageLoad()
    else 
      returnRoutes.PlasticExportedByAnotherBusinessController.onPageLoad(mode)

  def exportedByAnotherBusinessRoute(answers: UserAnswers, mode: Mode): Call =
    (answers.get(AnotherBusinessExportedPage), mode) match {
      case (Some(true), _) => returnRoutes.AnotherBusinessExportWeightController.onPageLoad(mode)
      case (Some(false), NormalMode) => returnRoutes.NonExportedHumanMedicinesPlasticPackagingController.onPageLoad(mode)
      case (Some(false), CheckMode) => returnRoutes.ReturnsCheckYourAnswersController.onPageLoad()
      case _ => throw new Exception("Unable to navigate to page")
    }

  def exportedByAnotherBusinessWeightRoute(isAllPlasticExported: Boolean, mode: Mode): Call = {
    if (mode.equals(CheckMode)) 
      returnRoutes.ReturnsCheckYourAnswersController.onPageLoad()
    else if (isAllPlasticExported)
      returnRoutes.ReturnsCheckYourAnswersController.onPageLoad()
    else 
      returnRoutes.NonExportedHumanMedicinesPlasticPackagingController.onPageLoad(mode)
  }

  private def nonExportedHumanMedicinesPlasticPackagingRoute(answers: UserAnswers, mode: Mode): Call =
    answers.get(NonExportedHumanMedicinesPlasticPackagingPage) match {
      case Some(true) => returnRoutes.NonExportedHumanMedicinesPlasticPackagingWeightController.onPageLoad(mode)
      case Some(false) => returnRoutes.NonExportedRecycledPlasticPackagingController.onPageLoad(mode)
      case _ => throw new Exception("Unable to navigate to page")
    }

  private def nonExportedRecycledPlasticPackagingPageRoute(answers: UserAnswers, mode: Mode): Call =
    answers.get(NonExportedRecycledPlasticPackagingPage) match {
      case Some(true) => returnRoutes.NonExportedRecycledPlasticPackagingWeightController.onPageLoad(mode)
      case Some(false) => returnRoutes.ReturnsCheckYourAnswersController.onPageLoad()
      case _ => throw new Exception("Unable to navigate to page")
    }

  def cyaChangeCredits: String =
    creditRoutes.CreditsClaimedListController.onPageLoad(CheckMode).url

}