package services

import connectors.SubscriptionConnector
import models.subscription.GroupMembers
import models.subscription.subscriptionDisplay.SubscriptionDisplayResponse
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar
import org.mockito.MockitoSugar.{mock, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SubscriptionServiceSpec extends PlaySpec with BeforeAndAfterEach {

  private val subscriptionConnector: SubscriptionConnector = mock[SubscriptionConnector]
  private val headerCarrier: HeaderCarrier = mock[HeaderCarrier]
  private val subscriptionDisplayResponse: SubscriptionDisplayResponse = mock[SubscriptionDisplayResponse]

  private val service = new SubscriptionService(subscriptionConnector)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    MockitoSugar.reset(subscriptionConnector, headerCarrier)
    when(subscriptionConnector.get(any)(any)) thenReturn Future.successful(Right(subscriptionDisplayResponse))
  }


  "SubscriptionService" should {
    
    "call its connector" in {
      service.fetchGroupMemberNames("ppt-ref")(headerCarrier)    
      verify(subscriptionConnector).get("ppt-ref")(headerCarrier)
    }
    
    "extract the group member names" in {
      await(service.fetchGroupMemberNames("ppt-ref")(headerCarrier)) mustBe GroupMembers(Seq())
    }
    
  }

}
