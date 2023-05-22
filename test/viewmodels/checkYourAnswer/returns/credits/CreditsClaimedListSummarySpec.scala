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

package viewmodels.checkYourAnswer.returns.credits

import models.returns.credits.CreditSummaryRow
import models.{CreditBalance, TaxablePlastic}
import navigation.ReturnsJourneyNavigator
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar
import org.mockito.integrations.scalatest.ResetMocksAfterEachTest
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Text}
import viewmodels.checkAnswers.returns.credits.CreditsClaimedListSummary

class CreditsClaimedListSummarySpec extends PlaySpec with BeforeAndAfterEach with MockitoSugar with ResetMocksAfterEachTest {

  private val message    = mock[Messages]
  private val navigator  = mock[ReturnsJourneyNavigator]
  private val creditBalance = mock[CreditBalance]


  override protected def beforeEach(): Unit = {
    super.beforeEach()
    when(navigator.creditSummaryChange(any)) thenReturn "change-url"
    when(navigator.creditSummaryRemove(any)) thenReturn "remove-url"
  }

  "create a list of row" in {
    val credit = Map(
      "2023-01-01-2023-03-31" -> TaxablePlastic(200L, 200, 0.2),
      "2023-04-01-2024-03-31" -> TaxablePlastic(100L, 150, 0.3)
    )
    when(message.apply(any[String])).thenAnswer((s: String) => s)
    when(creditBalance.credit).thenReturn(credit)

    val rows = CreditsClaimedListSummary.createRows(creditBalance, navigator)(message)
    rows mustBe expectedResult
  }

  "return an empty list" in {
      when(creditBalance.credit).thenReturn(Map.empty)

      val rows = CreditsClaimedListSummary.createRows(creditBalance, navigator)(message)

      rows mustBe Seq.empty
  }

  private def expectedResult: Seq[CreditSummaryRow] =
    Seq(
      CreditSummaryRow(
        label = "2023-01-01-2023-03-31",
        value = "£200.00",
        actions = Seq(ActionItem("change-url", Text("site.change")), ActionItem("remove-url", Text("site.remove")))
      ),
      CreditSummaryRow(
        label = "2023-04-01-2024-03-31",
        value = "£150.00",
        actions = Seq(ActionItem("change-url", Text("site.change")), ActionItem("remove-url", Text("site.remove")))
      )
    )
}
