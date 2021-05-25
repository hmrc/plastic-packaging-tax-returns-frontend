/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.plasticpackagingtax.returns.controllers.returns

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.mockito.{ArgumentCaptor, ArgumentMatchers, Mockito}
import org.scalatest.Inspectors.forAll
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.libs.json.JsObject
import play.api.test.Helpers.{await, flash, redirectLocation, status}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.plasticpackagingtax.returns.base.unit.ControllerSpec
import uk.gov.hmrc.plasticpackagingtax.returns.connectors.DownstreamServiceError
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.home.{routes => homeRoutes}
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.returns.{routes => returnRoutes}
import uk.gov.hmrc.plasticpackagingtax.returns.models.domain.TaxReturn
import uk.gov.hmrc.plasticpackagingtax.returns.models.response.FlashKeys
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.returns.check_your_return_page
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents

class CheckYourReturnControllerSpec extends ControllerSpec {

  private val mcc  = stubMessagesControllerComponents()
  private val page = mock[check_your_return_page]

  private val controller = new CheckYourReturnController(authenticate = mockAuthAction,
                                                         journeyAction = mockJourneyAction,
                                                         mcc = mcc,
                                                         page = page,
                                                         returnsConnector =
                                                           mockTaxReturnsConnector,
                                                         auditor = mockAuditor,
                                                         metrics = metricsMock
  )

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    when(page.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(page)
    reset(mockTaxReturnsConnector)
    super.afterEach()
  }

  "CheckYourReturnController" should {

    "return 200" when {

      "tax return completed and display page method is invoked" in {
        authorizedUser()
        mockTaxReturnFind(
          aTaxReturn(withConvertedPackagingCredit(5),
                     withDirectExportDetails(2),
                     withHumanMedicinesPlasticWeight(4),
                     withImportedPlasticWeight(2),
                     withManufacturedPlasticWeight(7),
                     withRecycledPlasticWeight(3)
          )
        )

        val result = controller.displayPage()(getRequest())

        status(result) mustBe OK
      }
    }

    "return 303 and redirect to HomeController" when {

      "tax return not completed and display page method is invoked" in {
        authorizedUser()
        val result = controller.displayPage()(getRequest())

        redirectLocation(result) mustBe Some(homeRoutes.HomeController.displayPage().url)
      }
    }

    def updatedTaxReturn: TaxReturn = {
      val captor = ArgumentCaptor.forClass(classOf[TaxReturn])
      verify(mockTaxReturnsConnector).update(captor.capture())(any())
      captor.getValue
    }

    forAll(Seq(saveAndContinueFormAction, saveAndComeBackLaterFormAction)) { formAction =>
      "return 303 (OK) for " + formAction._1 when {
        "user submits tax return" in {
          authorizedUser()
          mockTaxReturnFind(aTaxReturn())
          mockTaxReturnUpdate(aTaxReturn())

          val result =
            controller.submit()(postRequestEncoded(JsObject.empty, formAction))

          status(result) mustBe SEE_OTHER
          formAction match {
            case ("SaveAndContinue", "") =>
              metricsMock.defaultRegistry.counter(
                "ppt.returns.success.submission.counter"
              ).getCount mustBe 1
              updatedTaxReturn.metaData.returnCompleted mustBe true
              flash(result).apply(FlashKeys.referenceId) must (not be null and startWith("PPTR"))
              redirectLocation(result) mustBe Some(
                returnRoutes.ConfirmationController.displayPage().url
              )
            case _ =>
              metricsMock.defaultRegistry.counter(
                "ppt.returns.success.submission.counter"
              ).getCount mustBe 1
              redirectLocation(result) mustBe Some(homeRoutes.HomeController.displayPage().url)
          }
          reset(mockTaxReturnsConnector)
        }
      }
    }

    "send audit event" when {
      "submission of audit event" in {
        authorizedUser()
        val taxReturn = aTaxReturn()
        mockTaxReturnFind(taxReturn)
        mockTaxReturnUpdate(taxReturn)

        await(controller.submit()(postRequest(JsObject.empty)))

        verify(mockAuditor, Mockito.atLeast(1)).auditTaxReturn(ArgumentMatchers.eq(taxReturn))(
          any(),
          any()
        )
      }
    }

    "return an error" when {

      "user submits the tax return and update fails" in {
        authorizedUser()
        mockTaxReturnFailure()
        val result =
          controller.submit()(postRequestEncoded(JsObject.empty, saveAndContinueFormAction))

        intercept[DownstreamServiceError](status(result))
        metricsMock.defaultRegistry.counter(
          "ppt.returns.failed.submission.counter"
        ).getCount mustBe 1
      }

      "user submits the tax return and runtime exception occurs" in {
        authorizedUser()
        mockTaxReturnException()
        val result =
          controller.submit()(postRequestEncoded(JsObject.empty, saveAndContinueFormAction))

        intercept[RuntimeException](status(result))
      }

      "user is not authorised" in {
        unAuthorizedUser()
        val result = controller.displayPage()(getRequest())

        intercept[RuntimeException](status(result))
      }
    }
  }
}
