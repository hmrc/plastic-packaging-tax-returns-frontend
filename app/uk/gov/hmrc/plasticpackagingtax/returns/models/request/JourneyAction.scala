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
import uk.gov.hmrc.plasticpackagingtax.returns.connectors.{
  ObligationsConnector,
  ServiceError,
  TaxReturnsConnector
}
import uk.gov.hmrc.plasticpackagingtax.returns.models.domain.TaxReturn
import uk.gov.hmrc.plasticpackagingtax.returns.models.obligations.{Obligation, PPTObligations}
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class JourneyAction @Inject() (
  returnsConnector: TaxReturnsConnector,
  auditor: Auditor,
  obligationsConnector: ObligationsConnector
)(implicit val exec: ExecutionContext)
    extends ActionRefiner[AuthenticatedRequest, JourneyRequest] {

  private val logger = Logger(this.getClass)

  override protected def refine[A](
    request: AuthenticatedRequest[A]
  ): Future[Either[Result, JourneyRequest[A]]] = {
    implicit val hc: HeaderCarrier =
      HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    request.enrolmentId.filter(_.trim.nonEmpty) match {
      case Some(pptReference) =>
        loadOrCreateReturn(pptReference).map {
          case Right(reg)  => Right(new JourneyRequest[A](request, reg, Some(pptReference)))
          case Left(error) => throw error
        }
      case None =>
        logger.warn(s"Enrolment not present, redirecting to Start")
        throw InsufficientEnrolments()
    }
  }

  private def loadOrCreateReturn[A](
    pptReference: String
  )(implicit headerCarrier: HeaderCarrier): Future[Either[ServiceError, TaxReturn]] = {
    val futureReturn: Future[Either[ServiceError, Option[TaxReturn]]] =
      returnsConnector.find(pptReference)
    val futureObligation: Future[PPTObligations] = obligationsConnector.get(pptReference)

    (for {
      eitherTaxReturn <- futureReturn
      openObligations <- futureObligation
    } yield {
      val oldestObligation: Option[Obligation] = openObligations.nextObligationToReturn

      eitherTaxReturn match {
        case Right(Some(taxReturn)) =>
          Future.successful(Right(ensureObligationDetailPresent(taxReturn, oldestObligation)))
        case Right(None) =>
          auditor.newTaxReturnStarted()
          returnsConnector.create(TaxReturn(id = pptReference, obligation = oldestObligation))
        case Left(error) => Future.successful(Left(error))
      }
    }).flatten
  }

  // This is necessary since we may have in-flight production tax returns without obligation details
  private def ensureObligationDetailPresent(
    taxReturn: TaxReturn,
    oldestObligation: Option[Obligation]
  ): TaxReturn =
    if (taxReturn.obligation.isEmpty)
      taxReturn.copy(obligation = oldestObligation)
    else
      taxReturn

  override protected def executionContext: ExecutionContext = exec
}
