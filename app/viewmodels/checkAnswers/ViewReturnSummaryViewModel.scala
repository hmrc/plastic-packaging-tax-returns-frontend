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

final case class Field(key: String, value: String, bold: Boolean = false)

final case class Section(titleKey: String, fields : Seq[Field]) {
  def summaryList(implicit messages: Messages): SummaryList = SummaryListViewModel(fields.map{ row =>
    SummaryListRow(
      Key(Text(messages(row.key)), if (row.bold) "" else "govuk-!-font-weight-regular"),
      Value(Text(row.value)))
  })
}

object Section {
  def apply(name: String, lastBold: Boolean = true)(fields: String*): Section =
    Section(s"viewReturnSummary.$name.heading",
      fields.zipWithIndex.map{ case (value, i) =>
        val row = i + 1
        Field(s"viewReturnSummary.$name.field.$row", value, row==fields.length && lastBold)
      }
    )
}

final case class DetailsSection(liable: Section, exempt: Section, calculation: Section)

final case class ViewReturnSummaryViewModel(summarySection : Section, detailsSection: DetailsSection)

object ViewReturnSummaryViewModel {

  def asPounds(bigDecimal: BigDecimal): String = "£" + bigDecimal //there should be utils for this, and Kg

  def apply(submittedReturn: ReturnDisplayApi): ViewReturnSummaryViewModel =
    ViewReturnSummaryViewModel(
      Section("summary", lastBold = false)(
        asPounds(submittedReturn.returnDetails.taxDue),
        submittedReturn.processingDate,
        submittedReturn.chargeReferenceAsString,
      ),
      DetailsSection(
        Section("liable")(

        ),
        Section("exempt")(

        ),
        Section("calculation")(

        )
      )
    )

}
