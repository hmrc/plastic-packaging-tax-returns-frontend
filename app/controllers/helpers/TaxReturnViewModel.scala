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

import controllers.returns.routes
import models.Mode.CheckMode
import models.UserAnswers
import models.returns.{Calculations, TaxReturnObligation}
import pages.QuestionPage
import pages.returns._
import play.api.i18n.Messages
import play.api.libs.json.Reads
import viewmodels.{PrintBigDecimal, PrintLong}
import views.ViewUtils

case class RowInfo(key: String, value: String)

//todo move this to viewmodels
case class TaxReturnViewModel (
  userAnswers: UserAnswers,
  pptReference: String,
  obligation: TaxReturnObligation,
  calculations: Calculations
) (implicit messages: Messages) {

  private def getMustHave[ValueType](page: QuestionPage[ValueType])(implicit reads: Reads[ValueType]): ValueType = {
    userAnswers.get(page).getOrElse {
      throw new IllegalStateException(s"The field for '$page' is missing from user-answers")
    }
  }

  private def createYesNoRow(page: QuestionPage[Boolean], messageKey: String)(implicit reads: Reads[Boolean]) = {
    val answer = getMustHave(page)
    val value = if (answer) "site.yes" else "site.no"
    RowInfo(key = messages(messageKey), value = messages(value))
  }

  private def createYesNoRowWithDefault(page: QuestionPage[Boolean], messageKey: String)(implicit reads: Reads[Boolean]) = {
    val answer = userAnswers.get(page).getOrElse(false) // default to "no"
    val value = if (answer) "site.yes" else "site.no"
    RowInfo(key = messages(messageKey), value = messages(value))
  }

  private def createKgsRow(page: QuestionPage[Long], messageKey: String)(implicit reads: Reads[Long]) = {
    val answer = getMustHave(page)
    val value = answer.asKg
    RowInfo(key = messages(messageKey), value = value)
  }

  def manufacturedYesNo(messageKey: String): RowInfo = {
    createYesNoRow(ManufacturedPlasticPackagingPage, messageKey)
  }

  def manufacturedWeight(messageKey: String): RowInfo = {
    createKgsRow(ManufacturedPlasticPackagingWeightPage, messageKey)
  }

  def importedYesNo(messageKey: String): RowInfo = {
    createYesNoRow(ImportedPlasticPackagingPage, messageKey)
  }

  def importedWeight(messageKey: String): RowInfo = {
    createKgsRow(ImportedPlasticPackagingWeightPage, messageKey)
  }

  private def exportedTotal: Long = {
    val exported = getMustHave(ExportedPlasticPackagingWeightPage)
    val exportedByAnotherBusiness = getMustHave(AnotherBusinessExportWeightPage)

    exported + exportedByAnotherBusiness
  }

  // Show or hide edit links
  def canEditExported: Boolean    = (calculations.packagingTotal > 0 && calculations.packagingTotal > exportedTotal) || exportedTotal > 0
  def canEditNonExported: Boolean = calculations.packagingTotal > 0 && calculations.packagingTotal > exportedTotal

  def exportedYesNo(messageKey: String): RowInfo = {
    createYesNoRow(DirectlyExportedComponentsPage, messageKey)
  }

  def exportedByAnotherBusinessYesNo(messageKey: String) : RowInfo = {
    createYesNoRowWithDefault(PlasticExportedByAnotherBusinessPage, messageKey)
  }

  def exportedWeight(messageKey: String): RowInfo = {
    createKgsRow(ExportedPlasticPackagingWeightPage, messageKey)
  }

  def anotherBusinessExportedWeight(messageKey: String): RowInfo = {
    createKgsRow(AnotherBusinessExportWeightPage, messageKey)
  }

  def nonexportedMedicineYesNo(messageKey: String): RowInfo = {
    createYesNoRow(NonExportedHumanMedicinesPlasticPackagingPage, messageKey)
  }

  def nonexportedMedicineWeight(messageKey: String): RowInfo = {
    createKgsRow(NonExportedHumanMedicinesPlasticPackagingWeightPage, messageKey)
  }

  def nonexportedRecycledYesNo(messageKey: String): RowInfo = {
    createYesNoRow(NonExportedRecycledPlasticPackagingPage, messageKey)
  }

  def nonexportedRecycledWeight(messageKey: String): RowInfo = {
    createKgsRow(NonExportedRecycledPlasticPackagingWeightPage, messageKey)
  }

  def packagingTotal: String = {
    calculations.packagingTotal.asKg
  }

  def deductionsTotal: String = {
    calculations.deductionsTotal.asKg
  }

  def chargeableTotal: String = {
    calculations.chargeableTotal.asKg
  }

  def taxDue: String = {
    calculations.taxDue.asPounds
  }
  
  def packagingTotalMiniCya: String = routes.ConfirmPlasticPackagingTotalController.onPageLoad.url
  def exportedStartUrl: String = routes.DirectlyExportedComponentsController.onPageLoad(CheckMode).url
  def nonexportedStartUrl: String = routes.NonExportedHumanMedicinesPlasticPackagingController.onPageLoad(CheckMode).url

  def startDatePrettyPrint(implicit messages: Messages): String = {
    ViewUtils.displayLocalDate(obligation.fromDate)
  }

  def endDatePrettyPrint(implicit messages: Messages): String = {
    ViewUtils.displayLocalDate(obligation.toDate)
  }

  def isSubmittable: Boolean = calculations.isSubmittable
}
