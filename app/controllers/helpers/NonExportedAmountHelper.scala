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

import models.UserAnswers
import pages.QuestionPage
import pages.returns._

object NonExportedAmountHelper {

  def totalPlastic(userAnswers: UserAnswers) = {
    for {
      manufacturing <- manufacturingPlasticAmount(userAnswers)
      imported <- importedPlasticAmount(userAnswers)
    } yield manufacturing + imported
  }

  def nonExportedAmount(userAnswers: UserAnswers):Option[Long] = {

    for {
      manufacturing <- manufacturingPlasticAmount(userAnswers)
      imported <- importedPlasticAmount(userAnswers)
      exported <- exportedAmount(userAnswers)
      exportedByAnotherBusiness <- exportedByAnotherBusinessAmount(userAnswers)
    } yield manufacturing + imported - (exported + exportedByAnotherBusiness)
  }

  def getAmountAndDirectlyExportedAnswer(userAnswers: UserAnswers): Option[(Long, Boolean, Boolean)] = {
    for {
      isDirectExportYesNo <- userAnswers.get(DirectlyExportedComponentsPage)
      isAnotherBusinessYesNo <- userAnswers.get(PlasticExportedByAnotherBusinessPage)
      value <- nonExportedAmount(userAnswers)
    } yield (value, isDirectExportYesNo, isAnotherBusinessYesNo)
  }

  private def getAmount(
    userAnswer: UserAnswers,
    page: QuestionPage[Boolean],
    weightPage: QuestionPage[Long]
  ): Option[Long] = {
    userAnswer.get(page).flatMap { _  => userAnswer.get(weightPage) }
  }

  private def manufacturingPlasticAmount(userAnswer: UserAnswers): Option[Long] =
    getAmount(userAnswer, ManufacturedPlasticPackagingPage, ManufacturedPlasticPackagingWeightPage)

  private def importedPlasticAmount(userAnswer: UserAnswers):Option[Long] =
    getAmount(userAnswer, ImportedPlasticPackagingPage, ImportedPlasticPackagingWeightPage)

  private def exportedAmount(userAnswer: UserAnswers):Option[Long] =
    getAmount(userAnswer, DirectlyExportedComponentsPage, ExportedPlasticPackagingWeightPage)

  private def exportedByAnotherBusinessAmount(userAnswer: UserAnswers):Option[Long] =
    getAmount(userAnswer, PlasticExportedByAnotherBusinessPage, AnotherBusinessExportWeightPage)
}
