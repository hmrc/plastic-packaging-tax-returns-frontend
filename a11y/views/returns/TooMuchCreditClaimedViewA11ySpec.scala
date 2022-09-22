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

package views.returns

import base.ViewSpecBase
import play.api.mvc.Call
import play.api.test.Helpers.GET
import play.twirl.api.Html
import uk.gov.hmrc.scalatestaccessibilitylinter.AccessibilityMatchers
import views.html.returns.credits.TooMuchCreditClaimedView

class TooMuchCreditClaimedViewA11ySpec extends ViewSpecBase with AccessibilityMatchers {

  val page = inject[TooMuchCreditClaimedView]

  "view" should {
    "pass accessibility tests" in {
      def render: Html = page(Call(GET,"/change-weight"),Call(GET,"/cancel-claim"))(request, messages)

      render.toString() must passAccessibilityChecks
    }
  }

}
