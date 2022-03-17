package uk.gov.hmrc.plasticpackagingtax.returns.controllers.bta

import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.test.Helpers.{await, redirectLocation}
import uk.gov.hmrc.plasticpackagingtax.returns.base.unit.ControllerSpec
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.returns.{routes => returnRoutes}
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents

class BtaEntryPointControllerSpec extends ControllerSpec {

  private val mcc = stubMessagesControllerComponents()

  private val controller = new BtaEntryPointController(authenticate = mockAuthAction, mcc = mcc)

  "The BTA Entry Point Controller" should {
    "redirect to the create tax return start page" when {
      "user is authenticated" in {
        authorizedUser()

        redirectLocation(controller.startReturn()(getRequest())) mustBe Some(
          returnRoutes.StartDateReturnController.displayPage().url
        )
      }
    }
    "throw an exception" when {
      "user is unauthenticated" in {
        unAuthorizedUser()

        intercept[RuntimeException](await(controller.startReturn()(getRequest())))
      }
    }
  }
}
