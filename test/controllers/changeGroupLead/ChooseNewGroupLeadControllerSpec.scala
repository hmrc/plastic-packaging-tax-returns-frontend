package controllers.changeGroupLead

import config.FrontendAppConfig
import connectors.CacheConnector
import controllers.actions.JourneyAction
import forms.changeGroupLead.SelectNewGroupLeadForm
import models.requests.DataRequest
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar.{mock, reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SubscriptionService
import views.html.changeGroupLead.ChooseNewGroupLeadView

import scala.concurrent.ExecutionContext.global

class ChooseNewGroupLeadControllerSpec extends PlaySpec with BeforeAndAfterEach {

  private val mockMessagesApi: MessagesApi = mock[MessagesApi]
  private val controllerComponents = stubMessagesControllerComponents()
  private val mockAppConfig = mock[FrontendAppConfig]
  private val mockView = mock[ChooseNewGroupLeadView]
  private val mockFormProvider = mock[SelectNewGroupLeadForm]
  private val mockCache = mock[CacheConnector]
  private val mockSubscriptionService = mock[SubscriptionService]
  private val journeyAction = mock[JourneyAction]
  private val featureGuard = mock[FeatureGuard]
  private val dataRequest = mock[DataRequest[AnyContent]]

  val sut = new ChooseNewGroupLeadController(
    mockMessagesApi,
    journeyAction,
    controllerComponents,
    mockView,
    mockFormProvider,
    mockCache,
    featureGuard,
    mockSubscriptionService
  )(global)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(
      mockMessagesApi,
      journeyAction,
      mockView,
      mockFormProvider,
      mockCache,
      mockSubscriptionService,
      featureGuard
    )
  }


  //todo pan wip
  "onPageLoad" must {
    
    "invoke the journey action" in {
      when(journeyAction.async(any)) thenReturn mock[Action[AnyContent]]
      sut.onPageLoad()(FakeRequest())
      verify(journeyAction).async(any)
    }
    
//    "invoke feature guard" in {
//      when(journeyAction.async(any)) thenInvokeBlockWith dataRequest
//      sut.onPageLoad()(FakeRequest())
//      verify(featureGuard).check()
//    }
    
//    "feature guard" in {
//      //todo
//    }
//
//    "return the view with the form populated with the returned members" in {
//      when(mockView.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.raw("test view"))
//
//      val result: Future[Result] = sut.onPageLoad()(FakeRequest())
//      status(result) mustBe OK
//      contentAsString(result) mustBe "test view"
//    }
  }

  "for reasons it" should {
    "do stuff" in {
    }
  } 

}
