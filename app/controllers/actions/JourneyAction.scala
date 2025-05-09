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

import controllers.actions.JourneyAction.{RequestAsyncFunction, RequestFunction}
import models.requests.DataRequest
import play.api.mvc._

import javax.inject.Inject
import scala.concurrent.Future

class JourneyAction @Inject() (
  identifierAction: IdentifierAction,
  dataRetrievalAction: DataRetrievalAction,
  dataAction: DataNotRequiredAction
) {

  def async(function: RequestAsyncFunction): Action[AnyContent] =
    build.async(function)

  def apply(function: RequestFunction): Action[AnyContent] =
    build.apply(function)

  private def build: ActionBuilder[DataRequest, AnyContent] =
    identifierAction.andThen(dataRetrievalAction.andThen(dataAction))
}

object JourneyAction {
  type RequestFunction      = DataRequest[AnyContent] => Result
  type RequestAsyncFunction = DataRequest[AnyContent] => Future[Result]
}
