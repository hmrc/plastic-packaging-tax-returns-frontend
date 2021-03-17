/*
 * Copyright 2021 HM Revenue & Customs
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

import play.api.mvc.{ActionRefiner, Request, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.plasticpackagingtax.returns.connectors.TaxReturnsConnector
import uk.gov.hmrc.plasticpackagingtax.returns.models.domain.TaxReturn
import uk.gov.hmrc.plasticpackagingtax.returns.models.request.JourneyAction.tempTaxReturnId
import uk.gov.hmrc.play.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class JourneyAction @Inject() (returnsConnector: TaxReturnsConnector)(implicit
  val exec: ExecutionContext
) extends ActionRefiner[Request, JourneyRequest] {

  override protected def refine[A](
    request: Request[A]
  ): Future[Either[Result, JourneyRequest[A]]] = {
    implicit val hc: HeaderCarrier =
      HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))
    loadOrCreateReturn(tempTaxReturnId).map {
      case Right(reg)  => Right(new JourneyRequest[A](request, reg))
      case Left(error) => throw error
    }
  }

  private def loadOrCreateReturn[A](id: String)(implicit headerCarrier: HeaderCarrier) =
    returnsConnector.find(id).flatMap {
      case Right(taxReturn) =>
        taxReturn
          .map(r => Future.successful(Right(r)))
          .getOrElse(returnsConnector.create(TaxReturn(id)))
      case Left(error) => Future.successful(Left(error))
    }

  override protected def executionContext: ExecutionContext = exec
}

object JourneyAction {
  val tempTaxReturnId = "123"
}
