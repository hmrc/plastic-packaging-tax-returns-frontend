package services

import connectors.SubscriptionConnector
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar
import org.mockito.MockitoSugar.{mock, verify}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import uk.gov.hmrc.http.HeaderCarrier

class SubscriptionServiceSpec extends PlaySpec with BeforeAndAfterEach {

  private val subscriptionConnector: SubscriptionConnector = mock[SubscriptionConnector]
  private val headerCarrier: HeaderCarrier = mock[HeaderCarrier]

  private val service = new SubscriptionService(subscriptionConnector)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    MockitoSugar.reset(subscriptionConnector, headerCarrier)
  }

  "SubscriptionService" should {
    "call its connector" in {
      service.fetchGroupMemberNames("ppt-ref")(headerCarrier)    
      verify(subscriptionConnector).get("ppt-ref")(headerCarrier)
    }
  }

}
