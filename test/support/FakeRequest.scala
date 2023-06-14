/*
 * Copyright 2023 HM Revenue & Customs
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

package support

import controllers.actions.AuthenticatedIdentifierAction.IdentifierAction
import models.SignedInUser
import models.requests.IdentifiedRequest
import play.api.mvc.{AnyContentAsEmpty, Headers}
import play.api.test.FakeRequest
import support.PptTestData.pptEnrolment


//todo delete?
trait FakeCustomRequest {

  def authRequest(
    headers: Headers = Headers(),
    user: SignedInUser = PptTestData.newUser("123", Some(pptEnrolment("333"))),
    pptClient: Option[String] = None
  ): IdentifiedRequest[AnyContentAsEmpty.type] = {
    val request = pptClient.map { clientIdentifier =>
      FakeRequest("GET", "login-continue-url").withHeaders(headers).withSession(("clientPPT", clientIdentifier))
    }.getOrElse {
      FakeRequest("GET", "login-continue-url").withHeaders(headers)
    }

    IdentifiedRequest(request,
                      user,
                      user.enrolments.getEnrolment(IdentifierAction.pptEnrolmentKey).flatMap(
                        e =>
                          e.getIdentifier(IdentifierAction.pptEnrolmentIdentifierName).map(
                            i => i.value
                          )
                      ).get
    )
  }

}
