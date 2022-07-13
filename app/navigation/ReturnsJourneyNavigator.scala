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

import controllers.returns.routes
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import pages._
import pages.returns._
import play.api.mvc.Call

import javax.inject.Singleton

@Singleton
class ReturnsJourneyNavigator {

  type AnswerChanged = Boolean

  private def exportedAllPlastic(answers: UserAnswers): Boolean = {
    val manufactured = answers.get(ManufacturedPlasticPackagingWeightPage).getOrElse(0L)
    val imported = answers.get(ImportedPlasticPackagingWeightPage).getOrElse(0L)
    val exported = answers.get(ExportedPlasticPackagingWeightPage).getOrElse(0L)

    exported >= (manufactured + imported)
  }

  val normalRoutes: PartialFunction[Page, UserAnswers => Call] = {
    case StartYourReturnPage => startYourReturnRoute
    case ManufacturedPlasticPackagingPage => manufacturedPlasticPackagingRoute(_, mode = NormalMode)
    case ImportedPlasticPackagingPage => importedPlasticPackagingRoute(_, mode = NormalMode)
    case ManufacturedPlasticPackagingWeightPage =>
      _ => routes.ImportedPlasticPackagingController.onPageLoad(NormalMode)
    case ImportedPlasticPackagingWeightPage =>
      _ => routes.ConfirmPlasticPackagingTotalController.onPageLoad
    case DirectlyExportedComponentsPage => directlyExportedComponentsRoute(_, mode = NormalMode)
    case ExportedPlasticPackagingWeightPage => exportedPlasticPackagingWeightRoute(_, mode = NormalMode)
    case NonExportedHumanMedicinesPlasticPackagingPage => nonExportedHumanMedicinesPlasticPackagingRoute(_, mode = NormalMode)
    case NonExportedHumanMedicinesPlasticPackagingWeightPage => _ => routes.NonExportedRecycledPlasticPackagingController.onPageLoad(NormalMode)
    case NonExportedRecycledPlasticPackagingPage => nonExportedrecycledPlasticPackagingPageRoute(_, mode = NormalMode)
    case NonExportedRecycledPlasticPackagingWeightPage => _ => routes.ReturnsCheckYourAnswersController.onPageLoad
  }

  val checkRoutes: PartialFunction[Page, (UserAnswers, AnswerChanged) => Call] = {
    case ManufacturedPlasticPackagingPage => (answers, answerChanged) => manufacturedPlasticPackagingRoute(answers, mode = CheckMode, answerChanged)
    case ManufacturedPlasticPackagingWeightPage => (answers, _) => manufacturedPlasticPackagingWeightRoute(answers)
    case ImportedPlasticPackagingPage => (answers, answersChanged) => importedPlasticPackagingRoute(answers, mode = CheckMode, answersChanged)
    case ImportedPlasticPackagingWeightPage => (_, _) => routes.ConfirmPlasticPackagingTotalController.onPageLoad
    case DirectlyExportedComponentsPage => (answers, _) => directlyExportedComponentsRoute(answers, mode = CheckMode)
    case ExportedPlasticPackagingWeightPage => (answers, _) => exportedPlasticPackagingWeightRoute(answers, mode = CheckMode)
    case NonExportedHumanMedicinesPlasticPackagingPage => (answers, _) => nonExportedHumanMedicinesPlasticPackagingRoute(answers, mode = CheckMode)
    case NonExportedHumanMedicinesPlasticPackagingWeightPage => (_, _) => routes.NonExportedRecycledPlasticPackagingController.onPageLoad(CheckMode)
    case NonExportedRecycledPlasticPackagingPage => (answers, _) => nonExportedrecycledPlasticPackagingPageRoute(answers, mode = CheckMode)
    case NonExportedHumanMedicinesPlasticPackagingPage => (answers, _) => nonExportedHumanMedicinesPlasticPackagingRoute(answers, mode = CheckMode)
    case _ => (_, _) => routes.ReturnsCheckYourAnswersController.onPageLoad
  }

  private def startYourReturnRoute(answers: UserAnswers): Call =
    answers.get(StartYourReturnPage) match {
      case Some(true) => routes.ManufacturedPlasticPackagingController.onPageLoad(NormalMode)
      case Some(false) => routes.NotStartOtherReturnsController.onPageLoad
      case _ => throw new Exception("Unable to navigate to page")
    }

  private def manufacturedPlasticPackagingRoute(answers: UserAnswers, mode: Mode = NormalMode, answerChanged: AnswerChanged = false): Call = {
    if (mode == CheckMode) {
      answers.get(ManufacturedPlasticPackagingPage) match {
        case Some(true) if answerChanged => routes.ManufacturedPlasticPackagingWeightController.onPageLoad(mode)
        case Some(false) | Some(true) if !answerChanged => routes.ConfirmPlasticPackagingTotalController.onPageLoad
        case _ => throw new Exception("Unable to navigate to page")
      }
    } else {
      answers.get(ManufacturedPlasticPackagingPage) match {
        case Some(true) => routes.ManufacturedPlasticPackagingWeightController.onPageLoad(mode)
        case Some(false) => routes.ImportedPlasticPackagingController.onPageLoad(mode)
        case _ => throw new Exception("Unable to navigate to page")
      }
    }
  }

  private def manufacturedPlasticPackagingWeightRoute(answers: UserAnswers): Call =
    answers.get(ManufacturedPlasticPackagingWeightPage) match {
      case Some(_) => routes.ConfirmPlasticPackagingTotalController.onPageLoad
      case _ => throw new Exception("Unable to navigate to page")
    }

  private def importedPlasticPackagingRoute(answers: UserAnswers, mode: Mode = NormalMode, answerChanged: AnswerChanged = false): Call = {
    if (mode == CheckMode) {
      answers.get(ImportedPlasticPackagingPage) match {
        case Some(true) if answerChanged => routes.ImportedPlasticPackagingWeightController.onPageLoad(mode)
        case Some(false) | Some(true) if !answerChanged => routes.ConfirmPlasticPackagingTotalController.onPageLoad
        case _ => throw new Exception("Unable to navigate to page")
      }
    } else {
      answers.get(ImportedPlasticPackagingPage) match {
        case Some(true) => routes.ImportedPlasticPackagingWeightController.onPageLoad(mode)
        case Some(false) => routes.ConfirmPlasticPackagingTotalController.onPageLoad
        case _ => throw new Exception("Unable to navigate to page")
      }
    }
  }

  private def directlyExportedComponentsRoute(answers: UserAnswers, mode: Mode = NormalMode): Call =
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

  private def nonExportedrecycledPlasticPackagingPageRoute(answers: UserAnswers, mode: Mode): Call =
    answers.get(NonExportedRecycledPlasticPackagingPage) match {
      case Some(true) => routes.NonExportedRecycledPlasticPackagingWeightController.onPageLoad(mode)
      case Some(false) => routes.ReturnsCheckYourAnswersController.onPageLoad()
      case _ => throw new Exception("Unable to navigate to page")
    }
}