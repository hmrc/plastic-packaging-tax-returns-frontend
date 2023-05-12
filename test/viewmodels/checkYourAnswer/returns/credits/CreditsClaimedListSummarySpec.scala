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

import models.UserAnswers
import navigation.ReturnsJourneyNavigator
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar
import org.mockito.integrations.scalatest.ResetMocksAfterEachTest
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.govukfrontend.views.Aliases._
import viewmodels.checkAnswers.returns.credits.CreditsClaimedListSummary

class CreditsClaimedListSummarySpec extends PlaySpec 
  with BeforeAndAfterEach with MockitoSugar with ResetMocksAfterEachTest {

  private val message = mock[Messages]
  private val navigator = mock[ReturnsJourneyNavigator]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    when(navigator.creditSummaryChange(any)) thenReturn "change-url"
  }
  
  "create a list of row" in {

    val userAnswer = UserAnswers("123", Json.parse("""{
          "credit" : {
          |  "2023-01-01-2023-03-31" : {
          |     "endDate" : "2023-03-31",
          |     "exportedCredits" : {
          |       "yesNo" : true,
          |       "weight" : 34
          |     },
          |     "convertedCredits" : {
          |       "yesNo" : true,
          |       "weight" : 545
          |     }
          |   },
          |   "2023-04-01-2024-03-31" : {
          |     "endDate" : "2024-03-31",
          |       "exportedCredits" : {
          |         "yesNo" : true,
          |         "weight" : 30
          |       },
          |       "convertedCredits" : {
          |         "yesNo" : true,
          |         "weight" : 545
          |        }
          |      }
          |   }
          |}""".stripMargin).as[JsObject])

    when(message.apply(any[String])).thenAnswer((s: String) => s)

    val rows = CreditsClaimedListSummary.createRows(userAnswer, navigator)(message)

    rows mustBe Seq(
      SummaryListRow(
        key = Key(Text("2023-01-01-2023-03-31")),
        value = Value(Text("0")),
        actions = Some(Actions(items = Seq(ActionItem("change-url", Text("site.change")), ActionItem("/remove", Text("site.remove")))))
      ),
      SummaryListRow(
        key = Key(Text("2023-04-01-2024-03-31")),
        value = Value(Text("0")),
        actions = Some(Actions(items = Seq(ActionItem("change-url", Text("site.change")), ActionItem("/remove", Text("site.remove")))))
      )
    )
  }

}
