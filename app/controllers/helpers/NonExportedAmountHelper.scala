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

package controllers.helpers

import models.requests.DataRequest
import pages.QuestionPage
import pages.returns.{DirectlyExportedComponentsPage, ExportedPlasticPackagingWeightPage, ImportedPlasticPackagingPage, ImportedPlasticPackagingWeightPage, ManufacturedPlasticPackagingPage, ManufacturedPlasticPackagingWeightPage}
import play.api.mvc.Result
import play.api.mvc.Results.Redirect

object NonExportedAmountHelper {

  private def getAmount
  (
    page: QuestionPage[Boolean],
    weightPage: QuestionPage[Long])
  (implicit request: DataRequest[_]): Option[Long] = {
    request.userAnswers.get(page).flatMap
      { _  => request.userAnswers.get(weightPage)}
  }

  def manufacturingPlasticAmount(implicit request: DataRequest[_]): Option[Long] =
    getAmount(ManufacturedPlasticPackagingPage, ManufacturedPlasticPackagingWeightPage)

  def importedPlasticAmount(implicit request: DataRequest[_]):Option[Long] =
    getAmount(ImportedPlasticPackagingPage, ImportedPlasticPackagingWeightPage)

  def exportedAmount(implicit request: DataRequest[_]):Option[Long] =
    getAmount(DirectlyExportedComponentsPage, ExportedPlasticPackagingWeightPage)

  def nonExportedAmount(implicit request: DataRequest[_]):Either[Result,Long] = {

    (for {
      manufacturing <- NonExportedAmountHelper.manufacturingPlasticAmount
      imported <- NonExportedAmountHelper.importedPlasticAmount
      exported <- NonExportedAmountHelper.exportedAmount
    } yield manufacturing + imported - exported)
      .fold[Either[Result, Long]](
        Left(Redirect(controllers.routes.IndexController.onPageLoad)))(Right(_))

  }

}
