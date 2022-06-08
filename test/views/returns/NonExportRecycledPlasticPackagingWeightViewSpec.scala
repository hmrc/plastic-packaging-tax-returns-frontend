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

import forms.returns.NonExportRecycledPlasticPackagingWeightFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{AnyContent, Request}
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.{FakeRequest, Injecting}
import play.twirl.api.Html
import support.ViewMatchers
import views.html.returns.NonExportRecycledPlasticPackagingWeightView

class NonExportRecycledPlasticPackagingWeightViewSpec extends PlaySpec with GuiceOneAppPerSuite with Injecting with ViewMatchers {

  val page: NonExportRecycledPlasticPackagingWeightView = inject[NonExportRecycledPlasticPackagingWeightView]
  val request: Request[AnyContent] = FakeRequest().withCSRFToken
  val form: Form[Long] = new NonExportRecycledPlasticPackagingWeightFormProvider()()
  private val realMessagesApi: MessagesApi = inject[MessagesApi]

  implicit def messages: Messages =
    realMessagesApi.preferred(request)

  private def createView(form: Form[Long] = form): Html =
    page(form, NormalMode)(request, messages)

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

      view.getElementById("value-hint").text() must include(messages("NonExportRecycledPlasticPackagingWeight.paragraph"))
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
