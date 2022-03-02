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

package uk.gov.hmrc.plasticpackagingtax.returns.models.obligations

import play.api.libs.json.{Json, OFormat}

final case class PPTObligations(
  nextObligation: Option[Obligation],
  oldestOverdueObligation: Option[Obligation],
  overdueObligationCount: Int,
  isNextObligationDue: Boolean,
  displaySubmitReturnsLink: Boolean
) {

  //todo confirm this? what happens if there isnt one? illegal state?
  def nextObligationToPay: Option[Obligation] =
    oldestOverdueObligation.orElse {
      if (isNextObligationDue) nextObligation else None
    }

}

object PPTObligations {
  implicit val format: OFormat[PPTObligations] = Json.format[PPTObligations]
}
