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

package uk.gov.hmrc.plasticpackagingtax.returns.controllers.returns

import org.mockito.Mockito.when
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.http.Status.SEE_OTHER
import play.api.test.Helpers.{redirectLocation, status}
import uk.gov.hmrc.plasticpackagingtax.returns.base.unit.ControllerSpec
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents

class ReturnsInformationControllerSpec extends ControllerSpec {

  private val mcc        = stubMessagesControllerComponents()
  private val controller = new ReturnsInformationController(mcc, config)

  override protected def beforeEach(): Unit =
    super.beforeEach()

  "ReturnsInformationController" should {
    "re-direct to app config pptCompleteReturnGuidanceUrl" in {

      when(config.pptCompleteReturnGuidanceUrl).thenReturn("/some-return-guidance")
      val resp = controller.displayPage()(getRequest())

      status(resp) mustBe SEE_OTHER
      redirectLocation(resp) mustBe Some("/some-return-guidance")
    }
  }
}
