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
import forms.returns.ManufacturedPlasticPackagingWeightFormProvider
import models.NormalMode
import models.returns.TaxReturnObligation
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import play.twirl.api.Html
import support.ViewMatchers
import views.html.returns.ImportedPlasticPackagingWeightView

import java.time.LocalDate

class ImportedPlasticPackagingWeightViewSpec extends ViewSpecBase with ViewMatchers {

  val page: ImportedPlasticPackagingWeightView = inject[ImportedPlasticPackagingWeightView]
  val aTaxObligation: TaxReturnObligation      = TaxReturnObligation(LocalDate.now(), LocalDate.now().plusWeeks(12), LocalDate.now().plusWeeks(16), "PK1")
  val form: Form[Long]                         = new ManufacturedPlasticPackagingWeightFormProvider()()

  private def createView(form: Form[Long] = form): Html =
    page(form, NormalMode, aTaxObligation)(request, messages)

  "Manufactured packaging weight page" should {

    "have a what to include paragraph" in {
      val view: Html    = createView(form.fillAndValidate(-1))
      val doc: Document = Jsoup.parse(view.toString())

      doc.text() must include(messages("importedPlasticPackagingWeight.paragraph.include"))
      doc.text() must include(messages("importedPlasticPackagingWeight.list.include.1"))
      doc.text() must include(messages("importedPlasticPackagingWeight.list.include.2"))
    }

    "have a what not to include paragraph" in {
      val view: Html    = createView(form.fillAndValidate(-1))
      val doc: Document = Jsoup.parse(view.toString())

      doc.text() must include(messages("importedPlasticPackagingWeight.paragraph.1"))
      doc.text() must include(messages("importedPlasticPackagingWeight.li.1"))
      doc.text() must include(messages("importedPlasticPackagingWeight.li.2"))
      doc.text() must include(messages("importedPlasticPackagingWeight.li.3"))
      doc.text() must include(messages("importedPlasticPackagingWeight.li.4"))
    }

    "have a hint" in {
      val view: Html    = createView()
      val doc: Document = Jsoup.parse(view.toString())

      doc.getElementById("value-hint").text  must include(messages("importedPlasticPackagingWeight.hint"))
    }

    "display error" when {
      "negative number submitted" in {
        val view: Html    = createView(form.fillAndValidate(-1))
        val doc: Document = Jsoup.parse(view.toString())

        doc.text() must include(messages("importedPlasticPackagingWeight.error.outOfRange.low"))
      }

      "number submitted is greater than maximum" in {
        val view: Html    = createView(form.fillAndValidate(999999999999L))
        val doc: Document = Jsoup.parse(view.toString())

        doc.text() must include(messages("importedPlasticPackagingWeight.error.outOfRange.high"))
      }
    }
  }
}
