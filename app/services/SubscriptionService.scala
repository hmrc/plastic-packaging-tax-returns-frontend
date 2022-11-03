package services

import connectors.SubscriptionConnector
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject

class SubscriptionService @Inject() (
  subscriptionConnector: SubscriptionConnector
) {

  def fetchGroupMemberNames(pptReferenceNumber: String) (implicit hc: HeaderCarrier) = {
    subscriptionConnector.get(pptReferenceNumber)
  }
  
}
