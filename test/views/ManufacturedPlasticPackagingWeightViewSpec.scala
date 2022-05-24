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

package views

import models.returns.TaxReturnObligation
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{AnyContent, Request}
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.{FakeRequest, Injecting}
import play.twirl.api.Html
import views.html.ManufacturedPlasticPackagingWeightView
import forms.ManufacturedPlasticPackagingWeightFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import support.ViewMatchers

import java.time.LocalDate

class ManufacturedPlasticPackagingWeightViewSpec extends PlaySpec with GuiceOneAppPerSuite with Injecting with ViewMatchers {

  val page: ManufacturedPlasticPackagingWeightView = inject[ManufacturedPlasticPackagingWeightView]
  val request: Request[AnyContent]                 = FakeRequest().withCSRFToken
  val aTaxObligation: TaxReturnObligation          = TaxReturnObligation(LocalDate.now(), LocalDate.now().plusWeeks(12), LocalDate.now().plusWeeks(16), "PK1")
  val form: Form[Long]                              = new ManufacturedPlasticPackagingWeightFormProvider()()
  private val realMessagesApi: MessagesApi         = inject[MessagesApi]

  implicit def messages: Messages =
    realMessagesApi.preferred(request)

  private def createView(form: Form[Long] = form): Html =
    page(form, NormalMode, aTaxObligation)(request, messages)

  "Manufactured packaging weight page" should {

    "have a hint" in {
      val view          = createView()
      val doc: Document = Jsoup.parse(view.toString())

      doc.getElementById("value-hint").text contains messages("manufacturedPlasticPackagingWeight.hint")
    }

    "display error" when {
      "negative number submitted" in {
        val view = createView(form.fillAndValidate(-1))
        val doc: Document = Jsoup.parse(view.toString())

        doc.text() must include(messages("manufacturedPlasticPackagingWeight.error.outOfRange", 0,99999999999L))
      }

      "number submitted is greater than maximum"in{
        val view = createView(form.fillAndValidate(999999999999L))
        val doc: Document = Jsoup.parse(view.toString())
        doc.text() must include(messages("manufacturedPlasticPackagingWeight.error.outOfRange", 0,99999999999L))

      }
    }
  }
}
