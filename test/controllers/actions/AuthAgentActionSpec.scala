/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.actions

import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar.{mock, verify, when}
import org.scalatestplus.play.PlaySpec
import play.api.mvc.Result
import play.api.mvc.Results.{ImATeapot, Ok}
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, defaultAwaitTimeout, stubMessagesControllerComponents}
import uk.gov.hmrc.auth.core.AffinityGroup

import scala.concurrent.Future

class AuthAgentActionSpec extends PlaySpec {

  val authFun: AuthFunction = mock[AuthFunction]

  val sut = new AuthAgentActionImpl(
    authFun,
    stubMessagesControllerComponents()
  )

  "invoke block" must {
    "proxy to AuthFunction set to Agents" in {
      when(authFun.authorised(any, any, any)).thenReturn(Future.successful(ImATeapot("")))

      val request                                  = FakeRequest("GET", "/foo")
      val block: AuthedUser[Any] => Future[Result] = _ => Future.successful(Ok("test"))

      await(sut.invokeBlock(request, block))

      verify(authFun).authorised(AffinityGroup.Agent, request, block)
    }
  }
}
