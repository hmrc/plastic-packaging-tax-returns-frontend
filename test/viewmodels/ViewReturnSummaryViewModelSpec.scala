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
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.mock
import org.scalatestplus.play.PlaySpec
import play.api.i18n.Messages
import viewmodels.checkAnswers.{Field, ViewReturnSummaryViewModel}

class ViewReturnSummaryViewModelSpec extends PlaySpec {

  val mockMessages: Messages = mock[Messages]
  when(mockMessages.apply(anyString(), any())).thenReturn("August")

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
    val summarySection = ViewReturnSummaryViewModel(submittedReturn)(mockMessages).summarySection
    "have the title Key" in {
      summarySection.titleKey mustBe "viewReturnSummary.summary.heading"
    }

    "have 3 entries" in {
      summarySection.fields must have size 3
    }

    "have the liability field" in {
      summarySection.fields.head mustBe Field("viewReturnSummary.summary.field.liability", "£10.00")
    }

    "have the processed field" in {
      summarySection.fields(1) mustBe Field("viewReturnSummary.summary.field.processed", "28 August 2019")
    }

    "have the reference field" in {
      summarySection.fields(2) mustBe Field("viewReturnSummary.summary.field.reference", "charge-ref-no")
    }

    "say 'n/a' when the charge reference number is not available" in {
      val anotherReturn = submittedReturn.copy(chargeDetails = Some(returnDisplayChargeDetails.copy(chargeReference = None)))
      val section = ViewReturnSummaryViewModel(anotherReturn)(mockMessages).summarySection
      section.fields(2) mustBe Field("viewReturnSummary.summary.field.reference", "n/a")
    }

  }

  "The Liable plastic packaging section" must {
    val liableSection = ViewReturnSummaryViewModel(submittedReturn)(mockMessages).detailsSection.liable

    "have the title Key" in {
      liableSection.titleKey mustBe "viewReturnSummary.liable.heading"
    }

    "have 4 entries" in {
      liableSection.fields must have(size(3))
    }

    "have the manufactured field" in  {
      liableSection.fields(0) mustBe Field("viewReturnSummary.liable.field.manufactured", "100kg")
    }

    "have the imported field" in  {
      liableSection.fields(1) mustBe Field("viewReturnSummary.liable.field.imported", "2kg")
    }

    "have the total field" in  {
      liableSection.fields(2) mustBe Field("viewReturnSummary.liable.field.total", "102kg", bold = true)
    }
  }

  "Exempt section" must {
    val exemptSection = ViewReturnSummaryViewModel(submittedReturn)(mockMessages).detailsSection.exempt

    "have the title Key" in {
      exemptSection.titleKey mustBe "viewReturnSummary.exempt.heading"
    }

    "have 4 entries" in {
      exemptSection.fields must have(size(4))
    }

    "have the exported field" in  {
      exemptSection.fields(0) mustBe Field("viewReturnSummary.exempt.field.exported", "5kg")
    }

    "have the medicine field" in  {
      exemptSection.fields(1) mustBe Field("viewReturnSummary.exempt.field.medicine", "4kg")
    }

    "have the recycled field" in  {
      exemptSection.fields(2) mustBe Field("viewReturnSummary.exempt.field.recycled", "6kg")
    }

    "have the recycled total" in  {
      exemptSection.fields(3) mustBe Field("viewReturnSummary.exempt.field.total", "3kg", bold = true)
    }
  }

  "Calculation section" must {
    val calculationSection = ViewReturnSummaryViewModel(submittedReturn)(mockMessages).detailsSection.calculation

    "have the title Key" in {
      calculationSection.titleKey mustBe "viewReturnSummary.calculation.heading"
    }

    "have the right number of entries" in {
      calculationSection.fields must have(size(4))
    }

    "have the liable field" in {
      calculationSection.fields(0) mustBe Field("viewReturnSummary.calculation.field.liable", "102kg")
    }

    "have the exempt field" in {
      calculationSection.fields(1) mustBe Field("viewReturnSummary.calculation.field.exempt", "3kg")
    }

    "have the total field" in {
      calculationSection.fields(2) mustBe Field("viewReturnSummary.calculation.field.total", "9kg")
    }

    "have the tax total" in {
      calculationSection.fields(3) mustBe Field("viewReturnSummary.calculation.field.tax", "£10.00",
        bold = true, big = false)
    }
  }
}
