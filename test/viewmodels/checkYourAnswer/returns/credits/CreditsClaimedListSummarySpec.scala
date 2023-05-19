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
import models.{CreditBalance, TaxablePlastic, UserAnswers}
import navigation.ReturnsJourneyNavigator
import org.mockito.ArgumentMatchersSugar.{any, eqTo}
import org.mockito.MockitoSugar
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Text}
import viewmodels.checkAnswers.returns.credits.CreditsClaimedListSummary

class CreditsClaimedListSummarySpec extends PlaySpec with BeforeAndAfterEach with MockitoSugar {

  private val message    = mock[Messages]
  private val navigator  = mock[ReturnsJourneyNavigator]
  private val creditBalance = mock[CreditBalance]
  private val key1 = "2022-04-01-2023-03-31"
  private val key2 = "2023-04-01-2024-03-31"
  private val key3 =  "2024-04-01-2025-03-31"
  val credit = Map(
    key3 -> TaxablePlastic(300L, 350, 0.4),
    key2 -> TaxablePlastic(100L, 150, 0.3),
    key1 -> TaxablePlastic(200L, 200, 0.2),
  )

  val userAnswer = UserAnswers(
    "123",
    Json.parse(
    s"""{
      |  "credit" : {
      |    "$key3" : {
      |      "fromDate": "2024-04-01",
      |      "endDate" : "2025-03-31",
      |      "exportedCredits" : {
      |        "yesNo" : true,
      |        "weight" : 34
      |      },
      |      "convertedCredits" : {
      |        "yesNo" : true,
      |        "weight" : 545
      |      }
      |    },
      |   "$key2" : {
      |     "fromDate": "2023-04-01",
      |     "endDate" : "2024-03-31",
      |       "exportedCredits" : {
      |         "yesNo" : true,
      |         "weight" : 30
      |       },
      |       "convertedCredits" : {
      |         "yesNo" : true,
      |         "weight" : 545
      |       }
      |    },
      |   "$key1" : {
      |     "fromDate": "2022-04-01",
      |     "endDate" : "2023-03-31",
      |     "exportedCredits" : {
      |       "yesNo" : true,
      |       "weight" : 34
      |     },
      |     "convertedCredits" : {
      |       "yesNo" : true,
      |       "weight" : 545
      |     }
      |   }
      | }
      |}""".stripMargin).as[JsObject])


  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(navigator, message, creditBalance)

    when(navigator.creditSummaryChange(any)) thenReturn "change-url"
    when(navigator.creditSummaryRemove(any)) thenReturn "remove-url"
  }

  "create a chronological ordered list of row" in {
    when(message.apply(any[String])).thenAnswer((s: String) =>  s)
    when(message.apply(eqTo("return.quarter"), any[Seq[String]]))
      .thenReturn(
        "1 April 2022 to 31 March 2023",
        "1 April 2023 to 31 March 2024",
        "1 April 2024 to 31 March 2025")


    when(creditBalance.credit).thenReturn(credit)

    val rows = CreditsClaimedListSummary.createRows(userAnswer, creditBalance, navigator)(message)

    rows mustBe expectedResult
  }

  "return an empty list" in {
      when(creditBalance.credit).thenReturn(Map.empty)

      val rows = CreditsClaimedListSummary.createRows(userAnswer, creditBalance, navigator)(message)

      rows mustBe Seq.empty
  }

  "throw" when {
    "fromDate is invalid" in {
      when(creditBalance.credit).thenReturn(credit)

      intercept[IllegalStateException] {
        CreditsClaimedListSummary.createRows(userAnswerWithInvalidFromDate, creditBalance, navigator)(message)
      }
    }

    "endDate is invalid" in {
      when(creditBalance.credit).thenReturn(credit)

      intercept[IllegalStateException] {
        CreditsClaimedListSummary.createRows(userAnswerWithInvalidendDate, creditBalance, navigator)(message)
      }
    }

  }

  private def expectedResult: Seq[CreditSummaryRow] =
    Seq(
      CreditSummaryRow(
        label = "1 April 2022 to 31 March 2023",
        value = "£200.00",
        actions = Seq(ActionItem("change-url", Text("site.change")), ActionItem("remove-url", Text("site.remove")))
      ),
      CreditSummaryRow(
        label = "1 April 2023 to 31 March 2024",
        value = "£150.00",
        actions = Seq(ActionItem("change-url", Text("site.change")), ActionItem("remove-url", Text("site.remove")))
      ),
      CreditSummaryRow(
        label = "1 April 2024 to 31 March 2025",
        value = "£350.00",
        actions = Seq(ActionItem("change-url", Text("site.change")), ActionItem("remove-url", Text("site.remove")))
      )
    )

  def userAnswerWithInvalidFromDate = {
    UserAnswers("123", Json.parse(
      """{
        | "credit": {
        |   "key1": {
        |     "fromDate": "2022-3-2",
        |     "endDate": "2023-04-31"
        |    }
        | }
        |}""".stripMargin).as[JsObject])
  }

  def userAnswerWithInvalidendDate = {
    UserAnswers("123", Json.parse(
      """{
        | "credit": {
        |   "key1": {
        |     "endDate": "2022-3-2",
        |     "fromDate": "2023-04-31"
        |    }
        | }
        |}""".stripMargin).as[JsObject])
  }
}
