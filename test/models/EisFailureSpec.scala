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

package models

import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsArray, JsObject, JsString}

class EisFailureSpec extends PlaySpec {

  private def cookSingleFailureResponse(code: String, description: String) =
    JsObject(
      Seq(
        "failures" -> JsArray(
          Seq(
            JsObject(
              Seq(
                "code"   -> JsString(code),
                "reason" -> JsString(description)
              )
            )
          )
        )
      )
    )

  "it" should {

    "recognise a de-registered account" when {

      "account is apparently de-registered" in {
        val actualNotFoundResponseFromIf = cookSingleFailureResponse(
          "NO_DATA_FOUND",
          "The remote endpoint has indicated that the requested resource could not be found."
        )
        actualNotFoundResponseFromIf.asOpt[EisFailure].value.isDeregistered mustBe true
      }

      "something else, not de-registration" in {
        cookSingleFailureResponse("SUMMIT_ELSE", "Bang").asOpt[EisFailure].value.isDeregistered mustBe false
      }

    }

    "recognise a downstream outage" when {

      "it's bad gateway" in {
        val takenFromEisApiDoc =
          cookSingleFailureResponse("BAD_GATEWAY", "Dependent systems are currently not responding.")
        takenFromEisApiDoc.asOpt[EisFailure].value.isDependentSystemsNotResponding mustBe true
      }

      "it's service unavailable" in {
        val takenFromEisApiDoc =
          cookSingleFailureResponse("SERVICE_UNAVAILABLE", "Dependent systems are currently not responding.")
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
