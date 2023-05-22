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

package views

import models.returns.{ReturnDisplayApi, TaxReturnObligation}
import play.api.data.Form
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{PrefixOrSuffix, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.input.Input
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.all.FluentInput

import java.time.LocalDate

object ViewUtils {

  def title(form: Form[_], title: String, section: Option[String] = None)(implicit
    messages: Messages
  ): String =
    titleNoForm(title = s"${errorPrefix(form)} ${messages(title)}", section = section)

  def titleNoForm(title: String, section: Option[String] = None)(implicit
    messages: Messages
  ): String =
    s"${messages(title)} - ${section.fold("")(messages(_) + " - ")}${messages("service.name")} - ${messages("site.govuk")}"

  def errorPrefix(form: Form[_])(implicit messages: Messages): String =
    if (form.hasErrors || form.hasGlobalErrors) messages("error.browser.title.prefix") else ""

  def getMonthName(monthNumber: Int)(implicit messages: Messages): String =
    messages(s"month.$monthNumber")

  def displayReturnQuarter(from: LocalDate, to: LocalDate)(implicit messages: Messages): String = {
    // TODO move to Obligation
    messages("return.quarter",
      getMonthName(from.getMonthValue),
      getMonthName(to.getMonthValue),
      to.getYear.toString
    )
  }

  def displayDateRangeTo(from: LocalDate, to: LocalDate)(implicit messages: Messages): String = {
    messages("return.quarter",
      displayLocalDate(from),
      s"${to.getDayOfMonth} ${getMonthName(to.getMonthValue)}",
      to.getYear.toString
    )
  }

  def displayDateRangeAnd(from: LocalDate, to: LocalDate)(implicit messages: Messages): String = {
    messages("return.quarter.and",
      displayLocalDate(from),
      s"${to.getDayOfMonth} ${getMonthName(to.getMonthValue)}",
      to.getYear.toString
    )
  }

  def displayReturnQuarter(obligation: TaxReturnObligation)(implicit messages: Messages): String =
    displayReturnQuarter(obligation.fromDate, obligation.toDate)

  def displayReturnQuarter(returnDisplay: ReturnDisplayApi)(implicit messages: Messages): String = {
    val charge = returnDisplay.chargeDetails.getOrElse(throw new IllegalStateException("A return must have a charge details sub-container"))
    displayReturnQuarter(LocalDate.parse(charge.periodFrom), LocalDate.parse(charge.periodTo))
  }

  def displayLocalDate(date: LocalDate)(implicit messages: Messages): String =
    s"${date.getDayOfMonth} ${getMonthName(date.getMonthValue)} ${date.getYear}"

  implicit class FluentInputSuffixes(val input: Input) extends AnyVal {
    def asKg(): Input =
      input
        .withSuffix(PrefixOrSuffix(content = Text("kg")))
        .withPattern("[0-9]*")
  }

  implicit class RichSummaryListRow(val that: SummaryListRow) extends AnyVal {
    def bold: SummaryListRow = {
      def boldClass(c: String): String = c + " govuk-!-font-weight-bold"
      that.copy(
        key = that.key.copy(classes = boldClass(that.key.classes)),
        value = that.value.copy(classes = boldClass(that.value.classes)),
        classes = boldClass(that.classes)
      )
    }
  }
}
