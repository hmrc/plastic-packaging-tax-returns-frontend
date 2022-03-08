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
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.data.Form
import play.api.http.Status.{BAD_REQUEST, OK}
import play.api.test.Helpers.{await, contentAsString, redirectLocation, status}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.plasticpackagingtax.returns.base.unit.ControllerSpec
import uk.gov.hmrc.plasticpackagingtax.returns.forms.{
  ManufacturedPlastic,
  ManufacturedPlasticWeight => ManufacturedPlasticWeightForm
}
import uk.gov.hmrc.plasticpackagingtax.returns.models.domain.ManufacturedPlasticWeight
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.returns.{
  manufactured_plastic_page,
  manufactured_plastic_weight_page
}
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents

import scala.concurrent.Future

class ManufacturedPlasticControllerSpec extends ControllerSpec {

  private val mcc              = stubMessagesControllerComponents()
  private val contributionPage = mock[manufactured_plastic_page]
  private val weightPage       = mock[manufactured_plastic_weight_page]

  private val aTaxReturnWithManufacturedDetail =
    aTaxReturn(withManufacturedPlastic(true), withManufacturedPlasticWeight(12000L))

  private val controller = new ManufacturedPlasticController(
    authenticate = mockAuthAction,
    journeyAction = mockJourneyAction,
    mcc = mcc,
    returnsConnector = mockTaxReturnsConnector,
    appConfig = config,
    manufacturedPlasticPage = contributionPage,
    manufacturedPlasticWeightPage = weightPage
  )

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    mockTaxReturnFind(aTaxReturnWithManufacturedDetail)
    mockTaxReturnUpdate()

    when(contributionPage.apply(any(), any(), any())(any(), any())).thenReturn(
      HtmlFormat.raw("Manufactured Contribution")
    )
    when(weightPage.apply(any(), any(), any())(any(), any())).thenReturn(
      HtmlFormat.raw("Manufactured Weight")
    )
  }

  override protected def afterEach(): Unit = {
    reset(contributionPage, weightPage, mockTaxReturnsConnector)

    super.afterEach()
  }

  "Manufactured Plastic Controller" should {

    "show the any manufactured plastic contribution page" when {
      "user is authorised" in {
        val result = controller.contribution()(getRequest())

        status(result) mustBe OK
        contentAsString(result) mustBe "Manufactured Contribution"

        val formCaptor: ArgumentCaptor[Form[Boolean]] =
          ArgumentCaptor.forClass(classOf[Form[Boolean]])
        verify(contributionPage).apply(formCaptor.capture(), any(), any())(any(), any())
        formCaptor.getValue.value mustBe aTaxReturnWithManufacturedDetail.manufacturedPlastic
      }
    }

    "show the manufactured plastic weight page" when {
      "user is authorised" in {
        val result = controller.weight()(getRequest())

        status(result) mustBe OK
        contentAsString(result) mustBe "Manufactured Weight"

        val formCaptor: ArgumentCaptor[Form[ManufacturedPlasticWeightForm]] =
          ArgumentCaptor.forClass(classOf[Form[ManufacturedPlasticWeightForm]])
        verify(weightPage).apply(formCaptor.capture(), any(), any())(any(), any())
        formCaptor.getValue.value.map(
          _.totalKg
        ) mustBe aTaxReturnWithManufacturedDetail.manufacturedPlasticWeight.map(_.totalKg.toString)
      }
    }

    "redisplay page" when {
      "invalid input supplied" when {
        "user does not select an option for whether they have manufactured plastic" in {
          val result =
            controller.submitContribution()(postRequestEncoded(ManufacturedPlastic.form()))

          status(result) mustBe BAD_REQUEST
          contentAsString(result) mustBe "Manufactured Contribution"
        }
        "user does not input any manufactured weight value" in {
          val result =
            controller.submitWeight()(postRequestEncoded(ManufacturedPlasticWeightForm.form()))

          status(result) mustBe BAD_REQUEST
          contentAsString(result) mustBe "Manufactured Weight"
        }
      }
    }

    "update tax return and redirect as expected" when {
      "user indicates that they have manufactured contribution" in {
        val res =
          await(controller.submitContribution()(postRequestTuplesEncoded(Seq(("answer", "yes")))))

        modifiedTaxReturn.manufacturedPlastic mustBe Some(true)
        redirectLocation(Future.successful(res)) mustBe Some(
          routes.ManufacturedPlasticController.weight().url
        )
      }
      "user indicates that they do NOT have manufactured contribution" in {
        val res =
          await(controller.submitContribution()(postRequestTuplesEncoded(Seq(("answer", "no")))))

        modifiedTaxReturn.manufacturedPlastic mustBe Some(false)
        redirectLocation(Future.successful(res)) mustBe Some(
          routes.ImportedPlasticController.contribution().url
        )
      }

      "user inputs valid manufactured weight" in {
        val res = await(
          controller.submitWeight()(postRequestEncoded(ManufacturedPlasticWeightForm("15000")))
        )

        modifiedTaxReturn.manufacturedPlasticWeight mustBe Some(ManufacturedPlasticWeight(15000))
        redirectLocation(Future.successful(res)) mustBe Some(
          routes.ImportedPlasticController.contribution().url
        )
      }
    }
  }
}
