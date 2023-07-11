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
import config.FrontendAppConfig
import controllers.helpers.NonExportedAmountHelper
import controllers.returns.credits.{routes => creditRoutes}
import controllers.returns.{routes => returnRoutes}
import models.Mode.{CheckMode, NormalMode}
import models.returns.CreditRangeOption
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

  def nonExportedRecycledPlasticPackagingWeightPage() =
    returnRoutes.ReturnsCheckYourAnswersController.onPageLoad()

  def importedPlasticPackagingWeightPage() =
    returnRoutes.ConfirmPlasticPackagingTotalController.onPageLoad

  def manufacturedPlasticPackagingWeightPage(mode: Mode) =
    if (mode == NormalMode) returnRoutes.ImportedPlasticPackagingController.onPageLoad(NormalMode)
    else returnRoutes.ConfirmPlasticPackagingTotalController.onPageLoad

  def nonExportedHumanMedicinesPlasticPackagingPage(mode: Mode, yesNo: Boolean) =
      if (yesNo) returnRoutes.NonExportedHumanMedicinesPlasticPackagingWeightController.onPageLoad(mode)
      else returnRoutes.NonExportedRecycledPlasticPackagingController.onPageLoad(mode)

  def nonExportedHumanMedicinesPlasticPackagingWeightPage(mode: Mode) =
    returnRoutes.NonExportedRecycledPlasticPackagingController.onPageLoad(mode)

  def nonExportedRecycledPlasticPackagingPage(mode: Mode, yesNo: Boolean) =
    if (yesNo) returnRoutes.NonExportedRecycledPlasticPackagingWeightController.onPageLoad(mode)
    else returnRoutes.ReturnsCheckYourAnswersController.onPageLoad()

  def firstPageOfReturnSection =
    returnRoutes.ManufacturedPlasticPackagingController.onPageLoad(NormalMode)

  def whatDoYouWantDo(isClaimingCredit: Boolean): Call = {
    if(isClaimingCredit) creditRoutes.ClaimForWhichYearController.onPageLoad(NormalMode)
    else firstPageOfReturnSection
  }

  def claimForWhichYear(year: CreditRangeOption, mode: Mode): Call =
    creditRoutes.ExportedCreditsController.onPageLoad(year.key, mode)

  private def isConvertCreditQuestionAnswered(key: String, userAnswers: UserAnswers) = {
    // It's possible to be changing credit answers whilst changing a year, whilst in CheckMode from the final CYA 
    // page. To emulate this kind of double / nested check mode, we check to see if the converted question has 
    // been answered as a proxy. If it hasn't, we assume you're adding another year to the claim, so continue as 
    // normal mode would. If it has been answered, then we assume that you are in the nested-check mode and go 
    // back to the single year, mini-cya
    userAnswers.get(ConvertedCreditsPage(key)).isDefined
  }

  def exportedCreditsYesNo(key: String, mode: Mode, isYes: Boolean, userAnswers: UserAnswers): Call = {
    val isCheckMode = mode == CheckMode && isConvertCreditQuestionAnswered(key, userAnswers)
    (isCheckMode, isYes) match {
      case (_, true) => creditRoutes.ExportedCreditsWeightController.onPageLoad(key, mode)
      case (false, false) => creditRoutes.ConvertedCreditsController.onPageLoad(key, mode)
      case (true, false) =>  creditRoutes.ConfirmPackagingCreditController.onPageLoad(key, mode)
    }
  }

  def exportedCreditsWeight(key: String, mode: Mode, userAnswers: UserAnswers): Call = {
    val isCheckMode = mode == CheckMode && isConvertCreditQuestionAnswered(key, userAnswers)
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
    else if (nonExportedAmountHelper.returnsQuestionsAnswered(userAnswers))
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

  def startYourReturn(doesUserWantToStartReturn: Boolean): Call =
    if (doesUserWantToStartReturn)
        firstPageOfReturnSection
    else
      returnRoutes.NotStartOtherReturnsController.onPageLoad()

  def manufacturedPlasticPackaging(mode: Mode, hasAnswerChanged: Boolean, yesNo: Boolean): Call = {
    (mode, hasAnswerChanged, yesNo) match {
      case (NormalMode, _, true) => returnRoutes.ManufacturedPlasticPackagingWeightController.onPageLoad(NormalMode)
      case (NormalMode, _, _) => returnRoutes.ImportedPlasticPackagingController.onPageLoad(NormalMode)
      case (_, true, true) => returnRoutes.ManufacturedPlasticPackagingWeightController.onPageLoad(CheckMode)
      case _ => returnRoutes.ConfirmPlasticPackagingTotalController.onPageLoad
    }
  }

  def importedPlasticPackaging(mode: Mode, hasAnswerChanged: Boolean, yesNo: Boolean): Call = {
    (mode, hasAnswerChanged, yesNo) match {
      case (NormalMode, _, true) => returnRoutes.ImportedPlasticPackagingWeightController.onPageLoad(mode)
      case (NormalMode, _, _) => returnRoutes.ConfirmPlasticPackagingTotalController.onPageLoad
      case (_,true, true) => returnRoutes.ImportedPlasticPackagingWeightController.onPageLoad(mode)
      case _ => returnRoutes.ConfirmPlasticPackagingTotalController.onPageLoad
    }
  }

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

  def exportedByAnotherBusinessRoute(answer: Boolean, mode: Mode): Call =
    (answer, mode) match {
      case (true, _) => returnRoutes.AnotherBusinessExportWeightController.onPageLoad(mode)
      case (false, NormalMode) => returnRoutes.NonExportedHumanMedicinesPlasticPackagingController.onPageLoad(mode)
      case (false, CheckMode) => returnRoutes.ReturnsCheckYourAnswersController.onPageLoad()
    }

  def exportedByAnotherBusinessWeightRoute(isAllPlasticExported: Boolean, mode: Mode): Call = {
    if (mode.equals(CheckMode)) 
      returnRoutes.ReturnsCheckYourAnswersController.onPageLoad()
    else if (isAllPlasticExported)
      returnRoutes.ReturnsCheckYourAnswersController.onPageLoad()
    else 
      returnRoutes.NonExportedHumanMedicinesPlasticPackagingController.onPageLoad(mode)
  }

  def cyaChangeCredits: String =
    creditRoutes.CreditsClaimedListController.onPageLoad(CheckMode).url

}