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

package pages.returns

import models.UserAnswers
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsObject, Json}

class ConfirmPlasticPackagingTotalPageSpec extends PlaySpec {

  "cleanUp" should {

    "reset UserAnswer amount is less or equal to 0" in {
      val userAnswer = UserAnswers("123", userAnswerDataObj)


      val expected = userAnswer
        .set(DirectlyExportedComponentsPage, false).get
        .set(ExportedPlasticPackagingWeightPage, 0L, cleanup = false).get
        .set(PlasticExportedByAnotherBusinessPage, false).get
        .set(AnotherBusinessExportWeightPage, 0L, cleanup = false).get
        .set(NonExportedHumanMedicinesPlasticPackagingPage, false, cleanup = false).get
        .set(NonExportedHumanMedicinesPlasticPackagingWeightPage, 0L, cleanup = false).get
        .set(NonExportedRecycledPlasticPackagingPage, false, cleanup = false).get
        .set(NonExportedRecycledPlasticPackagingWeightPage, 0L, cleanup = false).get
        .remove(ConfirmPlasticPackagingTotalPage)

      ConfirmPlasticPackagingTotalPage.cleanup(Some(0L), userAnswer) mustEqual expected
    }

    "return the userAnswer when amount is greater than 0" in {
      val userAnswer = UserAnswers("123", userAnswerDataObj)

      ConfirmPlasticPackagingTotalPage.cleanup(Some(20L), userAnswer) mustEqual userAnswer.remove(ConfirmPlasticPackagingTotalPage)
    }

    "return the userAnswer when there is no amount" in {
      val userAnswer = UserAnswers("123", userAnswerDataObj)

      ConfirmPlasticPackagingTotalPage.cleanup(None, userAnswer) mustEqual userAnswer.remove(ConfirmPlasticPackagingTotalPage)
    }

  }

  private def userAnswerDataObj = Json.parse(
    """
      |{
      | "manufacturedPlasticPackaging":true,
      | "manufacturedPlasticPackagingWeight":20,
      | "importedPlasticPackaging":true,
      | "importedPlasticPackagingWeight":0,
      | "confirmPlasticPackagingTotalPage": 20,
      | "directlyExportedComponents":true,
      | "exportedPlasticPackagingWeight":50,
      | "plasticExportedByAnotherBusiness": true,
      | "anotherBusinessExportWeight": 5,
      | "nonExportedHumanMedicinesPlasticPackaging":true,
      | "nonExportedHumanMedicinesPlasticPackagingWeight":20,
      | "nonExportRecycledPlasticPackaging":true,
      | "nonExportRecycledPlasticPackagingWeight":30}
      |""".stripMargin).as[JsObject]

}
