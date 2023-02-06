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

package controllers.actions

import com.google.inject.Inject
import models.requests.IdentifiedRequest
import play.api.mvc._
import uk.gov.hmrc.auth.core.authorise.EmptyPredicate

import scala.concurrent.{ExecutionContext, Future}

//todo badly named... sign out action
class AuthCheckActionImpl @Inject() (authorisedFun: AuthFunction, mcc: MessagesControllerComponents)
    extends AuthCheckAction {

  implicit override val executionContext: ExecutionContext = mcc.executionContext
  override val parser: BodyParser[AnyContent]              = mcc.parsers.defaultBodyParser

  override def invokeBlock[A](
    request: Request[A],
    block: AuthedUser[A] => Future[Result]
  ): Future[Result] =
    authorisedFun.authorised(EmptyPredicate, request, block) // these are used for sign out let anyone do that!!!!

}

trait AuthCheckAction
    extends ActionBuilder[AuthedUser, AnyContent]
    with ActionFunction[Request, AuthedUser]
