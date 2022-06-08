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

  private def exportSplit(answers: UserAnswers): Boolean = {
    val total = answers.get(ManufacturedPlasticPackagingWeightPage).getOrElse(0L)
    val exported = answers.get(ExportedPlasticPackagingWeightPage).getOrElse(0L)

    exported < total
  }

  val normalRoutes: PartialFunction[Page, UserAnswers => Call] = {

    case StartYourReturnPage => startYourReturnRoute
    case ManufacturedPlasticPackagingPage => manufacturedPlasticPackagingRoute(_, mode = NormalMode)
    case ImportedPlasticPackagingPage => importedPlasticPackagingRoute(_, mode = NormalMode)
    case ManufacturedPlasticPackagingWeightPage =>
      _ => routes.ImportedPlasticPackagingController.onPageLoad(NormalMode)
    case ImportedPlasticPackagingWeightPage =>
      _ => routes.ConfirmPlasticPackagingTotalController.onPageLoad
    case HumanMedicinesPlasticPackagingPage => humanMedicinesPlasticPackagingRoute(_, mode = NormalMode)
    case HumanMedicinesPlasticPackagingWeightPage =>
      _ => routes.ExportedRecycledPlasticPackagingController.onPageLoad(NormalMode)
    case DirectlyExportedComponentsPage => directlyExportedComponentsRoute(_, mode = NormalMode)
    case ExportedPlasticPackagingWeightPage =>
      _ => routes.HumanMedicinesPlasticPackagingController.onPageLoad(NormalMode)
    case NonExportedHumanMedicinesPlasticPackagingPage => nonExportedHumanMedicinesPlasticPackagingRoute(_, mode = NormalMode)
    case NonExportedHumanMedicinesPlasticPackagingWeightPage => _ => routes.NonExportedRecycledPlasticPackagingController.onPageLoad(NormalMode)
    case ExportedRecycledPlasticPackagingPage => exportedRecycledPlasticPackagingPageRoute(_, mode = NormalMode)
    case ExportedRecycledPlasticPackagingWeightPage => exportedRecycledPlasticPackagingWeightPageRoute(_, mode = NormalMode)
    case NonExportedRecycledPlasticPackagingPage => recycledPlasticPackagingPageRoute(_, mode = NormalMode)
    case NonExportedRecycledPlasticPackagingWeightPage => _ => routes.ConvertedPackagingCreditController.onPageLoad(NormalMode)
    case ConvertedPackagingCreditPage => _ => routes.ReturnsCheckYourAnswersController.onPageLoad
  }

  val checkRoutes: PartialFunction[Page, UserAnswers => Call] = {
    case ManufacturedPlasticPackagingPage => manufacturedPlasticPackagingRoute(_, mode = CheckMode)
    case ManufacturedPlasticPackagingWeightPage => manufacturedPlasticPackagingWeightRoute
    case ImportedPlasticPackagingPage => importedPlasticPackagingRoute(_, mode = CheckMode)
    case ImportedPlasticPackagingWeightPage => _ => routes.ConfirmPlasticPackagingTotalController.onPageLoad
    case DirectlyExportedComponentsPage => directlyExportedComponentsRoute(_, mode = CheckMode)
    case ExportedPlasticPackagingWeightPage =>
      _ => routes.HumanMedicinesPlasticPackagingController.onPageLoad(CheckMode)
    case HumanMedicinesPlasticPackagingPage => humanMedicinesPlasticPackagingRoute(_, mode = CheckMode)
    case HumanMedicinesPlasticPackagingWeightPage =>
      _ => routes.ExportedRecycledPlasticPackagingController.onPageLoad(CheckMode)
    case ExportedRecycledPlasticPackagingPage => exportedRecycledPlasticPackagingPageRoute(_, mode = CheckMode)
    case ExportedRecycledPlasticPackagingWeightPage => exportedRecycledPlasticPackagingWeightPageRoute(_, mode = CheckMode)
    case NonExportedHumanMedicinesPlasticPackagingPage => nonExportedHumanMedicinesPlasticPackagingRoute(_, mode = CheckMode)
    case NonExportedHumanMedicinesPlasticPackagingWeightPage => _ => routes.NonExportedRecycledPlasticPackagingController.onPageLoad(CheckMode)
    case NonExportedRecycledPlasticPackagingPage => recycledPlasticPackagingPageRoute(_, mode = CheckMode)
    case NonExportedHumanMedicinesPlasticPackagingPage => nonExportedHumanMedicinesPlasticPackagingRoute(_, mode = CheckMode)
    case _ => _ => routes.ReturnsCheckYourAnswersController.onPageLoad
  }

  private def startYourReturnRoute(answers: UserAnswers): Call =
    answers.get(StartYourReturnPage) match {
      case Some(true) => routes.ManufacturedPlasticPackagingController.onPageLoad(NormalMode)
      case Some(false) => routes.NotStartOtherReturnsController.onPageLoad
      case _ => throw new Exception("Unable to navigate to page")
    }


  private def manufacturedPlasticPackagingRoute(answers: UserAnswers, mode: Mode = NormalMode): Call =
    answers.get(ManufacturedPlasticPackagingPage) match {
      case Some(true) => routes.ManufacturedPlasticPackagingWeightController.onPageLoad(mode)
      case Some(false) =>
        if (mode == CheckMode) {
          routes.ConfirmPlasticPackagingTotalController.onPageLoad
        } else {
          routes.ImportedPlasticPackagingController.onPageLoad(mode)
        }
      case _ => throw new Exception("Unable to navigate to page")
    }

  private def manufacturedPlasticPackagingWeightRoute(answers: UserAnswers): Call =
    answers.get(ManufacturedPlasticPackagingWeightPage) match {
      case Some(_) => routes.ConfirmPlasticPackagingTotalController.onPageLoad
      case _ => throw new Exception("Unable to navigate to page")
    }

  private def importedPlasticPackagingRoute(answers: UserAnswers, mode: Mode = NormalMode): Call =
    answers.get(ImportedPlasticPackagingPage) match {
      case Some(true) => routes.ImportedPlasticPackagingWeightController.onPageLoad(mode)
      case Some(false) =>
        if (mode == CheckMode) {
          routes.ConfirmPlasticPackagingTotalController.onPageLoad
        } else {
          routes.ConfirmPlasticPackagingTotalController.onPageLoad
        }
      case _ => throw new Exception("Unable to navigate to page")
    }

  /*******************************************************************************************
  // Start of exported / non-exported routing mini loops                                     *
  // These loops must be preserved for normal and check modes                                *
  // Any changes from the direct export question implies full completion of the mini loop    *
  // Edit at ANY point in a mini loop also implies full completion of that loop              *
  *******************************************************************************************/

  private def directlyExportedComponentsRoute(answers: UserAnswers, mode: Mode = NormalMode): Call =
    answers.get(DirectlyExportedComponentsPage) match {
      case Some(true)  => routes.ExportedPlasticPackagingWeightController.onPageLoad(mode)
      case Some(false) => routes.NonExportedHumanMedicinesPlasticPackagingController.onPageLoad(mode)
      case _           => throw new Exception("Unable to navigate to page")
    }

  private def humanMedicinesPlasticPackagingRoute(answers: UserAnswers, mode: Mode): Call =
    answers.get(HumanMedicinesPlasticPackagingPage) match {
      case Some(true)  => routes.HumanMedicinesPlasticPackagingWeightController.onPageLoad(mode)
      case Some(false) => routes.ExportedRecycledPlasticPackagingController.onPageLoad(mode)
      case _           => throw new Exception("Unable to navigate to page")
    }

  private def nonExportedHumanMedicinesPlasticPackagingRoute(answers: UserAnswers, mode: Mode): Call =
    answers.get(NonExportedHumanMedicinesPlasticPackagingPage) match {
      case Some(true)  => routes.NonExportedHumanMedicinesPlasticPackagingWeightController.onPageLoad(mode)
      case Some(false) => routes.NonExportedRecycledPlasticPackagingController.onPageLoad(mode)
      case _           => throw new Exception("Unable to navigate to page")
    }

  private def exportedRecycledPlasticPackagingPageRoute(answers: UserAnswers, mode: Mode): Call =
    answers.get(ExportedRecycledPlasticPackagingPage) match {
      case Some(true)  => routes.ExportedRecycledPlasticPackagingWeightController.onPageLoad(mode)
      case Some(false) => routes.NonExportedHumanMedicinesPlasticPackagingController.onPageLoad(mode)
      case _           => throw new Exception("Unable to navigate to page")
    }

  private def recycledPlasticPackagingPageRoute(answers: UserAnswers, mode: Mode): Call =
    answers.get(NonExportedRecycledPlasticPackagingPage) match {
      case Some(true)  => routes.NonExportedRecycledPlasticPackagingWeightController.onPageLoad(mode)
      case Some(false) =>
        if(mode == CheckMode) {
         routes.ReturnsCheckYourAnswersController.onPageLoad()
        } else {
          routes.ConvertedPackagingCreditController.onPageLoad(mode)
        }
      case _           => throw new Exception("Unable to navigate to page")
    }

  private def exportedRecycledPlasticPackagingWeightPageRoute(answers: UserAnswers, mode: Mode): Call =
    exportSplit(answers) match {
      case true  => routes.NonExportedHumanMedicinesPlasticPackagingController.onPageLoad(mode)
      case false =>
        if(mode == CheckMode) {
          routes.ReturnsCheckYourAnswersController.onPageLoad()
        } else {
          routes.ConvertedPackagingCreditController.onPageLoad(mode)
        }
      case _     => throw new Exception("Unable to navigate to page")
    }
}
