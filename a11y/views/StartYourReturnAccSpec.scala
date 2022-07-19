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

package views

import base.ViewSpecBase
import forms.returns.StartYourReturnFormProvider
import models.Mode.NormalMode
import models.returns.TaxReturnObligation
import play.twirl.api.Html
import uk.gov.hmrc.scalatestaccessibilitylinter.AccessibilityMatchers
import views.html.returns.StartYourReturnView

import java.time.LocalDate

class StartYourReturnAccSpec
  extends ViewSpecBase
    with AccessibilityMatchers {

  val form = new StartYourReturnFormProvider()()

  val page = inject[StartYourReturnView]
  val aTaxObligation: TaxReturnObligation = TaxReturnObligation(LocalDate.now(), LocalDate.now().plusWeeks(12), LocalDate.now().plusWeeks(16), "PK1")

  private def createView: Html =
    page(form, NormalMode, aTaxObligation, true)(request, messages)

  "StartYourReturnView" should {

    "pass accessibility checks" in {
      createView.toString() must passAccessibilityChecks
    }

  }

}
