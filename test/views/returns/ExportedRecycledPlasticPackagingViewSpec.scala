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

import config.FrontendAppConfig
import forms.ExportedRecycledPlasticPackagingFormProvider
import models.NormalMode
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{AnyContent, Request}
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.{FakeRequest, Injecting}
import play.twirl.api.Html
import support.{ViewAssertions, ViewMatchers}
import views.html.returns.ExportedRecycledPlasticPackagingView

class ExportedRecycledPlasticPackagingViewSpec
  extends PlaySpec
    with GuiceOneAppPerSuite
    with Injecting
    with ViewAssertions
    with ViewMatchers {

  val page: ExportedRecycledPlasticPackagingView = inject[ExportedRecycledPlasticPackagingView]
  val request: Request[AnyContent] = FakeRequest().withCSRFToken
  val formProvider = new ExportedRecycledPlasticPackagingFormProvider()
  val appConfig: FrontendAppConfig = inject[FrontendAppConfig]

  private val realMessagesApi: MessagesApi = inject[MessagesApi]

  implicit def messages: Messages = realMessagesApi.preferred(request)
  val weight: Long = 200

  private def createView(form: Form[Boolean]): Html =
    page(form, NormalMode, weight)(request, messages)

  "view" should {

    val view = createView(formProvider())
    "have a title" in {
      view.select("title").text() must include(messages("exportedRecycledPlasticPackaging.title", weight))

      view.select("title").text() mustBe
        (s"Did any of your ${weight}kg exported finished plastic packaging components contain 30% or more recycled plastic? - Submit return - Plastic Packaging Tax - GOV.UK")
    }

    "have a header" in {
      view.select("h1").text() mustBe s"Did any of your ${weight}kg exported finished plastic packaging components contain 30% or more recycled plastic?"
      view.select("h1").text() mustBe messages("exportedRecycledPlasticPackaging.heading", weight)
    }

    "have a caption" in {
      view.getElementById("section-header").text() mustBe "Exported plastic packaging"
      view.getElementById("section-header").text() mustBe messages("exportedRecycledPlasticPackaging.sectionHeader")
    }

    "contain paragraph content" in{
      view.getElementById("value-hint").text() must include (messages("exportedRecycledPlasticPackaging.hint1"))
      view.getElementById("value-hint").text() must include (messages(
        "exportedRecycledPlasticPackaging.hint2",
        messages("exportedRecycledPlasticPackaging.hint2.link")))
      view.getElementById("govuk-guidance-link") must haveHref(appConfig.pptRecycledPlasticGuidanceLink)
    }

    "contain save & continue button" in {
      view.getElementsByClass("govuk-button").text() mustBe  messages("site.continue")
    }

    "return an error" when {
      "no answer was selected" in {
        val emptyForm: Map[String, String] = Map.empty
        val view = createView(formProvider().bind(emptyForm))

        view.getElementsByClass("govuk-error-summary").text() must include("Select yes if any of your exported finished plastic packaging components contained 30% or more recycled plastic")
        view.getElementsByClass("govuk-error-summary").text() must include(messages("exportedRecycledPlasticPackaging.error.required"))
      }
    }

  }

}
