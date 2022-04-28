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

package support

import models.obligations.PPTObligations
import models.returns.TaxReturnObligation

import java.time.LocalDate

object ObligationsTestData {

  private val today           = LocalDate.now()
  private val firstDayOfMonth = today.minusDays(today.getDayOfMonth - 1)

  val noneDueUpToDate = PPTObligations(
    nextObligation = None,
    oldestOverdueObligation = None,
    overdueObligationCount = 0,
    isNextObligationDue = false,
    displaySubmitReturnsLink = false
  )

  val oneDueUpToDate = PPTObligations(
    nextObligation = Some(aDueObligation()),
    oldestOverdueObligation = None,
    overdueObligationCount = 0,
    isNextObligationDue = true,
    displaySubmitReturnsLink = true
  )

  val oneDueOneOverdue = PPTObligations(
    nextObligation = Some(aDueObligation()),
    oldestOverdueObligation = Some(anOverdueObligation()),
    overdueObligationCount = 1,
    isNextObligationDue = true,
    displaySubmitReturnsLink = true
  )

  val oneDueTwoOverdue = PPTObligations(
    nextObligation = Some(aDueObligation()),
    oldestOverdueObligation = Some(anOverdueObligation()),
    overdueObligationCount = 2,
    isNextObligationDue = true,
    displaySubmitReturnsLink = true
  )

  private def aDueObligation() = {
    val periodStartDate = firstDayOfMonth.minusMonths(3)
    TaxReturnObligation(
      fromDate = periodStartDate,
      toDate = periodEndDate(periodStartDate),
      dueDate = periodDueDate(periodStartDate),
      periodKey = "22C1"
    )
  }

  private def anOverdueObligation() = {
    val periodStartDate = firstDayOfMonth.minusMonths(6)
    TaxReturnObligation(
      fromDate = periodStartDate,
      toDate = periodEndDate(periodStartDate),
      dueDate = periodDueDate(periodStartDate),
      periodKey = "22C2"
    )
  }

  private def periodEndDate(periodStartDate: LocalDate) = periodStartDate.plusMonths(3)

  private def periodDueDate(periodStartDate: LocalDate) = periodStartDate.plusMonths(5).minusDays(1)

}
