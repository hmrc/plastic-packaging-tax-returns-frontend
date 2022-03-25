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

package uk.gov.hmrc.plasticpackagingtax.returns.views.returns

import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers
import uk.gov.hmrc.plasticpackagingtax.returns.base.unit.UnitViewSpec
import uk.gov.hmrc.plasticpackagingtax.returns.builders.TaxReturnBuilder
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.home.{routes => homeRoutes}
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.returns.submitted_returns_page
import uk.gov.hmrc.plasticpackagingtax.returns.views.tags.ViewTest

@ViewTest
class SubmittedReturnsViewSpec extends UnitViewSpec with Matchers with TaxReturnBuilder {

  private val page = instanceOf[submitted_returns_page]

  private def createView(): Document =
    page()(request, messages)

  override def exerciseGeneratedRenderingMethods(): Unit = {
    page.f()(request, messages)
    page.render(request, messages)
  }

  "Submitted Returns View" should {

    val view = createView()

    "contain timeout dialog function" in {
      containTimeoutDialogFunction(view) mustBe true
    }

    "display sign out link" in {
      displaySignOutLink(view)
    }

    "display title" in {
      view.select("title").text() must include(messages("returns.submitted.title"))
      view.select("title").text() must include(messages("service.section"))
    }

    "display main page heading" in {
      view.select("h1").text() must include(messages("returns.submitted.title"))
    }

    "display message indicating no previous returns submitted" in {
      view.select("p").text() must include(messages("returns.submitted.none"))
    }

    "display link to PPT dashboard" in {
      val link = view.select("main a").first()

      link.text() must include(messages("returns.submitted.link"))
      link must haveHref(homeRoutes.HomeController.displayPage())
    }

  }

}
