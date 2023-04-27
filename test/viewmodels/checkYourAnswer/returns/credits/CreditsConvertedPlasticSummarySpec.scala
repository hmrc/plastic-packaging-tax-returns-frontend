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
import models.returns.CreditsAnswer
import org.mockito.ArgumentMatchers
import org.mockito.MockitoSugar.{mock, reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import pages.returns.credits.OldConvertedCreditsPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Key, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Actions, SummaryListRow, Value}
import viewmodels.checkAnswers.returns.credits.CreditsConvertedPlasticSummary

class CreditsConvertedPlasticSummarySpec extends PlaySpec with BeforeAndAfterEach{

  private val messages = mock[Messages]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(messages)
    when(messages.apply(ArgumentMatchers.eq("confirmPackagingCredit.converted.answer"))).thenReturn("answer")
    when(messages.apply(ArgumentMatchers.eq("site.change"))).thenReturn("change")
  }



  "Row" should {
    "return a summary row" when {
      "answer is yes" in {
        when(messages.apply(ArgumentMatchers.eq("site.yes"))).thenReturn("yes")
        val userAnswer = UserAnswers("123").set(OldConvertedCreditsPage, CreditsAnswer(true, None)).get

        val result = CreditsConvertedPlasticSummary.row(userAnswer)(messages)

        result mustBe createExpectedResult("yes")
      }

      "answer is no" in {
        when(messages.apply(ArgumentMatchers.eq("site.no"))).thenReturn("no")
        val userAnswer = UserAnswers("123").set(OldConvertedCreditsPage, CreditsAnswer(false, None)).get

        val result = CreditsConvertedPlasticSummary.row(userAnswer)(messages)

        result mustBe createExpectedResult("no")
      }
    }
  }

  private def createExpectedResult(answerValue: String): Option[SummaryListRow] = {
    Some(SummaryListRow(
      key = Key(Text("answer"), "govuk-!-width-one-half"),
      value = Value(Text(answerValue)),
      actions = Some(Actions(items = Seq(ActionItem(
        controllers.returns.credits.routes.ConvertedCreditsController.onPageLoad(CheckMode).url,
        Text("change"),
        Some("answer")
      ))))
    ))
  }
}
