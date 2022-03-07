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

package uk.gov.hmrc.plasticpackagingtax.returns.controllers.returns

import com.codahale.metrics.{Counter, MetricRegistry}
import com.kenshoo.play.metrics.Metrics
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.mockito.{ArgumentCaptor, ArgumentMatchers, Mockito}
import org.scalatest.Inspectors.forAll
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.libs.json.JsObject
import play.api.mvc.Result
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

import scala.concurrent.Future

class CheckYourReturnControllerSpec extends ControllerSpec {

  private val mcc  = stubMessagesControllerComponents()
  private val page = mock[check_your_return_page]

  private val resettableMockMetrics = mock[Metrics]
  private val mockDefaultRegistry = mock[MetricRegistry]
  private val submissionSuccessCounter = mock[Counter]
  private val submissionFailedCounter = mock[Counter]

  {
    when(resettableMockMetrics.defaultRegistry).thenReturn(mockDefaultRegistry)
    when(mockDefaultRegistry.counter(ArgumentMatchers.eq("ppt.returns.success.submission.counter"))).thenReturn(submissionSuccessCounter)
    when(mockDefaultRegistry.counter(ArgumentMatchers.eq("ppt.returns.failed.submission.counter"))).thenReturn(submissionFailedCounter)
  }

  private val controller = new CheckYourReturnController(authenticate = mockAuthAction,
                                                         journeyAction = mockJourneyAction,
                                                         mcc = mcc,
                                                         page = page,
                                                         returnsConnector =
                                                           mockTaxReturnsConnector,
                                                         auditor = mockAuditor,
                                                         metrics = resettableMockMetrics
  )

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    when(page.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(page)
    reset(mockTaxReturnsConnector)
    reset(submissionSuccessCounter)
    reset(submissionFailedCounter)
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

    "submit return when user selects save and continue" in {
      authorizedUser()
      mockTaxReturnFind(aTaxReturn())
      mockTaxReturnUpdate(aTaxReturn())
      mockTaxReturnSubmission(aTaxReturn())

      val result = controller.submit()(postRequestEncoded(JsObject.empty, saveAndContinueFormAction))

      status(result) mustBe SEE_OTHER

      // Verify that a submit call occurred
      verify(mockTaxReturnsConnector).submit(ArgumentMatchers.eq(aTaxReturn()))(any())
    }

    "return 303 and redirect to HomeController" when {

      "tax return not completed and display page method is invoked" in {
        authorizedUser()
        val result = controller.displayPage()(getRequest())

        redirectLocation(result) mustBe Some(homeRoutes.HomeController.displayPage().url)
      }
    }

    forAll(Seq(saveAndContinueFormAction, saveAndComeBackLaterFormAction)) { formAction =>
      "return 303 (OK) for " + formAction._1 when {
        "user submits tax return" in {
          authorizedUser()
          mockTaxReturnFind(aTaxReturn())
          mockTaxReturnUpdate(aTaxReturn())
          mockTaxReturnSubmission(aTaxReturn())

          val result: Future[Result] =
            controller.submit()(postRequestEncoded(JsObject.empty, formAction))

          status(result) mustBe SEE_OTHER

          formAction match {
            case ("SaveAndContinue", "") =>
              updatedTaxReturn.metaData.returnCompleted mustBe true
              flash(result).apply(FlashKeys.referenceId) must (not be null and startWith("PPTR"))
              redirectLocation(result) mustBe Some(
                returnRoutes.ConfirmationController.displayPage().url
              )
              verify(submissionSuccessCounter).inc()
            case _ =>
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
        mockTaxReturnSubmission(aTaxReturn())
        mockTaxReturnFailure()
        val result =
          controller.submit()(postRequestEncoded(JsObject.empty, saveAndContinueFormAction))

        intercept[DownstreamServiceError](status(result))
        verify(submissionFailedCounter).inc()
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

    def updatedTaxReturn: TaxReturn = {
      val captor = ArgumentCaptor.forClass(classOf[TaxReturn])
      verify(mockTaxReturnsConnector).update(captor.capture())(any())
      captor.getValue
    }

  }

  private def mockTaxReturnSubmission(taxReturn: TaxReturn): Any = {
    when(mockTaxReturnsConnector.submit(ArgumentMatchers.eq(taxReturn))(any()))
      .thenReturn(Future.successful(Right(true)))
  }

}
