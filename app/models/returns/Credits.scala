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

package models.returns

import models.returns.credits.CreditSummaryRow

trait Credits {
  def summaryList: Seq[CreditSummaryRow]
}

object Credits {
  case object NoCreditsClaimed extends Credits {
    override def summaryList: Seq[CreditSummaryRow] = Seq.empty
  }

  case object NoCreditAvailable extends Credits {
    override def summaryList: Seq[CreditSummaryRow] = Seq.empty
  }
}
