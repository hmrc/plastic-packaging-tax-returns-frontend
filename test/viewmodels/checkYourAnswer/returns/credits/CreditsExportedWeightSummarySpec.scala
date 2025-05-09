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

package viewmodels.checkYourAnswer.returns.credits

import models.Mode.CheckMode
import models.UserAnswers
import models.returns.CreditsAnswer
import org.mockito.ArgumentMatchers
import org.mockito.MockitoSugar.{mock, when}
import org.scalatestplus.play.PlaySpec
import pages.returns.credits.ExportedCreditsPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._
import viewmodels.PrintLong
import viewmodels.checkAnswers.returns.credits.CreditsExportedWeightSummary

class CreditsExportedWeightSummarySpec extends PlaySpec {

  private val messages = mock[Messages]

  "summary" should {
    "return a Summary row" when {
      "answer is yes" in {
        when(messages.apply(ArgumentMatchers.eq("confirmPackagingCredit.exported.weight"))).thenReturn(
          "exported weight"
        )
        when(messages.apply(ArgumentMatchers.eq("site.change"))).thenReturn("change")
        when(messages.apply(ArgumentMatchers.eq(20L.asKg))).thenReturn(20L.asKg)

        val userAnswer =
          UserAnswers("123").set(ExportedCreditsPage("year-key"), CreditsAnswer.answerWeightWith(20L)).get

        CreditsExportedWeightSummary("year-key").row(userAnswer)(messages) mustBe createExpectedWeightResult
      }
    }
  }

  private def createExpectedWeightResult: Option[SummaryListRow] = {
    Some(
      SummaryListRow(
        key = Key(Text("exported weight"), "govuk-!-width-one-half"),
        value = Value(Text(20L.asKg)),
        actions = Some(
          Actions(items =
            Seq(
              ActionItem(
                controllers.returns.credits.routes.ExportedCreditsWeightController.onPageLoad(
                  "year-key",
                  CheckMode
                ).url,
                Text("change"),
                Some("exported weight")
              )
            )
          )
        )
      )
    )
  }
}
