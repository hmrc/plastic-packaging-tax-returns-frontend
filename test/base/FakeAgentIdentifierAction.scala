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

package base

import controllers.actions.IdentifierAction
import models.SignedInUser
import models.requests.{IdentifiedRequest, IdentityData}
import play.api.mvc._
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FakeAgentIdentifierAction @Inject()(bodyParsers: PlayBodyParsers) extends IdentifierAction {

  override def invokeBlock[A](
    request: Request[A],
    block: IdentifiedRequest[A] => Future[Result]
  ): Future[Result] = {
    val pptLoggedInUser = SignedInUser(Enrolments(Set.empty), IdentityData(internalId = "SomeId", affinityGroup = Some(AffinityGroup.Agent)))
    block(IdentifiedRequest(request, pptLoggedInUser, None))
  }

  override def parser: BodyParser[AnyContent] =
    bodyParsers.default

  override protected def executionContext: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global

}
