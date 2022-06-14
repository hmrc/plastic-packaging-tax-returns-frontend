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

import config.FrontendAppConfig
import controllers.returns.routes
import models.requests.DataRequest
import models.returns.TaxReturnObligation
import models.{CheckMode, UserAnswers}
import pages.QuestionPage
import pages.returns._
import play.api.i18n.Messages
import play.api.libs.json.Reads
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow, Value}
import viewmodels.checkAnswers.SummaryViewModel
import viewmodels.checkAnswers.returns._
import viewmodels.{InputWidth, PrintBigDecimal, PrintLong}
import views.ViewUtils

import scala.math.BigDecimal.RoundingMode
import scala.reflect.ClassTag
import viewmodels.PrintLong

case class TaxReturnViewModel (
  private val request: DataRequest[_], 
  private val obligation: TaxReturnObligation,
  private val appConfig: FrontendAppConfig
) (implicit messages: Messages) {

  private def userAnswers: UserAnswers = request.userAnswers

  private def instantiate[PageType <: SummaryViewModel](messageKey: String, tag: ClassTag[PageType]) = {
    tag.runtimeClass.getConstructor(classOf[String])
      .newInstance(messageKey).asInstanceOf[PageType]
  }

  private def ensureAnswer[PageType <: SummaryViewModel: ClassTag]: Long = {
    val tag = implicitly[ClassTag[PageType]]
    instantiate("", tag)
      .answer(userAnswers)
      .getOrElse {
        val className = tag.runtimeClass.getSimpleName
        val errorMessage = s"The field for '$className' is missing from user-answers"
        throw new IllegalStateException(errorMessage)
      }
  }
  
  private def getMustHave[ValueType](page: QuestionPage[ValueType])(implicit reads: Reads[ValueType]): ValueType = {
    userAnswers.get(page).getOrElse {
      throw new IllegalStateException(s"The field for '$page' is missing from user-answers")
    }
  }

  private def createRow(key: String, value: String) = {
    SummaryListRow(
      key = Key(content = Text(key), classes = s"govuk-!-font-weight-regular ${InputWidth.ThreeQuarters}"),
      value = Value(content = Text(value), classes = "govuk-!-width-one-quarter govuk-table__cell--numeric"),
    )
  }

  private def createYesNoRow(page: QuestionPage[Boolean], messageKey: String)(implicit reads: Reads[Boolean]) = {
    val answer = getMustHave(page)
    val value = if (answer) "site.yes" else "site.no"
    createRow(messages(messageKey), messages(value))
  }

  private def createKgsRow(page: QuestionPage[Long], messageKey: String)(implicit reads: Reads[Long]) = {
    val answer = getMustHave(page)
    val value = answer.asKgs
    createRow(messages(messageKey), value)
  }

  def manufacturedYesNo(messageKey: String): SummaryListRow = {
    createYesNoRow(ManufacturedPlasticPackagingPage, messageKey)
  }

  def manufacturedWeight(messageKey: String): SummaryListRow = {
    createKgsRow(ManufacturedPlasticPackagingWeightPage, messageKey)
  }

  def importedYesNo(messageKey: String): SummaryListRow = {
    createYesNoRow(ImportedPlasticPackagingPage, messageKey)
  }

  def importedWeight(messageKey: String): SummaryListRow = {
    createKgsRow(ImportedPlasticPackagingWeightPage, messageKey)
  }

  private def packagingTotalNumeric: Long = {
    (ensureAnswer[ManufacturedPlasticPackagingWeightSummary]
      + ensureAnswer[ImportedPlasticPackagingWeightSummary])
  }

  def packagingTotal: String = {
    packagingTotalNumeric.asKgs
  }


  def exportedYesNo(messageKey: String): SummaryListRow = {
    createYesNoRow(DirectlyExportedComponentsPage, messageKey)
  }

  def exportedWeight(messageKey: String): SummaryListRow = {
    createKgsRow(ExportedPlasticPackagingWeightPage, messageKey)
  }

  def nonexportedMedicineYesNo(messageKey: String): SummaryListRow = {
    createYesNoRow(NonExportedHumanMedicinesPlasticPackagingPage, messageKey)
  }

  def nonexportedMedicineWeight(messageKey: String): SummaryListRow = {
    createKgsRow(NonExportedHumanMedicinesPlasticPackagingWeightPage, messageKey)
  }

  def nonexportedRecycledYesNo(messageKey: String): SummaryListRow = {
    createYesNoRow(NonExportedRecycledPlasticPackagingPage, messageKey)
  }

  def nonexportedRecycledWeight(messageKey: String): SummaryListRow = {
    createKgsRow(NonExportedRecycledPlasticPackagingWeightPage, messageKey)
  }

  private def deductionsTotalNumeric: Long = {
    (ensureAnswer[ExportedPlasticPackagingWeightSummary]
      + ensureAnswer[NonExportedHumanMedicinesPlasticPackagingWeightSummary]
      + ensureAnswer[NonExportedRecycledPlasticPackagingWeightSummary])
  }

  def deductionsTotal: String = {
    deductionsTotalNumeric.asKgs
  }


  private def chargeableTotalNumeric = {
    // TODO if totalDeductions > totalPackaging is this the correct behaviour?!
    scala.math.max(0, (packagingTotalNumeric - deductionsTotalNumeric))
  }

  def chargeableTotal: String = {
    chargeableTotalNumeric.asKgs
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

  def creditsGuidanceUrl: String = appConfig.creditsGuidanceUrl
  def pptReference: String = request.pptReference
}
