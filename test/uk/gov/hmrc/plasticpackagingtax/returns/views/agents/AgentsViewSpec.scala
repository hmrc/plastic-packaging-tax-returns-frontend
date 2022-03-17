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

package uk.gov.hmrc.plasticpackagingtax.returns.views.agents

import org.scalatest.matchers.must.Matchers
import play.api.test.FakeRequest
import play.twirl.api.Html
import uk.gov.hmrc.plasticpackagingtax.returns.base.PptTestData
import uk.gov.hmrc.plasticpackagingtax.returns.base.unit.UnitViewSpec
import uk.gov.hmrc.plasticpackagingtax.returns.forms.agents.ClientIdentifier
import uk.gov.hmrc.plasticpackagingtax.returns.models.request.AuthenticatedRequest
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.agents.agents_page
import uk.gov.hmrc.plasticpackagingtax.returns.views.tags.ViewTest
import utils.FakeRequestCSRFSupport.CSRFFakeRequest

@ViewTest
class AgentsViewSpec extends UnitViewSpec with Matchers {

  private val agentsPage = instanceOf[agents_page]

  val authenticatedRequest = new AuthenticatedRequest(FakeRequest().withCSRFToken,
                                                      PptTestData.newUser(),
                                                      Some("XMPPT0000000001")
  )

  private def createView(): Html =
    agentsPage(ClientIdentifier.form())(authenticatedRequest, messages)

  override def exerciseGeneratedRenderingMethods(): Unit = {
    agentsPage.f(ClientIdentifier.form())(authenticatedRequest, messages)
    agentsPage.render(ClientIdentifier.form(), authenticatedRequest, messages)
  }

  val view: Html = createView()

  "Agents view" when {
    "display sign out link" in {
      displaySignOutLink(view)
    }

    "display title" in {
      view.select("title").text() must include(messages("account.agents.selectClient.header"))
    }
  }

}
