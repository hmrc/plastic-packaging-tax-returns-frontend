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

package views.amends

import base.ViewSpecBase
import forms.amends.AmendHumanMedicinePlasticPackagingFormProvider
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import play.twirl.api.Html
import views.html.amends.AmendHumanMedicinePlasticPackagingView

class AmendHumanMedicinePlasticPackagingViewSpec extends ViewSpecBase {

  val page: AmendHumanMedicinePlasticPackagingView = inject[AmendHumanMedicinePlasticPackagingView]
  val form: Form[Long] = new AmendHumanMedicinePlasticPackagingFormProvider()()

  private def createView(form: Form[Long] = form): Html =
    page(form)(request, messages)

  "Amend Human Medicine packaging page" should {

    "have a paragraph" in {
      val view: Html = createView()
      val doc: Document = Jsoup.parse(view.toString())

      doc.text() must include(messages("amendHumanMedicinePlasticPackaging.para"))
    }


    "have a hint" in {
      val view: Html = createView()
      val doc: Document = Jsoup.parse(view.toString())

      doc.getElementById("value-hint").text must include(messages("amendHumanMedicinePlasticPackaging.hint"))
    }

    "display error" when {
      "negative number submitted" in {
        val view: Html = createView(form.fillAndValidate(-1))
        val doc: Document = Jsoup.parse(view.toString())

        doc.text() must include(messages("amendHumanMedicinePlasticPackaging.error.outOfRange.low"))
      }

      "number submitted is greater than maximum" in {
        val view: Html = createView(form.fillAndValidate(999999999999L))
        val doc: Document = Jsoup.parse(view.toString())

        doc.text() must include(messages("amendHumanMedicinePlasticPackaging.error.outOfRange.high"))
      }

    }

  }
}
