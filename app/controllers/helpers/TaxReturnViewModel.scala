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

import controllers.returns.routes
import models.requests.DataRequest
import models.returns.TaxReturnObligation
import models.{CheckMode, UserAnswers}
import pages.QuestionPage
import pages.returns._
import play.api.i18n.Messages
import play.api.libs.json.Reads
import viewmodels.{PrintBigDecimal, PrintLong}
import views.ViewUtils

import scala.math.BigDecimal.RoundingMode

case class RowInfo(key: String, value: String)

//todo move this to viewmodels
case class TaxReturnViewModel (
  request: DataRequest[_],
  obligation: TaxReturnObligation,
  calculations: Calculations
) (implicit messages: Messages) {

  private def userAnswers: UserAnswers = request.userAnswers

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

  private def createKgsRow(page: QuestionPage[Long], messageKey: String)(implicit reads: Reads[Long]) = {
    val answer = getMustHave(page)
    val value = answer.asKgs
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

  // Show or hide edit links
  def exportedTotal: Long         = getMustHave(ExportedPlasticPackagingWeightPage)

  def canEditExported: Boolean    = (calculations.packagingTotal > 0 && calculations.packagingTotal > exportedTotal) || exportedTotal > 0
  def canEditNonExported: Boolean = calculations.packagingTotal > 0 && calculations.packagingTotal > exportedTotal
  // End

  def exportedYesNo(messageKey: String): RowInfo = {
    createYesNoRow(DirectlyExportedComponentsPage, messageKey)
  }

  def exportedWeight(messageKey: String): RowInfo = {
    createKgsRow(ExportedPlasticPackagingWeightPage, messageKey)
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

  // Calcs here
  // TODO - move to a calculations object in the back end

  def packagingTotal: String = {
    calculations.packagingTotal.asKg
  }

//  private def packagingTotalNumeric: Long = {
//    (getMustHave(ManufacturedPlasticPackagingWeightPage)
//      + getMustHave(ImportedPlasticPackagingWeightPage))
//  }

//  private def deductionsTotalNumeric: Long = {
//    (getMustHave(ExportedPlasticPackagingWeightPage)
//     + getMustHave(NonExportedHumanMedicinesPlasticPackagingWeightPage)
//     + getMustHave(NonExportedRecycledPlasticPackagingWeightPage))
//  }

  def deductionsTotal: String = {
    calculations.deductionsTotal.asKg
  }


//  private def chargeableTotalNumeric = {
//    scala.math.max(0, (packagingTotalNumeric - deductionsTotalNumeric))
//  }

  def chargeableTotal: String = {
    calculations.chargeableTotal.asKg
  }

  def taxDue: String = {
    calculations.taxDue.asPounds
  }

  // End calc
  
  def packagingTotalStartUrl: String = routes.ManufacturedPlasticPackagingController.onPageLoad(CheckMode).url
  def exportedStartUrl: String = routes.DirectlyExportedComponentsController.onPageLoad(CheckMode).url
  def nonexportedStartUrl: String = routes.NonExportedHumanMedicinesPlasticPackagingController.onPageLoad(CheckMode).url

  def startDatePrettyPrint(implicit messages: Messages): String = {
    ViewUtils.displayLocalDate(obligation.fromDate)
  }

  def endDatePrettyPrint(implicit messages: Messages): String = {
    ViewUtils.displayLocalDate(obligation.toDate)
  }

  def isSubmittable: Boolean = calculations.isSubmittable //calculations.packagingTotal >= calculations.deductionsTotal
}

case class Calculations(taxDue: BigDecimal,
                        chargeableTotal: BigDecimal,
                        deductionsTotal: BigDecimal,
                        packagingTotal: BigDecimal,
                        isSubmittable: Boolean)
