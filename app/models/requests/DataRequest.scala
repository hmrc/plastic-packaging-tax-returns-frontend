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

package models.requests

import play.api.mvc.WrappedRequest
import models.UserAnswers
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

case class OptionalDataRequest[A] (
  request: IdentifiedRequest[A],
  answers: Option[UserAnswers]
) extends WrappedRequest[A](request) {

  def pptReference: String     = request.pptReference
  def cacheKey: String         = request.cacheKey
  def userAnswers: UserAnswers = answers.getOrElse(UserAnswers(request.cacheKey))

}

case class DataRequest[A](
  request: IdentifiedRequest[A],
  userAnswers: UserAnswers
) extends WrappedRequest[A](request) {

  def pptReference: String = request.pptReference
  def cacheKey: String     = request.cacheKey

  def headerCarrier: HeaderCarrier = 
    HeaderCarrierConverter.fromRequestAndSession(request, request.session)

}

