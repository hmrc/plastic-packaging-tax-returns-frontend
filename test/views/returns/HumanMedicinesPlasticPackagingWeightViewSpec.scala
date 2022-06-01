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

import forms.returns.HumanMedicinesPlasticPackagingWeightFormProvider
import models.NormalMode
import models.returns.TaxReturnObligation
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
import views.html.returns.HumanMedicinesPlasticPackagingWeightView

import java.time.LocalDate

class HumanMedicinesPlasticPackagingWeightViewSpec extends PlaySpec with GuiceOneAppPerSuite with Injecting with ViewMatchers {

  val page: HumanMedicinesPlasticPackagingWeightView = inject[HumanMedicinesPlasticPackagingWeightView]
  val request: Request[AnyContent]                 = FakeRequest().withCSRFToken
  val aTaxObligation: TaxReturnObligation          = TaxReturnObligation(LocalDate.now(), LocalDate.now().plusWeeks(12), LocalDate.now().plusWeeks(16), "PK1")
  val form: Form[Long]                             = new HumanMedicinesPlasticPackagingWeightFormProvider()()
  private val realMessagesApi: MessagesApi         = inject[MessagesApi]

  implicit def messages: Messages =
    realMessagesApi.preferred(request)

  private def createView(form: Form[Long] = form): Html =
    page(Long.MinValue, form, NormalMode, aTaxObligation)(request, messages)

  "Human medicines packaging weight page" should {

    "have a hint" in {
      val view: Html    = createView()
      val doc: Document = Jsoup.parse(view.toString())

      doc.getElementById("value-hint").text contains messages("humanMedicinesPlasticPackagingWeight.hint")
    }

    "display error" when {
      "negative number submitted" in {
        val view: Html    = createView(form.fillAndValidate(-1))
        val doc: Document = Jsoup.parse(view.toString())

        doc.text() must include(messages("humanMedicinesPlasticPackagingWeight.error.outOfRange.low"))
      }

      "number submitted is greater than maximum" in {
        val view: Html    = createView(form.fillAndValidate(999999999999L))
        val doc: Document = Jsoup.parse(view.toString())

        doc.text() must include(messages("humanMedicinesPlasticPackagingWeight.error.outOfRange.high"))
      }
    }
  }
}
