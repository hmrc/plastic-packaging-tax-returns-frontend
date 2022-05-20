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

import forms.ManufacturedPlasticPackagingWeightFormProvider
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
import views.html.ManufacturedPlasticPackagingWeightView

import java.time.LocalDate

class ManufacturedPlasticPackagingWeightViewSpec extends PlaySpec with GuiceOneAppPerSuite with Injecting {

  val page: ManufacturedPlasticPackagingWeightView = inject[ManufacturedPlasticPackagingWeightView]
  val request: Request[AnyContent]                 = FakeRequest().withCSRFToken

  val aTaxObligation: TaxReturnObligation =
    TaxReturnObligation(LocalDate.now(), LocalDate.now().plusWeeks(12), LocalDate.now().plusWeeks(16), "PK1")

  private val realMessagesApi: MessagesApi = inject[MessagesApi]

  implicit def messages: Messages =
    realMessagesApi.preferred(request)

  private def createView(form: Form[Int], previousReturn: TaxReturnObligation): Html =
    page(form, NormalMode, previousReturn)(request, messages)

  "Manufactured weight page" must {

      val form = new ManufacturedPlasticPackagingWeightFormProvider()()
      val view = createView(form, aTaxObligation)
      val doc: Document = Jsoup.parse(view.toString())
    "test" in {

      val t = form.fill(-1)
      doc haveGovukGlobalErrorSummary

    }


  }
}
