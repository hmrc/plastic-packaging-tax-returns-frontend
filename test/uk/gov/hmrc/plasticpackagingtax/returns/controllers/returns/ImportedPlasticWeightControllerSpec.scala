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
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Result
import play.api.test.Helpers.{await, redirectLocation, status}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.plasticpackagingtax.returns.base.unit.ControllerSpec
import uk.gov.hmrc.plasticpackagingtax.returns.connectors.DownstreamServiceError
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.home.{routes => homeRoutes}
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.returns.{routes => returnRoutes}
import uk.gov.hmrc.plasticpackagingtax.returns.forms.{ImportedPlastic, ImportedPlasticWeight}
import uk.gov.hmrc.plasticpackagingtax.returns.models.obligations.Obligation
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.returns.{
  imported_plastic_page,
  imported_plastic_weight_page
}
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents

import java.time.LocalDate
import scala.concurrent.Future

class ImportedPlasticWeightControllerSpec extends ControllerSpec {

  private val mcc        = stubMessagesControllerComponents()
  private val weightPage = mock[imported_plastic_weight_page]
  private val page       = mock[imported_plastic_page]

  private val controller = new ImportedPlasticWeightController(authenticate = mockAuthAction,
                                                               journeyAction = mockJourneyAction,
                                                               mcc = mcc,
                                                               importedWeightPage = weightPage,
                                                               importedComponentPage = page,
                                                               returnsConnector =
                                                                 mockTaxReturnsConnector
  )

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    when(page.apply(any[Form[Boolean]], any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(weightPage.apply(any[Form[ImportedPlasticWeight]])(any(), any())).thenReturn(
      HtmlFormat.empty
    )
  }

  override protected def afterEach(): Unit = {
    reset(weightPage)
    super.afterEach()
  }

  "ImportedPlasticWeightController" should {

    "return 200" when {
      "contribution page method is invoked" in {
        authorizedUser()
        val result = controller.contribution()(getRequest())

        status(result) mustBe OK
      }

      "imported plastic value exists and contribution page method is invoked" in {
        authorizedUser()
        mockTaxReturnFind(aTaxReturn().copy(importedPlastic = Some(true)))
        val result = controller.contribution()(getRequest())

        status(result) mustBe OK
      }
      "weight page method is invoked" in {
        authorizedUser()
        val result = controller.weight()(getRequest())

        status(result) mustBe OK
      }

      "imported plastic weight already exists and weight page method is invoked" in {
        authorizedUser()
        mockTaxReturnFind(aTaxReturn())
        val result = controller.weight()(getRequest())

        status(result) mustBe OK
      }
    }

    "return 303 (OK) for " when {
      "user submits the contribution details" in {
        authorizedUser()
        val taxReturn = aTaxReturn()
        mockTaxReturnFind(taxReturn)
        mockTaxReturnUpdate(taxReturn)

        val correctForm = Seq("answer" -> "yes")
        val result: Future[Result] =
          controller.submitContribution()(postJsonRequestEncoded(correctForm: _*))

        status(result) mustBe SEE_OTHER
        modifiedTaxReturn.importedPlastic.get mustBe true
        redirectLocation(result) mustBe Some(
          returnRoutes.ImportedPlasticWeightController.weight().url
        )
        reset(mockTaxReturnsConnector)
      }
      "user submits the weight details" in {
        authorizedUser()
        mockTaxReturnFind(aTaxReturn())
        mockTaxReturnUpdate(aTaxReturn())

        val result =
          controller.submitWeight()(postRequestEncoded(ImportedPlasticWeight(totalKg = "10")))

        status(result) mustBe SEE_OTHER
        modifiedTaxReturn.importedPlasticWeight.get.totalKg mustBe 10
        redirectLocation(result) mustBe Some(
          returnRoutes.HumanMedicinesPlasticWeightController.displayPage().url
        )
        reset(mockTaxReturnsConnector)
      }
    }

    "return prepopulated form" when {

      "weight exist" in {
        def pageForm: Form[ImportedPlasticWeight] = {
          val captor = ArgumentCaptor.forClass(classOf[Form[ImportedPlasticWeight]])
          verify(weightPage).apply(captor.capture())(any(), any())
          captor.getValue
        }
        authorizedUser()
        mockTaxReturnFind(aTaxReturn(withImportedPlasticWeight(totalKg = 10)))

        await(controller.weight()(getRequest()))

        pageForm.get.totalKg mustBe "10"

      }
    }

    "return 400 (BAD_REQUEST)" when {

      "user submits imported plastic question" in {
        authorizedUser()
        val result =
          controller.submitContribution()(postRequest(JsObject.empty))

        status(result) mustBe BAD_REQUEST
      }

      "user submits invalid imported plastic weight" in {
        authorizedUser()
        val result =
          controller.submitWeight()(postRequest(Json.toJson(ImportedPlasticWeight(totalKg = ""))))

        status(result) mustBe BAD_REQUEST
      }
    }

    "return an error" when {

      "user submits imported plastic contribution form and the tax return update fails" in {
        authorizedUser()
        mockTaxReturnFailure()
        val correctForm = Seq("answer" -> "yes")
        val result: Future[Result] =
          controller.submitContribution()(postJsonRequestEncoded(correctForm: _*))

        intercept[DownstreamServiceError](status(result))
      }

      "user submits form and the tax return update fails" in {
        authorizedUser()
        mockTaxReturnFailure()
        val result =
          controller.submitWeight()(postRequest(Json.toJson(ImportedPlasticWeight(totalKg = "5"))))

        intercept[DownstreamServiceError](status(result))
      }

      "user submits imported plastic contribution form and a tax return update runtime exception occurs" in {
        authorizedUser()
        mockTaxReturnException()

        val correctForm = Seq("answer" -> "yes")
        val result: Future[Result] =
          controller.submitContribution()(postJsonRequestEncoded(correctForm: _*))

        intercept[RuntimeException] {
          status(result)
        }
      }

      "user submits form and a tax return update runtime exception occurs" in {
        authorizedUser()
        mockTaxReturnException()
        val result =
          controller.submitWeight()(postRequest(Json.toJson(ImportedPlasticWeight(totalKg = "5"))))

        intercept[RuntimeException](status(result))
      }

      "user is not authorised to submit the contribution page" in {
        unAuthorizedUser()
        val result = controller.submitContribution()(getRequest())

        intercept[RuntimeException](status(result))
      }

      "user is not authorised" in {
        unAuthorizedUser()
        val result = controller.submitWeight()(getRequest())

        intercept[RuntimeException](status(result))
      }
    }
  }
}
