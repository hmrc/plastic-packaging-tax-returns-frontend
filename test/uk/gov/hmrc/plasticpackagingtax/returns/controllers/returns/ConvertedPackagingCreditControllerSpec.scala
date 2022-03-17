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

import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.Inspectors.forAll
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.data.Form
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.libs.json.Json
import play.api.test.Helpers.{await, redirectLocation, status}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.plasticpackagingtax.returns.base.unit.ControllerSpec
import uk.gov.hmrc.plasticpackagingtax.returns.connectors.{
  DownstreamServiceError,
  ExportCreditsConnector
}
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.home.{routes => homeRoutes}
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.returns.{routes => returnRoutes}
import uk.gov.hmrc.plasticpackagingtax.returns.forms.ConvertedPackagingCredit
import uk.gov.hmrc.plasticpackagingtax.returns.models.exportcredits.ExportCreditBalance
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.returns.converted_packaging_credit_page
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents

import java.time.LocalDate
import scala.concurrent.Future

class ConvertedPackagingCreditControllerSpec extends ControllerSpec {

  private val mcc                        = stubMessagesControllerComponents()
  private val page                       = mock[converted_packaging_credit_page]
  private val mockExportCreditsConnector = mock[ExportCreditsConnector]

  private val balance = ExportCreditBalance(totalPPTCharges = BigDecimal("1234.56"),
                                            totalExportCreditClaimed = BigDecimal("12.34"),
                                            totalExportCreditAvailable = BigDecimal("123.45")
  )

  private val controller = new ConvertedPackagingCreditController(authenticate = mockAuthAction,
                                                                  journeyAction = mockJourneyAction,
                                                                  mcc = mcc,
                                                                  page = page,
                                                                  returnsConnector =
                                                                    mockTaxReturnsConnector,
                                                                  exportCreditsConnector =
                                                                    mockExportCreditsConnector
  )

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    when(
      page.apply(any[Form[ConvertedPackagingCredit]], any[Option[BigDecimal]], any())(any(), any())
    ).thenReturn(HtmlFormat.empty)
    when(mockExportCreditsConnector.get(anyString(), any(), any())(any())).thenReturn(
      Future.successful(Right(balance))
    )
  }

  override protected def afterEach(): Unit = {
    reset(page, mockExportCreditsConnector)
    super.afterEach()
  }

  "ConvertedPackagingCreditController" should {

    "return 200" when {

      "display page method is invoked" in {
        authorizedUser()
        val result = controller.displayPage()(getRequest())

        status(result) mustBe OK
      }

      "tax return already exists and display page method is invoked" in {
        authorizedUser()
        mockTaxReturnFind(aTaxReturn())
        val result = controller.displayPage()(getRequest())

        status(result) mustBe OK
      }

      "correct date parameters are set in export credits call" in {
        authorizedUser()
        mockTaxReturnFind(aTaxReturn())
        await(controller.displayPage()(getRequest()))

        val dateCaptor = ArgumentCaptor.forClass(classOf[LocalDate])
        verify(mockExportCreditsConnector).get(anyString(),
                                               dateCaptor.capture(),
                                               dateCaptor.capture()
        )(any())
        val dates = dateCaptor.getAllValues

        val obligationFromDate = aTaxReturn().obligation.get.fromDate
        //export credits date range spans 8 quarters before current obligation quarter
        dates.get(0).toString mustBe obligationFromDate.minusYears(2).toString
        dates.get(1).toString mustBe obligationFromDate.minusDays(1).toString

        dates.get(0).toString mustBe "2020-04-01"
        dates.get(1).toString mustBe "2022-03-31"
      }

      "display page method is invoked even when export credits call fails" in {
        authorizedUser()
        when(mockExportCreditsConnector.get(any(), any(), any())(any())).thenReturn(
          Future.successful(
            Left(DownstreamServiceError("error", new InternalServerException("error")))
          )
        )
        val result = controller.displayPage()(getRequest())

        status(result) mustBe OK
      }
    }

    forAll(Seq(saveAndContinueFormAction, saveAndComeBackLaterFormAction)) { formAction =>
      "return 303 (OK) for " + formAction._1 when {
        "user submits the amount of credit" in {
          authorizedUser()
          mockTaxReturnFind(aTaxReturn())
          mockTaxReturnUpdate(aTaxReturn())

          val result =
            controller.submit()(
              postRequestEncoded(ConvertedPackagingCredit(totalInPounds = "1020"), formAction)
            )

          status(result) mustBe SEE_OTHER
          modifiedTaxReturn.convertedPackagingCredit.get.totalInPounds mustBe 1020
          formAction match {
            case ("SaveAndContinue", "") =>
              redirectLocation(result) mustBe Some(
                returnRoutes.CheckYourReturnController.displayPage().url
              )
            case _ =>
              redirectLocation(result) mustBe Some(homeRoutes.HomeController.displayPage().url)
          }
          reset(mockTaxReturnsConnector)
        }
      }
    }

    "return prepopulated form" when {

      def pageForm: Form[ConvertedPackagingCredit] = {
        val captor = ArgumentCaptor.forClass(classOf[Form[ConvertedPackagingCredit]])
        verify(page).apply(captor.capture(), any(), any())(any(), any())
        captor.getValue
      }

      "data exist" in {
        authorizedUser()
        mockTaxReturnFind(aTaxReturn(withConvertedPackagingCredit(totalInPounds = 5500)))

        await(controller.displayPage()(getRequest()))

        pageForm.get.totalInPounds mustBe "5500.00"
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "user submits invalid job title" in {
        authorizedUser()
        val result =
          controller.submit()(
            postRequest(Json.toJson(ConvertedPackagingCredit(totalInPounds = "")))
          )

        status(result) mustBe BAD_REQUEST
      }
    }

    "return an error" when {

      "user submits form and the tax return update fails" in {
        authorizedUser()
        mockTaxReturnFailure()
        val result =
          controller.submit()(
            postRequest(Json.toJson(ConvertedPackagingCredit(totalInPounds = "10.20")))
          )

        intercept[DownstreamServiceError](status(result))
      }

      "user submits form and a tax return update runtime exception occurs" in {
        authorizedUser()
        mockTaxReturnException()
        val result =
          controller.submit()(
            postRequest(Json.toJson(ConvertedPackagingCredit(totalInPounds = "10.20")))
          )

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
