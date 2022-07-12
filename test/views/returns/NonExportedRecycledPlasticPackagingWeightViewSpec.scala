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
import forms.returns.NonExportedRecycledPlasticPackagingWeightFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import play.twirl.api.Html
import support.ViewMatchers
import views.html.returns.NonExportedRecycledPlasticPackagingWeightView

class NonExportedRecycledPlasticPackagingWeightViewSpec extends ViewSpecBase with ViewMatchers {

  val page: NonExportedRecycledPlasticPackagingWeightView = inject[NonExportedRecycledPlasticPackagingWeightView]
  val form: Form[Long] = new NonExportedRecycledPlasticPackagingWeightFormProvider()()
  val amount = 321L

  private def createView(form: Form[Long] = form): Html =
    page(form, NormalMode, amount)(request, messages)

  "NonExport Recycled Plastic Packaging Weight page" should {
    val view: Html = createView()
    val doc: Document = Jsoup.parse(view.toString())

    "have a hint" in {

      doc.getElementById("value-hint").text must include(messages("NonExportRecycledPlasticPackagingWeight.hint"))
    }

    "have a caption" in {

      view.getElementById("section-header").text() mustBe messages("caption.non.exported.plastic")
    }

    "contain paragraph content" in {

      view.getElementsByClass("govuk-body").text() must include(messages("NonExportRecycledPlasticPackagingWeight.paragraph"))
    }
    "contain save & continue button" in {

      view.getElementsByClass("govuk-button").text() mustBe messages("site.continue")
    }
    "display error" when {
      "negative number submitted" in {
        val view: Html = createView(form.fillAndValidate(-1))
        val doc: Document = Jsoup.parse(view.toString())

        doc.text() must include(messages("NonExportRecycledPlasticPackagingWeight.error.outOfRange.low"))
      }

      "number submitted is greater than maximum" in {
        val view: Html = createView(form.fillAndValidate(999999999999L))
        val doc: Document = Jsoup.parse(view.toString())

        doc.text() must include(messages("NonExportRecycledPlasticPackagingWeight.error.outOfRange.high"))
      }
    }
  }
}
