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

package models.returns.credits

import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases
import uk.gov.hmrc.govukfrontend.views.Aliases.ActionItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.TableRow

case class CreditSummaryRow(
  label: String,
  value: String,
  actions: Seq[ActionItem] = Seq.empty
) {

  def createContent(createActionsContent: Seq[ActionItem] => Html, isActionColumnHidden: Boolean = false): Seq[TableRow] = {
    val alignRight = "" // if (isActionColumnHidden) "govuk-table__cell--numeric" else ""  
    Seq(
      TableRow(content = Text(label), format = Some("text")),
      TableRow(content = Text(value), format = Some("text"), classes = alignRight), // classes = "govuk-table__cell--numeric govuk-summary-list__value")
    ) ++ createActionsCell(createActionsContent, isActionColumnHidden)
  }

  // ++ (if (row.actions.isEmpty) Seq() else Seq())
  //  TableRow(content = HtmlContent(actionContent(row.actions)), format = Some("text"))

  private def createActionsCell(f: Seq[Aliases.ActionItem] => Html, isActionColumnHidden: Boolean) = {
    if (isActionColumnHidden)
      Seq(TableRow(content = HtmlContent(f(actions)), format = Some("text"), classes = "govuk-table__cell--numeric govuk-summary-list__value"))
    else
      Seq()
  }
}
