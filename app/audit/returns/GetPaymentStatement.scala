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

package audit.returns

import models.financials.PPTFinancials
import play.api.libs.json.{Json, OFormat}

case class GetPaymentStatement(internalId: String,
                               pptReference: String,
                               result: String,
                               response: Option[PPTFinancials],
                               error: Option[String],
                               headers: Seq[(String, String)])

object GetPaymentStatement {
  implicit val format: OFormat[GetPaymentStatement] = Json.format[GetPaymentStatement]
  val eventType: String                             = "GetPaymentStatement"
}





