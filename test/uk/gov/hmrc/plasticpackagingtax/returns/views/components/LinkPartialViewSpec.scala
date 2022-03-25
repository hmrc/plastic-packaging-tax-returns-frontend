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

package uk.gov.hmrc.plasticpackagingtax.returns.views.components

import org.scalatest.matchers.must.Matchers
import play.api.mvc.{AnyContent, Call, Request}
import play.api.test.FakeRequest
import play.twirl.api.Html
import uk.gov.hmrc.plasticpackagingtax.returns.base.unit.UnitViewSpec
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.components.link
import uk.gov.hmrc.plasticpackagingtax.returns.views.tags.ViewTest
import utils.FakeRequestCSRFSupport.CSRFFakeRequest

@ViewTest
class LinkPartialViewSpec extends UnitViewSpec with Matchers {

  override implicit val request: Request[AnyContent] = FakeRequest().withCSRFToken

  private val linkPartial = instanceOf[link]

  private def createPartial(hiddenText: Option[String] = None): Html =
    linkPartial(text = "visible text", call = Call("GET", "#overview"), textHidden = hiddenText)

  override def exerciseGeneratedRenderingMethods(): Unit = {
    linkPartial.f("text", None, Call("GET", "#overview"), false, None, "")
    linkPartial.render("text", None, Call("GET", "#overview"), true, None, "")
  }

  "Link partial" should {

    val partial: Html = createPartial()

    "validate visible text" in {
      partial.getElementsByClass("govuk-link").text() mustBe "visible text"
    }
  }
}
