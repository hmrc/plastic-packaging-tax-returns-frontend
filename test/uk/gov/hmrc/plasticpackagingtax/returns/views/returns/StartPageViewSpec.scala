/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.plasticpackagingtax.returns.views.returns

import org.scalatest.matchers.must.Matchers
import play.api.mvc.{AnyContent, Request}
import play.api.test.FakeRequest
import play.twirl.api.Html
import uk.gov.hmrc.plasticpackagingtax.returns.base.unit.UnitViewSpec
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.returns.{routes => returnsRoutes}
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.returns.start_page
import uk.gov.hmrc.plasticpackagingtax.returns.views.tags.ViewTest
import utils.FakeRequestCSRFSupport.CSRFFakeRequest

@ViewTest
class StartPageViewSpec extends UnitViewSpec with Matchers {

  override implicit val request: Request[AnyContent] = FakeRequest().withCSRFToken

  private val startPage = instanceOf[start_page]

  private def createView(): Html = startPage()

  "Start Page view" should {

    "have proper messages for labels" in {

      messages must haveTranslationFor("returns.startPage.title")
      messages must haveTranslationFor("returns.startPage.title.sectionHeader")
      messages must haveTranslationFor("returns.startPage.informationYouNeed.header")
      messages must haveTranslationFor("returns.startPage.informationYouNeed.body")
      messages must haveTranslationFor("returns.startPage.informationYouNeed.listItems.header")
      messages must haveTranslationFor("returns.startPage.informationYouNeed.listItem.1")
      messages must haveTranslationFor("returns.startPage.informationYouNeed.listItem.2")
      messages must haveTranslationFor("returns.startPage.informationYouNeed.listItem.3")
      messages must haveTranslationFor("returns.startPage.informationYouNeed.listItem.4")
      messages must haveTranslationFor("returns.startPage.informationYouNeed.listItem.5")
      messages must haveTranslationFor("returns.startPage.whatIsLiable.header")
      messages must haveTranslationFor("returns.startPage.whatIsLiable.body.1")
      messages must haveTranslationFor("returns.startPage.whatIsLiable.body.2")
      messages must haveTranslationFor("returns.startPage.whatIsLiable.body.3")
      messages must haveTranslationFor("returns.startPage.whatIsLiable.listItems.header")
      messages must haveTranslationFor("returns.startPage.whatIsLiable.listItem.1")
      messages must haveTranslationFor("returns.startPage.whatIsLiable.listItem.2")
      messages must haveTranslationFor("returns.startPage.whatIsLiable.listItem.3")
      messages must haveTranslationFor("returns.startPage.buttonName")
    }

    val view: Html = createView()

    "validate other rendering  methods" in {
      startPage.f()(request, messages).select("title").text() must include(
        messages("returns.startPage.title")
      )
      startPage.render(request, messages).select("title").text() must include(
        messages("returns.startPage.title")
      )
    }

    "display title" in {

      view.select("title").text() must include(messages("returns.startPage.title"))
    }

    "display section header" in {

      view.getElementById("section-header") must containMessage(
        "returns.startPage.title.sectionHeader"
      )
    }

    "display header" in {

      view.getElementById("title") must containMessage("returns.startPage.title")
    }

    "display 'Information you need' section" in {

      view.getElementById("section-1") must containMessage(
        "returns.startPage.informationYouNeed.header"
      )
      view.getElementsByClass("govuk-body").get(0) must containMessage(
        "returns.startPage.informationYouNeed.body"
      )
      view.getElementsByClass("govuk-body").get(1) must containMessage(
        "returns.startPage.informationYouNeed.listItems.header"
      )
      view.getElementsByClass("dashed-list-item").get(0) must containMessage(
        "returns.startPage.informationYouNeed.listItem.1"
      )
      view.getElementsByClass("dashed-list-item").get(1) must containMessage(
        "returns.startPage.informationYouNeed.listItem.2"
      )
      view.getElementsByClass("dashed-list-item").get(2) must containMessage(
        "returns.startPage.informationYouNeed.listItem.3"
      )
      view.getElementsByClass("dashed-list-item").get(3) must containMessage(
        "returns.startPage.informationYouNeed.listItem.4"
      )
      view.getElementsByClass("dashed-list-item").get(4) must containMessage(
        "returns.startPage.informationYouNeed.listItem.5"
      )
    }

    "display 'What is liable?" in {

      view.getElementById("section-2") must containMessage("returns.startPage.whatIsLiable.header")
      view.getElementsByClass("govuk-body").get(2) must containMessage(
        "returns.startPage.whatIsLiable.body.1"
      )
      view.getElementsByClass("govuk-body").get(3) must containMessage(
        "returns.startPage.whatIsLiable.body.2"
      )
      view.getElementsByClass("govuk-body").get(4) must containMessage(
        "returns.startPage.whatIsLiable.listItems.header"
      )
      view.getElementsByClass("dashed-list-item").get(5) must containMessage(
        "returns.startPage.whatIsLiable.listItem.1"
      )
      view.getElementsByClass("dashed-list-item").get(6) must containMessage(
        "returns.startPage.whatIsLiable.listItem.2"
      )
      view.getElementsByClass("dashed-list-item").get(7) must containMessage(
        "returns.startPage.whatIsLiable.listItem.3"
      )
      view.getElementsByClass("govuk-body").get(5).text() must include(
        messages("returns.startPage.whatIsLiable.body.3",
                 messages("returns.startPage.whatIsLiable.body.3.link")
        )
      )
      view.getElementsByClass("govuk-link").get(1) must haveHref(
        "https://www.gov.uk/government/publications/introduction-of-plastic-packaging-tax/plastic-packaging-tax"
      )
    }

    "display 'Start now' button" in {

      view.getElementsByClass("govuk-button").first() must containMessage(
        "returns.startPage.buttonName"
      )
      view.getElementsByClass("govuk-button").first() must haveHref(
        returnsRoutes.ManufacturedPlasticWeightController.displayPage().url
      )
    }
  }
}
