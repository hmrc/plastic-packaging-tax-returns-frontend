package controllers.payments

import base.SpecBase
import play.api.Application
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, defaultAwaitTimeout, redirectLocation, route, writeableOf_AnyContentAsEmpty}


class DirectDebitControllerSpec extends SpecBase {


  "DirectDebitController" - {
    "redirectLink" - {
      "redirect to enter email address page" in {

        val app: Application = applicationBuilder().build()

        val request = FakeRequest(GET, routes.DirectDebitController.redirectLink().url)

        val result = route(app, request).value

        redirectLocation(result) mustBe Some("/bleach")
      }
    }
  }
}
