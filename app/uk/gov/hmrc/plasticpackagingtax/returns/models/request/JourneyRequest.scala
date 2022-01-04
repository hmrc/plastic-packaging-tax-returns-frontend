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

package uk.gov.hmrc.plasticpackagingtax.returns.models.request

import uk.gov.hmrc.auth.core.InsufficientEnrolments
import uk.gov.hmrc.plasticpackagingtax.returns.models.domain.TaxReturn

class JourneyRequest[+A](
  val authenticatedRequest: AuthenticatedRequest[A],
  val taxReturn: TaxReturn,
  override val enrolmentId: Option[String]
) extends AuthenticatedRequest[A](authenticatedRequest, authenticatedRequest.user, enrolmentId) {

  val pptReference =
    enrolmentId.getOrElse(throw InsufficientEnrolments("Enrolment id not found on request"))

}
