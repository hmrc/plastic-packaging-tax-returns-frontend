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

  def createContent(createActionsContent: Seq[ActionItem] => Html): Seq[TableRow] = {
    Seq(
      TableRow(content = Text(label), format = Some("text")),
      TableRow(content = Text(value), format = Some("text"), attributes = Map("style" -> "text-align:right;")),
    ) ++ createActionsCell(createActionsContent)
  }

  private def createActionsCell(createActionsContent: Seq[Aliases.ActionItem] => Html) = {
    if (actions.isEmpty)
      Seq(TableRow(attributes=Map("aria-hidden" -> "true")))
    else
      Seq(TableRow(content = HtmlContent(createActionsContent(actions)), format = Some("text"), 
        classes = "govuk-table__cell--numeric"))
  }
}
