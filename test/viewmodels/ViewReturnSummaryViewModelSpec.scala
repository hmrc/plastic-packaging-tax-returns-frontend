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

import models.returns.{ChargeDetails, IdDetails, SubmittedReturn}
import org.scalatestplus.play.PlaySpec
import viewmodels.checkAnswers.{Field, ViewReturnSummaryViewModel}

class ViewReturnSummaryViewModelSpec extends PlaySpec {

  "apply" must {
    "build the Summary section" in {
      val submittedReturn = SubmittedReturn("31 July 2022", IdDetails("pptRef", ""), Some(ChargeDetails("", "", 400, "5 July 2022")), None, None)

      val section = ViewReturnSummaryViewModel(submittedReturn).summarySection

      section.titleKey mustBe "viewReturnSummary.summary.heading"
      section.fields.zip(Seq(
        Field("viewReturnSummary.summary.field.1", "£400"),
        Field("viewReturnSummary.summary.field.2", "31 July 2022"),
        Field("viewReturnSummary.summary.field.3", "5 July 2022"),
        Field("viewReturnSummary.summary.field.4", "pptRef")
      )).map {
        case (actual, expected) => actual mustBe expected
      }
    }
  }

}
