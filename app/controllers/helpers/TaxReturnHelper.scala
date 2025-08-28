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

package controllers.helpers

import connectors.{ObligationsConnector, TaxReturnsConnector}
import models.returns._
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TaxReturnHelper @Inject() (
  returnsConnector: TaxReturnsConnector,
  obligationsConnector: ObligationsConnector
)(implicit ec: ExecutionContext) {

  def nextOpenObligationAndIfFirst(
    pptId: String
  )(implicit hc: HeaderCarrier): Future[Option[(TaxReturnObligation, Boolean)]] = {
    obligationsConnector.getOpen(pptId) flatMap { obligations =>
      obligations.nextObligationToReturn.fold[Future[Option[(TaxReturnObligation, Boolean)]]](
        Future.successful(None)
      )(nextObligation =>
        obligationsConnector.getFulfilled(pptId).map { fulfilledObs =>
          Some((nextObligation, fulfilledObs.isEmpty))
        }
      )
    }
  }

  def getObligation(pptId: String, periodKey: String)(implicit
    hc: HeaderCarrier
  ): Future[Option[TaxReturnObligation]] = {
    obligationsConnector.getFulfilled(pptId)
      .map(obligations => obligations.filter(o => o.periodKey == periodKey))
      .map {
        case Seq(obligation) => Some(obligation)
        case Nil             => None
        case sequence =>
          throw new IllegalStateException(s"Expected one obligation for '$periodKey', got ${sequence.length}")
      }
  }

  def fetchTaxReturn(userId: String, periodKey: String)(implicit hc: HeaderCarrier): Future[SubmittedReturn] =
    returnsConnector.get(userId, periodKey)

}
