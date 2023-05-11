package viewmodels.checkYourAnswer.returns.credits

import models.UserAnswers
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar.{mock, when}
import org.scalatestplus.play.PlaySpec
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Actions, Key, SummaryListRow, Text, Value}
import viewmodels.checkAnswers.returns.credits.CreditsClaimedListSummary

class CreditsClaimedListSummarySpec extends PlaySpec {

  private val message = mock[Messages]
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

    val rows = CreditsClaimedListSummary.row(userAnswer)(message)



    rows mustBe Seq(
      SummaryListRow(
        key = Key(Text("2023-01-01-2023-03-31")),
        value = Value(Text("0")),
        actions = Some(Actions(items = Seq(ActionItem("/change", Text("site.change")), ActionItem("/remove", Text("site.remove")))))
      ),
      SummaryListRow(
        key = Key(Text("2023-04-01-2024-03-31")),
        value = Value(Text("0")),
        actions = Some(Actions(items = Seq(ActionItem("/change", Text("site.change")), ActionItem("/remove", Text("site.remove")))))
      )
    )
  }
}
