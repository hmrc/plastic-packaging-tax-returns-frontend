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

import forms.returns.NonExportedHumanMedicinesPlasticPackagingWeightFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{AnyContent, Request}
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.{FakeRequest, Injecting}
import play.twirl.api.Html
import support.{ViewAssertions, ViewMatchers}
import views.html.returns.NonExportedHumanMedicinesPlasticPackagingWeightView

class NonExportedHumanMedicinesPlasticPackagingWeightViewSpec extends PlaySpec with GuiceOneAppPerSuite with Injecting with ViewAssertions with ViewMatchers {

  private val realMessagesApi: MessagesApi = inject[MessagesApi]

  val request: Request[AnyContent] = FakeRequest().withCSRFToken

  implicit def messages: Messages =
    realMessagesApi.preferred(request)

  val form = new NonExportedHumanMedicinesPlasticPackagingWeightFormProvider()()

  val page = inject[NonExportedHumanMedicinesPlasticPackagingWeightView]

  val plastic = 1234L

  private def createView: Html =
    page(plastic, form, NormalMode)(request, messages)

  "NonExportedHumanMedicinesPlasticPackagingWeightView" should {
    val view = createView

    "have a title" in {

      view.select("title").text mustBe
        "Out of the 1,234 kg of finished plastic packaging components that you did not export, how much was used for the immediate packaging of licenced human medicines? - Submit return - Plastic Packaging Tax - GOV.UK"

    }
    "have a heading" in{

      view.select("h1").text mustBe
        "Out of the 1,234 kg of finished plastic packaging components that you did not export, how much was used for the immediate packaging of licenced human medicines?"

    }
    "have a caption" in {

      view.getElementById("section-header").text() mustBe messages("nonExportedHumanMedicinesPlasticPackagingWeight.caption")

    }

    "have a hint" in {

      val view: Html    = createView
      val doc: Document = Jsoup.parse(view.toString())

      doc.getElementById("value-hint").text must include (messages("1 tonne is 1,000kg."))

    }


    "contain save & continue button" in {

      view.getElementsByClass("govuk-button").text() mustBe  messages("site.continue")

    }

  }

}
