/*
 * Copyright 2022 HM Revenue & Customs
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

package viewmodels

import models.returns.{IdDetails, ReturnDisplayApi, ReturnDisplayChargeDetails, ReturnDisplayDetails}
import org.scalatestplus.play.PlaySpec
import viewmodels.checkAnswers.{Field, ViewReturnSummaryViewModel}

class ViewReturnSummaryViewModelSpec extends PlaySpec {


  "apply" must {
    "build the Summary section" in {
      val returnDisplayChargeDetails = ReturnDisplayChargeDetails(
        "21C2", Some("charge-ref"), "2022-04-01", "2022-06-30", "2022-07-03", "New"
      )
      val returnDetails = ReturnDisplayDetails(
        1, 2, 3, 4, 5, 6, 7, 8, 9, 10
      )
      val submittedReturn = ReturnDisplayApi(
        "2019-08-28T09:30:47Z",
        IdDetails("XMPPT0000000001", "00-11-submission-id"),
        Some(returnDisplayChargeDetails),
        returnDetails
      )

      val section = ViewReturnSummaryViewModel(submittedReturn).summarySection

      section.titleKey mustBe "viewReturnSummary.summary.heading"
      section.fields.zip(
        Seq(Field("viewReturnSummary.summary.field.1", "£10"),
            Field("viewReturnSummary.summary.field.2", "2019-08-28T09:30:47Z"), // TODO
            Field("viewReturnSummary.summary.field.3", "TODO"), // TODO
            Field("viewReturnSummary.summary.field.4", "TODO") // TODO
        )
      ).map {
        case (actual, expected) => actual mustBe expected
      }
    }
  }

}
