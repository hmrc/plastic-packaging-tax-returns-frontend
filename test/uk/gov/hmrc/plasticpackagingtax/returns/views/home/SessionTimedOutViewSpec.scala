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

package uk.gov.hmrc.plasticpackagingtax.returns.views.home

import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers
import play.api.mvc.{AnyContent, Request}
import play.api.test.FakeRequest
import uk.gov.hmrc.plasticpackagingtax.returns.base.unit.UnitViewSpec
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.home.session_timed_out
import uk.gov.hmrc.plasticpackagingtax.returns.views.tags.ViewTest
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.home.{routes => homeRoutes}
import utils.FakeRequestCSRFSupport._

@ViewTest
class SessionTimedOutViewSpec extends UnitViewSpec with Matchers {

  override implicit val request: Request[AnyContent] = FakeRequest().withCSRFToken

  private val page                   = instanceOf[session_timed_out]
  private def createView(): Document = page()(request, messages)

  override def exerciseGeneratedRenderingMethods(): Unit = {
    page.f()(request, messages)
    page.render(request, messages)
  }

  "Session Timeout View" should {

    "have proper messages for labels" in {
      messages must haveTranslationFor("sessionTimeout.title")
      messages must haveTranslationFor("sessionTimeout.paragraph.saved")
      messages must haveTranslationFor("sessionTimeout.signin.button")
      messages must haveTranslationFor("site.backToGovUk")

    }

    val view = createView()

    "not contain timeout dialog function" in {

      containTimeoutDialogFunction(view) mustBe false
    }

    "display title" in {

      view.getElementById("title") must containMessage("sessionTimeout.title")
    }

    "display saved answers info" in {

      view.getElementsByClass("govuk-body").first() must containMessage(
        "sessionTimeout.paragraph.saved"
      )
    }

    "display 'Sign in' button" in {

      view must containElementWithClass("govuk-button")
      view.getElementsByClass("govuk-button").first() must containMessage(
        "sessionTimeout.signin.button"
      )
      view.getElementsByClass("govuk-button").first() must haveHref(
        homeRoutes.HomeController.displayPage().url
      )
    }

    "display 'back to gov' link" in {

      view must containElementWithID("govuk-link")
      view.getElementById("govuk-link") must containMessage("site.backToGovUk")
      view.getElementById("govuk-link") must haveHref("https://www.gov.uk")
    }
  }
}
