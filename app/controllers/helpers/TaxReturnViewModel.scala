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
  obligation: TaxReturnObligation
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

  private def packagingTotalNumeric: Long = {
    (getMustHave(ManufacturedPlasticPackagingWeightPage) 
      + getMustHave(ImportedPlasticPackagingWeightPage))
  }

  // Show or hide edit links
  def exportedTotal: Long         = getMustHave(ExportedPlasticPackagingWeightPage)
  def canEditExported: Boolean    = (packagingTotalNumeric > 0 && packagingTotalNumeric > exportedTotal) || exportedTotal > 0
  def canEditNonExported: Boolean = packagingTotalNumeric > 0 && packagingTotalNumeric > exportedTotal
  // End

  def packagingTotal: String = {
    packagingTotalNumeric.asKg
  }


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

  private def deductionsTotalNumeric: Long = {
    (getMustHave(ExportedPlasticPackagingWeightPage)
     + getMustHave(NonExportedHumanMedicinesPlasticPackagingWeightPage)
     + getMustHave(NonExportedRecycledPlasticPackagingWeightPage))
  }

  def deductionsTotal: String = {
    deductionsTotalNumeric.asKg
  }


  private def chargeableTotalNumeric = {
    // TODO if totalDeductions > totalPackaging is this the correct behaviour?!
    scala.math.max(0, (packagingTotalNumeric - deductionsTotalNumeric))
  }

  def chargeableTotal: String = {
    chargeableTotalNumeric.asKg
  }


  // TODO robbed from TaxLiabilityFactory - should be in app-config?
  private val taxValueInPencePerKg = BigDecimal("0.20")

  def taxDue: String = {
    val taxDue = taxValueInPencePerKg * BigDecimal(chargeableTotalNumeric).setScale(2, RoundingMode.HALF_EVEN)
    taxDue.asPounds
  }

  
  def packagingTotalStartUrl: String = routes.ManufacturedPlasticPackagingController.onPageLoad(CheckMode).url
  def exportedStartUrl: String = routes.DirectlyExportedComponentsController.onPageLoad(CheckMode).url
  def nonexportedStartUrl: String = routes.NonExportedHumanMedicinesPlasticPackagingController.onPageLoad(CheckMode).url

  def startDatePrettyPrint(implicit messages: Messages): String = {
    ViewUtils.displayLocalDate(obligation.fromDate)
  }

  def endDatePrettyPrint(implicit messages: Messages): String = {
    ViewUtils.displayLocalDate(obligation.toDate)
  }

  def isSubmittable: Boolean = packagingTotalNumeric >= deductionsTotalNumeric
}
