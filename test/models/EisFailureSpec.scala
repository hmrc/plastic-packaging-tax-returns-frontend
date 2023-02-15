package models

import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsArray, JsObject, JsString, Json}

import scala.util.Try

class EisFailureSpec extends PlaySpec with BeforeAndAfterEach {
  
  override protected def beforeEach(): Unit = { 
    super.beforeEach()
  }
  
  private def cookSingleFailureResponse(code: String, description: String) =
    JsObject(Seq(
      "failures" -> JsArray(Seq(
        JsObject(Seq(
          "code" -> JsString(code),
          "reason" -> JsString(description)
        ))
      ))
    ))
    
  "it" should {
    
    "recognise a de-registered account" when {
      
      "account is apparently de-registered" in {
        val actualNotFoundResponseFromIf = cookSingleFailureResponse("NO_DATA_FOUND", 
          "The remote endpoint has indicated that the requested resource could not be found.") 
        actualNotFoundResponseFromIf.asOpt[EisFailure].value.isDeregistered mustBe true
      }
      
      "something else, not de-registration" in {
        cookSingleFailureResponse("SUMMIT_ELSE", "Bang").asOpt[EisFailure].value.isDeregistered mustBe false
      }
      
    }
    
    "recognise a downstream outage" when {
      
      "it's bad gateway" in {
        val takenFromEisApiDoc = cookSingleFailureResponse("BAD_GATEWAY", 
          "Dependent systems are currently not responding.")
        takenFromEisApiDoc.asOpt[EisFailure].value.isDependentSystemsNotResponding mustBe true
      }
      
      "it's service unavailable" in {
        val takenFromEisApiDoc = cookSingleFailureResponse("SERVICE_UNAVAILABLE", 
          "Dependent systems are currently not responding.")
        takenFromEisApiDoc.asOpt[EisFailure].value.isDependentSystemsNotResponding mustBe true
      }
      
      "it's something else" in {
        cookSingleFailureResponse("SUMMIT_ELSE", "Bang")
          .asOpt[EisFailure]
          .value
          .isDependentSystemsNotResponding mustBe false
      }
      
    }
  }
}
