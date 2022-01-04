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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.Inspectors.forAll
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.data.Form
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.libs.json.Json
import play.api.test.Helpers.{await, redirectLocation, status}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.plasticpackagingtax.returns.base.unit.ControllerSpec
import uk.gov.hmrc.plasticpackagingtax.returns.connectors.DownstreamServiceError
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.home.{routes => homeRoutes}
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.returns.{routes => returnRoutes}
import uk.gov.hmrc.plasticpackagingtax.returns.forms.ImportedPlasticWeight
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.returns.imported_plastic_weight_page
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents

class ImportedPlasticWeightControllerSpec extends ControllerSpec {

  private val mcc  = stubMessagesControllerComponents()
  private val page = mock[imported_plastic_weight_page]

  private val controller = new ImportedPlasticWeightController(authenticate = mockAuthAction,
                                                               journeyAction = mockJourneyAction,
                                                               mcc = mcc,
                                                               page = page,
                                                               returnsConnector =
                                                                 mockTaxReturnsConnector
  )

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    when(page.apply(any[Form[ImportedPlasticWeight]])(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(page)
    super.afterEach()
  }

  "ImportedPlasticWeightController" should {

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
    }

    forAll(Seq(saveAndContinueFormAction, saveAndComeBackLaterFormAction)) { formAction =>
      "return 303 (OK) for " + formAction._1 when {
        "user submits the weight details" in {
          authorizedUser()
          mockTaxReturnFind(aTaxReturn())
          mockTaxReturnUpdate(aTaxReturn())

          val result =
            controller.submit()(
              postRequestEncoded(ImportedPlasticWeight(totalKg = "10"), formAction)
            )

          status(result) mustBe SEE_OTHER
          modifiedTaxReturn.importedPlasticWeight.get.totalKg mustBe 10
          formAction match {
            case ("SaveAndContinue", "") =>
              redirectLocation(result) mustBe Some(
                returnRoutes.HumanMedicinesPlasticWeightController.displayPage().url
              )
            case _ =>
              redirectLocation(result) mustBe Some(homeRoutes.HomeController.displayPage().url)
          }
          reset(mockTaxReturnsConnector)
        }
      }
    }

    "return prepopulated form" when {

      def pageForm: Form[ImportedPlasticWeight] = {
        val captor = ArgumentCaptor.forClass(classOf[Form[ImportedPlasticWeight]])
        verify(page).apply(captor.capture())(any(), any())
        captor.getValue
      }

      "data exist" in {
        authorizedUser()
        mockTaxReturnFind(aTaxReturn(withImportedPlasticWeight(totalKg = 10)))

        await(controller.displayPage()(getRequest()))

        pageForm.get.totalKg mustBe "10"

      }
    }

    "return 400 (BAD_REQUEST)" when {

      "user submits invalid imported plastic weight" in {
        authorizedUser()
        val result =
          controller.submit()(postRequest(Json.toJson(ImportedPlasticWeight(totalKg = ""))))

        status(result) mustBe BAD_REQUEST
      }
    }

    "return an error" when {

      "user submits form and the tax return update fails" in {
        authorizedUser()
        mockTaxReturnFailure()
        val result =
          controller.submit()(postRequest(Json.toJson(ImportedPlasticWeight(totalKg = "5"))))

        intercept[DownstreamServiceError](status(result))
      }

      "user submits form and a tax return update runtime exception occurs" in {
        authorizedUser()
        mockTaxReturnException()
        val result =
          controller.submit()(postRequest(Json.toJson(ImportedPlasticWeight(totalKg = "5"))))

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
