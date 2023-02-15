package models

import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsArray, JsObject, JsString, Json}

import scala.util.Try

class EisFailureSpec extends PlaySpec with BeforeAndAfterEach {
  
  override protected def beforeEach(): Unit = { 
    super.beforeEach()
  }
  
  "it" should {
    
    "recognise a de-registered account" when {
      
      "account is apparently de-registered" in {
        val actualNotFoundResponseFromIf = JsObject(Seq(
          "failures" -> JsArray(Seq(
            JsObject(Seq(
              "code" -> JsString("NO_DATA_FOUND"),
              "reason" -> JsString("The remote endpoint has indicated that the requested resource could not be found.")
            ))
          ))
        ))
        actualNotFoundResponseFromIf.asOpt[EisFailure].value.isDeregistered mustBe true
      }
      
      "something else, not de-registration" in {
        val actualNotFoundResponseFromIf = JsObject(Seq(
          "failures" -> JsArray(Seq(
            JsObject(Seq(
              "code" -> JsString("SUMMIT_ELSE"),
              "reason" -> JsString("Bang")
            ))
          ))
        ))
        actualNotFoundResponseFromIf.asOpt[EisFailure].value.isDeregistered mustBe false
      }
      
    }
    
    "recognise a downstream outage" when {
      
      "it's bad gateway" in {
        // failures:
        //                      - code: BAD_GATEWAY
        //                        reason: >-
        //                          Dependent systems are currently not responding.
        val takenFromEisApiDoc = JsObject(Seq(
          "failures" -> JsArray(Seq(
            JsObject(Seq(
              "code" -> JsString("BAD_GATEWAY"),
              "reason" -> JsString("Dependent systems are currently not responding.")
            ))
          ))
        ))

        takenFromEisApiDoc.asOpt[EisFailure].value.isDependentSystemsNotResponding mustBe true
      }
      
      "it's service unavailable" in {
        val takenFromEisApiDoc = JsObject(Seq(
          "failures" -> JsArray(Seq(
            JsObject(Seq(
              "code" -> JsString("SERVICE_UNAVAILABLE"),
              "reason" -> JsString("Dependent systems are currently not responding.")
            ))
          ))
        ))

        takenFromEisApiDoc.asOpt[EisFailure].value.isDependentSystemsNotResponding mustBe true
      }
      
      "it's something else" in {
        val takenFromEisApiDoc = JsObject(Seq(
          "failures" -> JsArray(Seq(
            JsObject(Seq(
              "code" -> JsString("SUMMIT_ELSE"),
              "reason" -> JsString("Bang")
            ))
          ))
        ))

        takenFromEisApiDoc.asOpt[EisFailure].value.isDependentSystemsNotResponding mustBe false
      }
      
    }
  }
}
