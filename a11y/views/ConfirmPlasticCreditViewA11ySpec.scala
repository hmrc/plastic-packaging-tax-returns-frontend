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

package views

import base.ViewSpecBase
import models.Mode.NormalMode
import play.api.mvc.Call
import play.twirl.api.Html
import uk.gov.hmrc.scalatestaccessibilitylinter.AccessibilityMatchers
import views.html.returns.credits.ConfirmPackagingCreditView

class ConfirmPlasticCreditViewA11ySpec extends ViewSpecBase with AccessibilityMatchers {

  "ConfirmPackagingCreditView" should {
    "pass accessibility checks" in {

      val page = inject[ConfirmPackagingCreditView]

      def render: Html = page(BigDecimal(200), true,"1 April 2022", "31 MArch 2023", Seq.empty, Call("GET", "/test"), NormalMode)(request, messages)

      render.toString() must passAccessibilityChecks
    }
  }
}
