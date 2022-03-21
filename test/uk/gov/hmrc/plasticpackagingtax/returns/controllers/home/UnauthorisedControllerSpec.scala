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

package uk.gov.hmrc.plasticpackagingtax.returns.controllers.home

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.auth.core.CredentialStrength
import uk.gov.hmrc.plasticpackagingtax.returns.base.unit.ControllerSpec
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.actions.AuthCheckActionImpl
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.home.unauthorised

class UnauthorisedControllerSpec extends ControllerSpec {

  private val page = mock[unauthorised]

  val mockAuthCheckAction = new AuthCheckActionImpl(mockAuthConnector,
                                                    config,
                                                    metricsMock,
                                                    stubMessagesControllerComponents()
  )

  val controller =
    new UnauthorisedController(mockAuthCheckAction, stubMessagesControllerComponents(), page)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    when(page.apply()(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(page)
    super.afterEach()
  }

  "Unauthorised controller" should {

    "return 200 (OK)" when {
      "display page method is invoked" in {
        val result = controller.unauthorised()(getRequest())

        status(result) must be(OK)
      }

      "enrolments page is invoked with by a signed in user" in {
        authorizedUser(requiredPredicate = CredentialStrength(CredentialStrength.strong))

        val result = controller.notEnrolled()(getRequest())

        status(result) must be(OK)
      }
    }

  }
}
