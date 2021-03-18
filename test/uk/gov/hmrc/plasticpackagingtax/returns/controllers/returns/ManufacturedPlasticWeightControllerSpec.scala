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

import controllers.Assets
import controllers.Assets.{BAD_REQUEST, OK}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.Inspectors.forAll
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.data.Form
import play.api.libs.json.Json
import play.api.test.Helpers.{await, redirectLocation, status}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.plasticpackagingtax.returns.base.unit.ControllerSpec
import uk.gov.hmrc.plasticpackagingtax.returns.connectors.DownstreamServiceError
import uk.gov.hmrc.plasticpackagingtax.returns.forms.ManufacturedPlasticWeight
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.returns.manufactured_plastic_weight_page
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.home.{routes => homeRoutes}

class ManufacturedPlasticWeightControllerSpec extends ControllerSpec {

  private val mcc  = stubMessagesControllerComponents()
  private val page = mock[manufactured_plastic_weight_page]

  private val controller = new ManufacturedPlasticWeightController(
    authenticate = mockAuthAction,
    journeyAction = mockJourneyAction,
    mcc = mcc,
    page = page,
    returnsConnector = mockTaxReturnsConnector
  )

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    when(page.apply(any[Form[ManufacturedPlasticWeight]])(any(), any())).thenReturn(
      HtmlFormat.empty
    )
  }

  override protected def afterEach(): Unit = {
    reset(page)
    super.afterEach()
  }

  "ManufacturedPlasticWeightController" should {

    "return 200" when {

      "display page method is invoked" in {
        authorizedUser()

        val result = controller.displayPage()(getRequest())

        status(result) mustBe OK
      }

      "tax return already exists and display page method is invoked" in {
        mockTaxReturnFind(aTaxReturn())
        authorizedUser()

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
              postRequestEncoded(
                ManufacturedPlasticWeight(totalKg = Some("10"), totalKgBelowThreshold = Some("5")),
                formAction
              )
            )

          status(result) mustBe Assets.SEE_OTHER
          modifiedTaxReturn.manufacturedPlasticWeight.totalKg mustBe Some(10)
          modifiedTaxReturn.manufacturedPlasticWeight.totalKgBelowThreshold mustBe Some(5)
          formAction match {
            case ("SaveAndContinue", "") =>
              redirectLocation(result) mustBe Some(homeRoutes.HomeController.displayPage().url)
            case _ =>
              redirectLocation(result) mustBe Some(homeRoutes.HomeController.displayPage().url)
          }
          reset(mockTaxReturnsConnector)
        }
      }
    }

    "return prepopulated form" when {

      def pageForm: Form[ManufacturedPlasticWeight] = {
        val captor = ArgumentCaptor.forClass(classOf[Form[ManufacturedPlasticWeight]])
        verify(page).apply(captor.capture())(any(), any())
        captor.getValue
      }

      "data exist" in {
        authorizedUser()
        mockTaxReturnFind(
          aTaxReturn(
            withManufacturedPlasticWeight(totalKg = Some(10), totalKgBelowThreshold = Some(5))
          )
        )

        await(controller.displayPage()(getRequest()))

        pageForm.get.totalKg mustBe Some("10")
        pageForm.get.totalKgBelowThreshold mustBe Some("5")

      }
    }

    "return 400 (BAD_REQUEST)" when {

      "user submits invalid job title" in {
        authorizedUser()
        val result =
          controller.submit()(
            postRequest(
              Json.toJson(
                ManufacturedPlasticWeight(totalKg = Some("0"), totalKgBelowThreshold = None)
              )
            )
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
            postRequest(
              Json.toJson(
                ManufacturedPlasticWeight(totalKg = Some("5"), totalKgBelowThreshold = Some("5"))
              )
            )
          )

        intercept[DownstreamServiceError](status(result))
      }

      "user submits form and a tax return update runtime exception occurs" in {
        authorizedUser()
        mockTaxReturnException()
        val result =
          controller.submit()(
            postRequest(
              Json.toJson(
                ManufacturedPlasticWeight(totalKg = Some("5"), totalKgBelowThreshold = Some("5"))
              )
            )
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
