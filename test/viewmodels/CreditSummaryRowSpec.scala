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

package viewmodels

import models.returns.credits.CreditSummaryRow
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar
import org.mockito.integrations.scalatest.ResetMocksAfterEachTest
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, TableRow, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{Empty, HtmlContent}

class CreditSummaryRowSpec extends PlaySpec 
  with MockitoSugar with ResetMocksAfterEachTest with BeforeAndAfterEach {

  val createAction = mock[Seq[ActionItem] => Html]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    when(createAction(any)) thenReturn Html("action-html")
  }
  
  "it" should {
    "render a two column version with no actions" in {
      val creditSummaryRow = CreditSummaryRow("a-label", "a-value", actions = Seq())
      val result = creditSummaryRow.createContent(createAction)
      verifyZeroInteractions(createAction)
      result mustBe Seq(
        TableRow(Text("a-label"), Some("text")),
        TableRow(Text("a-value"), Some("text"), attributes = Map("style" -> "text-align:right;")),
        TableRow(Empty, None, "", None, None, Map("aria-hidden" -> "true"))
      )
    }

    "render a three column version with actions" in {
      val creditSummaryRow = CreditSummaryRow("a-label", "a-value", Seq(ActionItem()))
      val result = creditSummaryRow.createContent(createAction)
      verify(createAction).apply(Seq(ActionItem()))
      result mustBe Seq(
        TableRow(Text("a-label"), Some("text")),
        TableRow(Text("a-value"), Some("text"), attributes = Map("style" -> "text-align:right;")),
        TableRow(HtmlContent(Html("action-html")), Some("text"), attributes = Map("style" -> "text-align:right;"))
      )
    }
  }

}
