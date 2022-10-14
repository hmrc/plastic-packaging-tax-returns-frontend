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
import controllers.actions.AuthenticatedIdentifierAction.IdentifierAction.{pptEnrolmentIdentifierName, pptEnrolmentKey}
import models.SignedInUser
import models.requests.{IdentifiedRequest, IdentityData}
import play.api.mvc._
import uk.gov.hmrc.auth.core.{Enrolment, Enrolments}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FakeIdentifierActionWithEnrolment @Inject() (bodyParsers: PlayBodyParsers)
    extends IdentifierAction {

  override def invokeBlock[A](
    request: Request[A],
    block: IdentifiedRequest[A] => Future[Result]
  ): Future[Result] = {
    val pptLoggedInUser = SignedInUser(pptEnrolment("123"), IdentityData(internalId = "SomeId"))
    block(IdentifiedRequest(request, pptLoggedInUser, Some("123")))
  }

  override def parser: BodyParser[AnyContent] =
    bodyParsers.default

  override protected def executionContext: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global

  def pptEnrolment(pptEnrolmentId: String) =
    newEnrolments(newEnrolment(pptEnrolmentKey, pptEnrolmentIdentifierName, pptEnrolmentId))

  def newEnrolments(enrolment: Enrolment*): Enrolments =
    Enrolments(enrolment.toSet)

  def newEnrolment(key: String, identifierName: String, identifierValue: String): Enrolment =
    Enrolment(key).withIdentifier(identifierName, identifierValue)

}
