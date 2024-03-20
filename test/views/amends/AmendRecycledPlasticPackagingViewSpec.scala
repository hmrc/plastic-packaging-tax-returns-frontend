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
import forms.amends.AmendRecycledPlasticPackagingFormProvider
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import views.html.amends.AmendRecycledPlasticPackagingView

class AmendRecycledPlasticPackagingViewSpec extends ViewSpecBase {

  val page: AmendRecycledPlasticPackagingView = inject[AmendRecycledPlasticPackagingView]
  val form: Form[Long]                        = new AmendRecycledPlasticPackagingFormProvider()()

  private def getDocumentFromView(form: Form[Long] = form): Document = {
    val view = page(form)(request, messages)
    Jsoup.parse(view.toString())
  }

  "Amend Recycled packaging page" should {

    "have a title" in {
      val view = getDocumentFromView()

      val title = view.select("title").text()
      title mustBe "Out of the finished plastic packaging components that were not exported, how much contained 30% or more recycled plastic? - Submit return - Plastic Packaging Tax - GOV.UK"
      title must include(messages("amendRecycledPlasticPackaging.title"))
    }

    "have a heading" in {
      val view = getDocumentFromView()

      val heading = view.select("h1").text()
      heading mustBe "Out of the finished plastic packaging components that were not exported, how much contained 30% or more recycled plastic?"
      heading must include(messages("amendRecycledPlasticPackaging.heading"))
    }

    "have a paragraph" in {
      val doc: Document = getDocumentFromView()

      doc.text() must include(messages("amendRecycledPlasticPackaging.para"))
    }

    "have a hint" in {
      val doc: Document = getDocumentFromView()

      doc.getElementById("value-hint").text must include(messages("amendRecycledPlasticPackaging.hint"))
    }

    "display error" when {
      "negative number submitted" in {
        val doc: Document = getDocumentFromView(form.fillAndValidate(-1))

        doc.text() must include(messages("amendRecycledPlasticPackaging.error.outOfRange.low"))
      }

      "number submitted is greater than maximum" in {
        val doc: Document = getDocumentFromView(form.fillAndValidate(999999999999L))

        doc.text() must include(messages("amendRecycledPlasticPackaging.error.outOfRange.high"))
      }

    }

  }
}
