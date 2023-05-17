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

import models.returns.CreditsAnswer
import models.returns.credits.CreditSummaryRow
import models.{CreditBalance, UserAnswers}
import navigation.ReturnsJourneyNavigator
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar
import org.mockito.integrations.scalatest.ResetMocksAfterEachTest
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
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
    when(message.apply(any[String])).thenAnswer((s: String) => s)
    val rows = CreditsClaimedListSummary.createRows(creditBalance, navigator)(message)
    rows mustBe expectedResult(result)
  }

  "return an empty list" when {
    "no credit found" in {
      val userAnswer = UserAnswers("123", JsObject.empty)

      val rows = CreditsClaimedListSummary.createRows(creditBalance, navigator)(message)

      rows mustBe Seq.empty
    }

    "credit is empty" in {
      val userAnswer = UserAnswers("123", Json.parse("""{
          "credit" : {}
          |}""".stripMargin).as[JsObject])

      val rows = CreditsClaimedListSummary.createRows(creditBalance, navigator)(message)

      rows mustBe Seq.empty
    }
  }

  private def createJsonUserAnswer(exported: CreditsAnswer, converted: CreditsAnswer) = UserAnswers("123",
    Json.parse(s"""{
          "credit" : {
                  |  "2023-01-01-2023-03-31" : {
                  |     "endDate" : "2023-03-31",
                  |     "exportedCredits" : {
                  |       "yesNo" : ${exported.yesNo},
                  |       "weight" : ${exported.weightValue}
                  |     },
                  |     "convertedCredits" : {
                  |       "yesNo" : ${converted.yesNo},
                  |       "weight" : ${converted.weightValue}
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
  private def expectedResult(value: String): Seq[CreditSummaryRow] =
    Seq(
      CreditSummaryRow(
        label = "2023-01-01-2023-03-31",
        value = value,
        actions = Seq(ActionItem("change-url", Text("site.change")), ActionItem("remove-url", Text("site.remove")))
      ),
      CreditSummaryRow(
        label = "2023-04-01-2024-03-31",
        value = "575kg",
        actions = Seq(ActionItem("change-url", Text("site.change")), ActionItem("remove-url", Text("site.remove")))
      )
    )

}
