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
import uk.gov.hmrc.plasticpackagingtax.returns.config.AppConfig
import uk.gov.hmrc.plasticpackagingtax.returns.base.unit.UnitViewSpec
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.returns.{returns_information_page}
import uk.gov.hmrc.plasticpackagingtax.returns.views.tags.ViewTest
import utils.FakeRequestCSRFSupport.CSRFFakeRequest

@ViewTest
class ReturnsInformationPageViewSpec extends UnitViewSpec with Matchers {

  override implicit val request: Request[AnyContent] = FakeRequest().withCSRFToken

  private val returnsInformationPage = instanceOf[returns_information_page]
  private val appConfig              = instanceOf[AppConfig]

  private def createView(): Html = returnsInformationPage()

  "Returns information page" should {
    "have proper messages for labels" in {
      messages must haveTranslationFor("returns.information.title")
      messages must haveTranslationFor("returns.information.heading1")
      messages must haveTranslationFor("returns.information.body.1")
      messages must haveTranslationFor("returns.information.body.2")
      messages must haveTranslationFor("returns.information.listItem.1")
      messages must haveTranslationFor("returns.information.listItem.2")
      messages must haveTranslationFor("returns.information.listItem.3")
      messages must haveTranslationFor("returns.information.listItem.4")
      messages must haveTranslationFor("returns.information.listItem.5")
      messages must haveTranslationFor("returns.information.heading2")
      messages must haveTranslationFor("returns.information.body.3")
      messages must haveTranslationFor("returns.information.body.4.text")
      messages must haveTranslationFor("returns.information.body.4.link.text")
    }

    val view: Html = createView()

    "validate other rendering methods" in {
      returnsInformationPage.f()(request, messages)
      returnsInformationPage.render(request, messages)
    }

    "display title" in {
      view.select("title").text() must include(messages("returns.information.title"))
    }

    "display main heading" in {
      view.select("h1").text() must include(messages("returns.information.title"))
    }

    "display required information content" in {
      view.getElementById("info-heading").text() mustBe messages("returns.information.heading1")
      view.getElementById("info-detail1").text() mustBe messages("returns.information.body.1")
      view.getElementById("info-detail2").text() mustBe messages("returns.information.body.2")
      view.getElementById("info-detail-components").text() must include(
        messages("returns.information.listItem.1")
      )
      view.getElementById("info-detail-components").text() must include(
        messages("returns.information.listItem.2")
      )
      view.getElementById("info-detail-components").text() must include(
        messages("returns.information.listItem.3")
      )
      view.getElementById("info-detail-components").text() must include(
        messages("returns.information.listItem.4")
      )
      view.getElementById("info-detail-components").text() must include(
        messages("returns.information.listItem.5")
      )
    }

    "display when finished content" in {
      view.getElementById("finished-heading").text() mustBe messages("returns.information.heading2")
      view.getElementById("finished-detail1").text() mustBe messages("returns.information.body.3")
      view.getElementById("finished-detail2").text() mustBe messages(
        "returns.information.body.4.text",
        messages("returns.information.body.4.link.text")
      )
    }

    "display guidance link" in {
      view.getElementById("guidance-link") must haveHref(appConfig.pptGuidanceUrl)
    }
  }
}
