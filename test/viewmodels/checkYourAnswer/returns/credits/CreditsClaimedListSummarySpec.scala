package viewmodels.checkYourAnswer.returns.credits

import akka.io.Udp.Message
import models.UserAnswers
import org.mockito.MockitoSugar.mock
import org.scalatestplus.play.PlaySpec
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import viewmodels.checkAnswers.returns.credits.CreditsClaimedListSummary

class CreditsClaimedListSummarySpec extends PlaySpec {

  private val message = mock[Messages]
  "create a list of row" in {

    val userAnswer = UserAnswers(
      "123",
      Json.parse(
        """{
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

    val rows = CreditsClaimedListSummary.row(userAnswer)(message)


  }

}
