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
import connectors.{DownstreamServiceError, SubscriptionConnector}
import models.PPTSubscriptionDetails
import models.requests.IdentifiedRequest
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionFilter, Result}
import repositories.SessionRepository
import repositories.SessionRepository.Paths.SubscriptionIsActive
import uk.gov.hmrc.http.ServiceUnavailableException
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import util.PurplePrint.purplePrint

import scala.concurrent.{ExecutionContext, Future}

class SubscriptionFilter @Inject()(
                                    subscriptionConnector: SubscriptionConnector,
                                    sessionRepository: SessionRepository
                                  )(implicit val executionContext: ExecutionContext)
  extends ActionFilter[IdentifiedRequest] with HeaderCarrierConverter {

  override def filter[A](request: IdentifiedRequest[A]): Future[Option[Result]] = {
    sessionRepository.get[PPTSubscriptionDetails](request.cacheKey, SubscriptionIsActive).flatMap{
      case Some(_) => Future.successful(None)
      case _ =>
        subscriptionConnector.get(request.pptReference)(fromRequestAndSession(request, request.session)).flatMap{
          case Right(subscription) => sessionRepository
            .set(request.cacheKey, SubscriptionIsActive, PPTSubscriptionDetails(subscription.legalEntityDetails))
            .map(_ => None)
          case Left(eisFailure) if eisFailure.isDeregistered =>
            Future.successful(Some(Redirect(controllers.routes.DeregisteredController.onPageLoad())))
          case Left(eisFailure) if eisFailure.isDependentSystemsNotResponding =>
            throw DownstreamServiceError("Dependent systems are currently not responding.", new ServiceUnavailableException(eisFailure.failures.toString))
          case Left(eisFailure) =>
            throw new RuntimeException(
              s"Failed to get subscription - ${eisFailure.failures.map(_.headOption.map(_.reason))
                .getOrElse("no underlying reason supplied")}"
            )
        }
    }
  }

}
