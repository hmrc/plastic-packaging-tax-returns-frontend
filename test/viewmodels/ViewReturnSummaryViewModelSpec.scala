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

  private val returnDisplayChargeDetails = ReturnDisplayChargeDetails(
    "21C2", Some("charge-ref-no"), "2022-04-01", "2022-06-30", "2022-07-03", "New"
  )

  private val manufacturedWeight = 100
  private val returnDetails = ReturnDisplayDetails(
    manufacturedWeight, 2, 3, 4, 5, 6, 7, 8, 9, 10
  )

  private val submittedReturn = ReturnDisplayApi(
    "2019-08-28T09:30:47Z",
    IdDetails("XMPPT0000000001", "00-11-submission-id"),
    Some(returnDisplayChargeDetails),
    returnDetails
  )


  "The Summary section" must {

    "include a heading, the total liability, and processing date" in {
      val section = ViewReturnSummaryViewModel(submittedReturn).summarySection

      section.titleKey mustBe "viewReturnSummary.summary.heading"
      section.fields.zip(
        Seq(Field("viewReturnSummary.summary.field.1", "£10"), // TODO commas etc?
            Field("viewReturnSummary.summary.field.2", "2019-08-28T09:30:47Z"), // TODO parse and welsh-ify
        )
      ).map {
        case (actual, expected) => actual mustBe expected
      }
    }

    "have 3 entries" in {
      val section = ViewReturnSummaryViewModel(submittedReturn).summarySection
      section.fields must have(size(3))
    }

    "include the charge reference number when available" in {
      val section = ViewReturnSummaryViewModel(submittedReturn).summarySection
      section.fields(2) mustBe Field("viewReturnSummary.summary.field.3", "charge-ref-no")
    }

    "say 'n/a' when the charge reference number is not available" in {
      val anotherReturn = submittedReturn.changeChargeReferenceTo(None)
      val section = ViewReturnSummaryViewModel(anotherReturn).summarySection
      section.fields(2) mustBe Field("viewReturnSummary.summary.field.3", "n/a")
    }

  }

  "The Liable plastic packaging section" must {
    val liableSection = ViewReturnSummaryViewModel(submittedReturn).detailsSection.liable

    "have 3 entries" in {
      liableSection.fields must have(size(3))
    }

    "show manufactured packaging amount " ignore  {
      liableSection.fields(1) mustBe Field("viewReturnSummary.liable.field.1", "100")
    }
  }

}
