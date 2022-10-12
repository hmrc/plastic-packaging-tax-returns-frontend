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
import models.Mode.NormalMode
import models.returns.TaxReturnObligation
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import play.twirl.api.Html
import support.ViewMatchers
import views.html.returns.ManufacturedPlasticPackagingWeightView

import java.time.LocalDate

class ManufacturedPlasticPackagingWeightViewSpec extends ViewSpecBase with ViewMatchers {

  val page: ManufacturedPlasticPackagingWeightView = inject[ManufacturedPlasticPackagingWeightView]
  val aTaxObligation: TaxReturnObligation          = TaxReturnObligation(LocalDate.now(), LocalDate.now().plusWeeks(12), LocalDate.now().plusWeeks(16), "PK1")
  val form: Form[Long]                             = new ManufacturedPlasticPackagingWeightFormProvider()()

  private def createView(form: Form[Long] = form): Html =
    page(form, NormalMode, aTaxObligation)(request, messages)

  "Manufactured packaging weight page" should {

    "have a what to include paragraph" in {
      val view: Html    = createView(form.fillAndValidate(-1))
      val doc: Document = Jsoup.parse(view.toString())

      doc.text() must include("Include in your total, plastic packaging which (either of the following):")
      doc.text() must include(messages("manufacturedPlasticPackagingWeight.paragraph.include"))
      doc.text() must include(messages("manufacturedPlasticPackagingWeight.list.include.1"))
      doc.text() must include(messages("manufacturedPlasticPackagingWeight.list.include.2"))
    }

    "have a what not to include paragraph" in {
      val view: Html    = createView(form.fillAndValidate(-1))
      val doc: Document = Jsoup.parse(view.toString())

      doc.text() must include(messages("manufacturedPlasticPackagingWeight.paragraph"))
      doc.text() must include(messages("manufacturedPlasticPackagingWeight.list.1"))
      doc.text() must include(messages("manufacturedPlasticPackagingWeight.list.2"))
      doc.text() must include(messages("manufacturedPlasticPackagingWeight.list.3"))
      doc.text() must include(messages("manufacturedPlasticPackagingWeight.list.4"))
      doc.text() must include(messages("manufacturedPlasticPackagingWeight.list.5"))
    }

    "have a hint" in {
      val view: Html    = createView()
      val doc: Document = Jsoup.parse(view.toString())

      doc.getElementById("value-hint").text  must include(messages("manufacturedPlasticPackagingWeight.hint"))
    }

    "display error" when {
      "negative number submitted" in {
        val view: Html    = createView(form.fillAndValidate(-1))
        val doc: Document = Jsoup.parse(view.toString())

        doc.text() must include(messages("manufacturedPlasticPackagingWeight.error.outOfRange.low"))
      }

      "number submitted is greater than maximum" in {
        val view: Html    = createView(form.fillAndValidate(999999999999L))
        val doc: Document = Jsoup.parse(view.toString())

        doc.text() must include(messages("manufacturedPlasticPackagingWeight.error.outOfRange.high"))
      }
    }
  }
}
