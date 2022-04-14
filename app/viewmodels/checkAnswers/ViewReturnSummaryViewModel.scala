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

package viewmodels.checkAnswers

import models.returns.ReturnDisplayApi
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{Key, SummaryListRow, Text, Value}
import viewmodels.govuk.summarylist._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.PrintBigDecimal

final case class Field(key: String, value: String, bold: Boolean = false, big: Boolean = false){
  def classes: String =
    Seq(
      if (big) "govuk-body-l" else "govuk-body-m",
      if (bold) "govuk-!-font-weight-bold" else "govuk-!-font-weight-regular",
    ).mkString(" ")
}

final case class Section(titleKey: String, fields : Seq[Field]) {
  def summaryList(implicit messages: Messages): SummaryList = SummaryListViewModel(fields.map{ row =>
    SummaryListRow(
      Key(Text(messages(row.key)), row.classes),
      Value(Text(row.value), row.classes))
  })
}

object Section {
  def apply(name: String, lastBold: Boolean = true, lastBig: Boolean = false)(fields: (String, String)*): Section =
    Section(s"viewReturnSummary.$name.heading",
      fields.zipWithIndex.map{ case ((row, value), i) =>
        val isLast = i+1==fields.length
        Field(s"viewReturnSummary.$name.field.$row", value, isLast && lastBold, isLast && lastBig)
      }
    )
}

final case class DetailsSection(liable: Section, exempt: Section, calculation: Section, taxCredit: Section)

final case class ViewReturnSummaryViewModel(summarySection : Section, detailsSection: DetailsSection)

object ViewReturnSummaryViewModel {

  def apply(submittedReturn: ReturnDisplayApi): ViewReturnSummaryViewModel =
    ViewReturnSummaryViewModel(
      Section("summary", lastBold = false)(
        "liability" -> submittedReturn.returnDetails.taxDue.asPounds,
        "processed" -> submittedReturn.processingDate,
        "reference" -> submittedReturn.chargeReferenceAsString,
      ),
      DetailsSection(
        Section("liable")(
          "manufactured" -> submittedReturn.returnDetails.manufacturedWeight.asKgs,
          "imported" -> submittedReturn.returnDetails.importedWeight.asKgs,
          "total" -> submittedReturn.returnDetails.totalWeight.asKgs
        ),
        Section("exempt")(
          "exported" -> submittedReturn.returnDetails.directExports.asKgs,
          "medicine" -> submittedReturn.returnDetails.humanMedicines.asKgs,
          "recycled" -> submittedReturn.returnDetails.recycledPlastic.asKgs,
          "total" -> submittedReturn.returnDetails.totalNotLiable.asKgs,
        ),
        Section("calculation", lastBig = true)(
          "total" -> submittedReturn.returnDetails.totalWeight.asKgs,
          "exempt" -> submittedReturn.returnDetails.totalNotLiable.asKgs,
          "liable" -> submittedReturn.returnDetails.liableWeight.asKgs,
          "tax" -> submittedReturn.returnDetails.taxDue.asPounds,
        ),
        Section("credits", lastBold = false)(
          "credit" -> submittedReturn.returnDetails.creditForPeriod.asPounds,
          "debit" -> submittedReturn.returnDetails.debitForPeriod.asPounds,
        )
      )
    )

}
