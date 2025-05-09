/*
 * Copyright 2025 HM Revenue & Customs
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
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels._
import viewmodels.govuk.summarylist._
import views.ViewUtils

import java.time.LocalDate
import java.time.format.DateTimeFormatter

final case class Field(key: String, value: String, bold: Boolean = false, big: Boolean = false) {
  def classes: String =
    Seq(
      "govuk-!-width-one-half",
      if (big) "govuk-body-l" else "govuk-body-m",
      if (bold) "govuk-!-font-weight-bold" else "govuk-!-font-weight-regular"
    ).mkString(" ")
}

final case class Section(titleKey: String, fields: Seq[Field]) {
  def summaryList(implicit messages: Messages): SummaryList = SummaryListViewModel(fields.map { row =>
    SummaryListRow(Key(Text(messages(row.key)), row.classes), Value(Text(row.value), row.classes))
  })
}

object Section {
  def apply(name: String, lastBold: Boolean = true, lastBig: Boolean = false)(fields: (String, String)*): Section =
    Section(
      s"viewReturnSummary.$name.heading",
      fields.zipWithIndex.map { case ((row, value), i) =>
        val isLast = i + 1 == fields.length
        Field(s"viewReturnSummary.$name.field.$row", value, isLast && lastBold, isLast && lastBig)
      }
    )
}

final case class DetailsSection(credit: Section, liable: Section, exempt: Section, calculation: Section)

final case class ViewReturnSummaryViewModel(summarySection: Section, detailsSection: DetailsSection)

object ViewReturnSummaryViewModel {

  private def toLocalDate(date: String) = LocalDate.parse(date, DateTimeFormatter.ISO_DATE_TIME)

  def apply(returnDisplay: ReturnDisplayApi)(implicit messages: Messages): ViewReturnSummaryViewModel =
    ViewReturnSummaryViewModel(
      Section("summary", lastBold = false)(
        "processed" -> returnDisplay.chargeDetails.fold("n/a")(cd =>
          ViewUtils.displayLocalDate(toLocalDate(cd.receiptDate))
        ),
        "reference" -> returnDisplay.chargeReferenceAsString
      ),
      DetailsSection(
        Section("credit", lastBold = false)(
          "total" -> returnDisplay.returnDetails.creditForPeriod.asPounds
        ),
        Section("liable")(
          "manufactured" -> returnDisplay.returnDetails.manufacturedWeight.asKg,
          "imported"     -> returnDisplay.returnDetails.importedWeight.asKg,
          "total"        -> returnDisplay.returnDetails.liableWeight.asKg
        ),
        Section("exempt")(
          "exported" -> returnDisplay.returnDetails.directExports.asKg,
          "medicine" -> returnDisplay.returnDetails.humanMedicines.asKg,
          "recycled" -> returnDisplay.returnDetails.recycledPlastic.asKg,
          "total"    -> returnDisplay.returnDetails.totalNotLiable.asKg
        ),
        Section("calculation", lastBig = false)(
          "liable" -> returnDisplay.returnDetails.liableWeight.asKg,
          "exempt" -> returnDisplay.returnDetails.totalNotLiable.asKg,
          "total"  -> returnDisplay.returnDetails.totalWeight.asKg,
          "tax"    -> returnDisplay.returnDetails.taxDue.asPounds
        )
      )
    )
}
