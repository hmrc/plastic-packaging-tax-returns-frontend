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

import models.UserAnswers
import models.returns.TaxReturnObligation
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.{InputWidth, PrintLong}
import viewmodels.checkAnswers.SummaryViewModel
import viewmodels.checkAnswers.returns.{ImportedPlasticPackagingSummary, ImportedPlasticPackagingWeightSummary,
  ManufacturedPlasticPackagingSummary, ManufacturedPlasticPackagingWeightSummary}

import scala.reflect.ClassTag

case class TaxReturnViewModel (
  private val pptReference: String,
  private val obligation: TaxReturnObligation,
  private val userAnswers: UserAnswers
) (implicit messages: Messages) {

  private def stylize(row: SummaryListRow) = {
    val classes = s"govuk-!-font-weight-regular ${InputWidth.ThreeQuarters}"
    row.copy(key = row.key.copy(
      classes = classes
    ))
  }

  private def instantiate[PageType <: SummaryViewModel](messageKey: String, tag: ClassTag[PageType]) = {
    tag.runtimeClass.getConstructor(classOf[String])
      .newInstance(messageKey).asInstanceOf[PageType]
  }

  private def createSummaryRow[PageType <: SummaryViewModel](messageKey: String)(implicit tag: ClassTag[PageType]) = {
    stylize(instantiate(messageKey, tag)
      .row(userAnswers)
      .getOrElse {
        val errorMessage = s"The field for '${messages(messageKey)}' is missing from user-answers"
        throw new IllegalStateException(errorMessage)
      }
    )
  }

  private def ensureAnswer[PageType <: SummaryViewModel: ClassTag]: Long = {
    val tag = implicitly[ClassTag[PageType]]
    instantiate("", tag)
      .answer(userAnswers)
      .getOrElse {
        val className = implicitly[ClassTag[PageType]].runtimeClass.getSimpleName
        val errorMessage = s"The field for '$className' is missing from user-answers"
        throw new IllegalStateException(errorMessage)
      }
  }

  def manufacturedYesNo(messageKey: String): SummaryListRow = {
    createSummaryRow[ManufacturedPlasticPackagingSummary](messageKey)
  }

  def manufacturedWeight(messageKey: String): SummaryListRow = {
    createSummaryRow[ManufacturedPlasticPackagingWeightSummary](messageKey)
  }

  def importedYesNo(messageKey: String): SummaryListRow = {
    createSummaryRow[ImportedPlasticPackagingSummary](messageKey)
  }

  def importedWeight(messageKey: String): SummaryListRow = {
    createSummaryRow[ImportedPlasticPackagingWeightSummary](messageKey)
  }

  def packagingTotal: String = {
    val total = ensureAnswer[ManufacturedPlasticPackagingWeightSummary]
      + ensureAnswer[ImportedPlasticPackagingWeightSummary]
    total.asKgs
  }


}
