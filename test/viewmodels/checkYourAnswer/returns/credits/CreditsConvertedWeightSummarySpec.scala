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

import models.Mode.CheckMode
import models.UserAnswers
import org.mockito.ArgumentMatchers
import org.mockito.MockitoSugar.{mock, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import pages.returns.credits.ConvertedCreditsWeightPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Key, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Actions, SummaryListRow, Value}
import viewmodels.PrintLong
import viewmodels.checkAnswers.returns.credits.CreditsConvertedWeightSummary

class CreditsConvertedWeightSummarySpec extends PlaySpec with BeforeAndAfterEach{

  private val messages = mock[Messages]

  "Row" should {
    "return a summary row" in {
      when(messages.apply(ArgumentMatchers.eq("confirmPackagingCredit.converted.weight"))).thenReturn("answer")
      when(messages.apply(ArgumentMatchers.eq("site.change"))).thenReturn("change")
      when(messages.apply(ArgumentMatchers.eq(50L.asKg))).thenReturn(50L.asKg)
      val userAnswer = UserAnswers("123").set(ConvertedCreditsWeightPage, 50L).get

      val result = CreditsConvertedWeightSummary.row(userAnswer)(messages)

      result mustBe createExpectedResult(50L)
    }
  }

  private def createExpectedResult(answerValue: Long): Option[SummaryListRow] = {
    Some(SummaryListRow(
      key = Key(Text("answer"), "govuk-!-width-one-half"),
      value = Value(Text(answerValue.asKg)),
      actions = Some(Actions(items = Seq(ActionItem(
        controllers.returns.credits.routes.ConvertedCreditsWeightController.onPageLoad(CheckMode).url,
        Text("change"),
        Some("answer")
      ))))
    ))
  }

}
