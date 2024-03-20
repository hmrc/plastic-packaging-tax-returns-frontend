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

package support

import cacheables.{AmendObligationCacheable, ReturnDisplayApiCacheable}
import models.UserAnswers
import models.returns._

import java.time.LocalDate

trait AmendExportedData {

  val charge: ReturnDisplayChargeDetails = ReturnDisplayChargeDetails(
    periodFrom = "2022-04-01",
    periodTo = "2022-06-30",
    periodKey = "22AC",
    chargeReference = Some("pan"),
    receiptDate = "2022-06-31",
    returnType = "TYPE"
  )

  val retDisApi: ReturnDisplayApi = ReturnDisplayApi(
    "",
    IdDetails("", ""),
    Some(charge),
    ReturnDisplayDetails(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
  )

  val taxReturnOb: TaxReturnObligation = TaxReturnObligation(
    LocalDate.parse("2022-04-01"),
    LocalDate.parse("2022-06-30"),
    LocalDate.parse("2022-06-30").plusWeeks(8),
    "00XX"
  )

  def createUserAnswers: UserAnswers =
    UserAnswers("123").set(ReturnDisplayApiCacheable, retDisApi).get
      .set(AmendObligationCacheable, taxReturnOb).get
}
