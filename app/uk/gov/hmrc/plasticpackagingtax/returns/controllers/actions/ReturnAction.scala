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

package uk.gov.hmrc.plasticpackagingtax.returns.controllers.actions

import com.google.inject.Inject
import play.api.mvc.{ActionTransformer, WrappedRequest}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.plasticpackagingtax.returns.connectors.ObligationsConnector
import uk.gov.hmrc.plasticpackagingtax.returns.models.obligations.Obligation
import uk.gov.hmrc.plasticpackagingtax.returns.models.request.JourneyRequest
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

final case class OpenObligationsRequest[+A](
  nextObligationToPay: Obligation,
  request: JourneyRequest[A]
) extends WrappedRequest[A](request)

class ReturnAction @Inject() (obligations: ObligationsConnector)(implicit
  override val executionContext: ExecutionContext
) extends ActionTransformer[JourneyRequest, OpenObligationsRequest] {

  override protected def transform[A](
    request: JourneyRequest[A]
  ): Future[OpenObligationsRequest[A]] = {
    implicit val hc: HeaderCarrier =
      HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    obligations.get(request.pptReference).map { obligations =>
      val nextReturnToPay = obligations.nextObligationToPay.getOrElse(
        throw new IllegalStateException("No open obligations that need returned.")
      )
      OpenObligationsRequest(nextReturnToPay, request)
    }
  }

}
