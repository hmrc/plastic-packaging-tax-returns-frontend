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

package controllers.helpers

import com.google.inject.Singleton
import models.UserAnswers
import pages.QuestionPage
import pages.returns._

@Singleton
class NonExportedAmountHelper { //todo rename?

  def returnsQuestionsAnswered(userAnswers: UserAnswers): Boolean =
    userAnswers.get(ManufacturedPlasticPackagingWeightPage).isDefined &&
    userAnswers.get(ImportedPlasticPackagingWeightPage).isDefined &&
    userAnswers.get(DirectlyExportedWeightPage).isDefined &&
    userAnswers.get(AnotherBusinessExportedWeightPage).isDefined &&
    userAnswers.get(NonExportedHumanMedicinesPlasticPackagingWeightPage).isDefined &&
    userAnswers.get(NonExportedRecycledPlasticPackagingWeightPage).isDefined

  def totalPlasticAdditions(userAnswers: UserAnswers): Option[Long] = {
    for {
      manufacturing <- manufacturingPlasticAmount(userAnswers)
      imported <- importedPlasticAmount(userAnswers)
    } yield manufacturing + imported
  }

  def nonExportedAmount(userAnswers: UserAnswers): Option[Long] = {
    for {
      manufacturing <- manufacturingPlasticAmount(userAnswers)
      imported <- importedPlasticAmount(userAnswers)
      exported <- directlyExportedAmount(userAnswers)
      exportedByAnotherBusiness = exportedByAnotherBusinessAmount(userAnswers)
    } yield manufacturing + imported - (exported + exportedByAnotherBusiness)
  }

  def getAmountAndDirectlyExportedAnswer(userAnswers: UserAnswers): Option[(Long, Boolean, Boolean)] = {
    val value = nonExportedAmount(userAnswers).get
    val isDirectExportYesNo = userAnswers.get(DirectlyExportedPage).get
    val isAnotherBusinessYesNo = userAnswers.get(AnotherBusinessExportedPage).getOrElse(false) // default to no if not answered
    Some(value, isDirectExportYesNo, isAnotherBusinessYesNo)
  }

  private def getAmount(
                         userAnswer: UserAnswers,
                         page: QuestionPage[Boolean],
                         weightPage: QuestionPage[Long]
                       ): Option[Long] = {
    userAnswer.get(page).flatMap { _ => userAnswer.get(weightPage) }
  }

  def manufacturingPlasticAmount(userAnswer: UserAnswers): Option[Long] =
    getAmount(userAnswer, ManufacturedPlasticPackagingPage, ManufacturedPlasticPackagingWeightPage)

  def importedPlasticAmount(userAnswer: UserAnswers):Option[Long] =
    getAmount(userAnswer, ImportedPlasticPackagingPage, ImportedPlasticPackagingWeightPage)

  def directlyExportedAmount(userAnswer: UserAnswers):Option[Long] =
    getAmount(userAnswer, DirectlyExportedPage, DirectlyExportedWeightPage)

  private def exportedByAnotherBusinessAmount(userAnswer: UserAnswers): Long =
    getAmount(userAnswer, AnotherBusinessExportedPage, AnotherBusinessExportedWeightPage)
      .getOrElse(0L) // default to zero kg if unanswered
}
