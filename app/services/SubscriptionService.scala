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

package services

import connectors.SubscriptionConnector
import models.EisFailure
import models.subscription.GroupMembers
import models.subscription.subscriptionDisplay.SubscriptionDisplayResponse
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SubscriptionService @Inject() (
  subscriptionConnector: SubscriptionConnector
) (implicit ec: ExecutionContext) {

  def fetchGroupMemberNames(pptReferenceNumber: String) (implicit hc: HeaderCarrier): Future[GroupMembers] = {
    subscriptionConnector
      .get(pptReferenceNumber)
      .map(extractNames)
  }

  private def extractNames(connectorResponse: Either[EisFailure, SubscriptionDisplayResponse]): GroupMembers = {
    connectorResponse.fold(
      eisFailure => throw new RuntimeException("Subscription connector failed"),
      GroupMembers.create
    )
  }

}
