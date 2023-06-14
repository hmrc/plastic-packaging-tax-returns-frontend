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

package views.templates

import base.ViewSpecBase
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.html.templates.YesNoLayout
import forms.mappings.Mappings
import play.api.data.Forms.boolean
import support.ViewMatchers

class YesNoLayoutSpec extends ViewSpecBase with ViewMatchers {

  private val page = inject[YesNoLayout]

  private val view = page(
    Some("section heading"),
    "heading",
    HtmlFormat.empty,
    None,
    Form("value" -> boolean)
  )(request, messages)

  "component" should {
    "contain a legend" in {
      view.select("legend").size() mustBe 1
    }

    "contain a legend with an hidden header" in {
      view.getElementsByClass("govuk-visually-hidden").text() mustBe "heading"
    }
  }
}
