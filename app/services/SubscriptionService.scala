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
      subscriptionConnector => GroupMembers(Seq()))
        
  }
  
}
