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

package views.amends

import base.ViewSpecBase
import forms.amends.AmendImportedPlasticPackagingFormProvider
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import play.twirl.api.Html
import views.html.amends.AmendImportedPlasticPackagingView

class AmendImportedPackagingWeightViewSpec extends ViewSpecBase {

  val page: AmendImportedPlasticPackagingView = inject[AmendImportedPlasticPackagingView]
  val form: Form[Long] = new AmendImportedPlasticPackagingFormProvider()()

  private def createView(form: Form[Long] = form): Html =
    page(form)(request, messages)

  "Amend Imported packaging page" should {

    "have a what to include paragraph" in {
      val view: Html = createView()
      val doc: Document = Jsoup.parse(view.toString())

      doc.text() must include(messages("amendImportedPlasticPackaging.para.include"))
      doc.text() must include(messages("amendImportedPlasticPackaging.para.list.1"))
      doc.text() must include(messages("amendImportedPlasticPackaging.para.list.2"))

    }

    "have a what not to include paragraph" in {
      val view: Html = createView()
      val doc: Document = Jsoup.parse(view.toString())

      doc.text() must include(messages("amendImportedPlasticPackaging.para.exclude"))
      doc.text() must include(messages("amendImportedPlasticPackaging.para.list.3"))
      doc.text() must include(messages("amendImportedPlasticPackaging.para.list.4"))
      doc.text() must include(messages("amendImportedPlasticPackaging.para.list.5"))
      doc.text() must include(messages("amendImportedPlasticPackaging.para.list.6"))
      doc.text() must include(messages("amendImportedPlasticPackaging.para.list.7"))
      doc.text() must include(messages("amendImportedPlasticPackaging.para.list.8"))
    }

    "have a hint" in {
      val view: Html = createView()
      val doc: Document = Jsoup.parse(view.toString())

      doc.getElementById("value-hint").text must include(messages("amendImportedPlasticPackaging.hint"))
    }

    "display error" when {
      "negative number submitted" in {
        val view: Html = createView(form.fillAndValidate(-1))
        val doc: Document = Jsoup.parse(view.toString())

        doc.text() must include(messages("amendImportedPlasticPackaging.error.outOfRange.low"))
      }

      "number submitted is greater than maximum" in {
        val view: Html = createView(form.fillAndValidate(999999999999L))
        val doc: Document = Jsoup.parse(view.toString())

        doc.text() must include(messages("amendImportedPlasticPackaging.error.outOfRange.high"))
      }

    }

  }
}