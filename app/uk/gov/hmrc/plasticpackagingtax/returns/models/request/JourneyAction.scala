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

import play.api.Logger
import play.api.mvc.{ActionRefiner, Result}
import uk.gov.hmrc.auth.core.InsufficientEnrolments
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.plasticpackagingtax.returns.audit.Auditor
import uk.gov.hmrc.plasticpackagingtax.returns.connectors.TaxReturnsConnector
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import uk.gov.hmrc.plasticpackagingtax.returns.models.domain.TaxReturn

import scala.concurrent.{ExecutionContext, Future}

class JourneyAction @Inject() (returnsConnector: TaxReturnsConnector, auditor: Auditor)(implicit
  val exec: ExecutionContext
) extends ActionRefiner[AuthenticatedRequest, JourneyRequest] {

  private val logger = Logger(this.getClass)

  override protected def refine[A](
    request: AuthenticatedRequest[A]
  ): Future[Either[Result, JourneyRequest[A]]] = {
    implicit val hc: HeaderCarrier =
      HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    request.enrolmentId.filter(_.trim.nonEmpty) match {
      case Some(id) =>
        loadOrCreateReturn(id).map {
          case Right(reg)  => Right(new JourneyRequest[A](request, reg, Some(id)))
          case Left(error) => throw error
        }
      case None =>
        logger.warn(s"Enrolment not present, redirecting to Start")
        throw InsufficientEnrolments()
    }
  }

  private def loadOrCreateReturn[A](id: String)(implicit headerCarrier: HeaderCarrier) =
    returnsConnector.find(id).flatMap {
      case Right(taxReturn) =>
        taxReturn
          .map { r =>
            Future.successful(Right(r))
          }
          .getOrElse {
            auditor.newTaxReturnStarted()
            returnsConnector.create(TaxReturn(id))
          }
      case Left(error) => Future.successful(Left(error))
    }

  override protected def executionContext: ExecutionContext = exec
}
