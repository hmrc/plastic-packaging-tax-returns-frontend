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

package uk.gov.hmrc.plasticpackagingtax.returns.controllers.agents

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.libs.json.Json
import play.api.test.Helpers.{await, redirectLocation, status, stubMessagesControllerComponents}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.plasticpackagingtax.returns.base.PptTestData
import uk.gov.hmrc.plasticpackagingtax.returns.base.unit.ControllerSpec
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.actions.AuthAgentActionImpl
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.home.{routes => homeRoutes}
import uk.gov.hmrc.plasticpackagingtax.returns.forms.agents.ClientIdentifier
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.agents.agents_page

class AgentsControllerSpec extends ControllerSpec {

  private val mcc  = stubMessagesControllerComponents()
  private val page = mock[agents_page]

  val mockAuthAgentAction = new AuthAgentActionImpl(mockAuthConnector,
                                                    config,
                                                    metricsMock,
                                                    stubMessagesControllerComponents()
  )

  private val controller =
    new AgentsController(authenticate = mockAuthAgentAction, mcc = mcc, page = page)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    when(page.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  "Agents Controller" should {
    "display client identifier page" when {
      "agent is authorised and display select client page is requested" in {
        val agent = PptTestData.newAgent("456")
        authorizedUser(agent, requiredPredicate = AffinityGroup.Agent)

        val result = controller.displayPage()(getRequest())

        status(result) must be(OK)
      }
    }

    "set selected client identifier on session and redirect to account page" when {
      "a correctly formatted PPT identifier is submitted" in {
        val agent = PptTestData.newAgent("456")
        authorizedUser(agent, requiredPredicate = AffinityGroup.Agent)

        val result = controller.submit(
          postRequest(Json.toJson(ClientIdentifier(identifier = "XMPPT1234567890")))
        )

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some(homeRoutes.HomeController.displayPage().url))

        await(result).newSession.flatMap(_.get("clientPPT")) must be(Some("XMPPT1234567890"))
      }
    }
  }
}
