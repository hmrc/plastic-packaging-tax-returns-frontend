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

package viewmodels.checkYourAnswer.changeGroupLead

import controllers.changeGroupLead.routes
import models.Mode.CheckMode
import models.UserAnswers
import org.mockito.MockitoSugar.{mock, when}
import org.scalatestplus.play.PlaySpec
import pages.changeGroupLead.MainContactNamePage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{Actions, Text, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow}
import viewmodels.checkAnswers.changeGroupLead.MainContactNameSummary
import viewmodels.govuk.summarylist._

class MainContactNameSummarySpec extends PlaySpec {

  private val message = mock[Messages]

  "summary" should {
    "return a row" in {

      when(message.apply("newGroupLeadCheckYourAnswers.contact.name.key")).thenReturn("[KEY]")
      when(message.apply("site.change")).thenReturn("[CHANGE]")
      when(message.apply("Pan")).thenReturn("Pan")

      val userAnswers = UserAnswers("test").setOrFail(MainContactNamePage, "Pan", false)

      MainContactNameSummary.row(userAnswers)(message) mustBe Some(SummaryListRow(
        key = Key(Text("[KEY]")),
        value = Value(Text("Pan")),
        actions = Some(Actions("", Seq(
          ActionItemViewModel(Text("[CHANGE]"), routes.MainContactNameController.onPageLoad(CheckMode).url)
          .withVisuallyHiddenText("[KEY]")
      )))
      ))

    }
    "not return a row" in  {
      val userAnswers = UserAnswers("test")

      MainContactNameSummary.row(userAnswers)(message) mustBe None
    }
  }
}