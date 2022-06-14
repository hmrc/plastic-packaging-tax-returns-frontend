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

package controllers.returns

import base.SpecBase
import cacheables.ObligationCacheable
import controllers.returns.{routes => returnsRoutes}
import models.{CheckMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.mockito.MockitoSugar.mock
import org.scalatest.BeforeAndAfterEach
import pages.returns.{ImportedPlasticPackagingPage, ImportedPlasticPackagingWeightPage, ManufacturedPlasticPackagingPage, ManufacturedPlasticPackagingWeightPage}
import play.api.Application
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.auth.core.InsufficientEnrolments
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import viewmodels.PrintLong
import views.html.returns.ConfirmPlasticPackagingTotalView

import scala.util.Try

class ConfirmPlasticPackagingTotalControllerSpec extends SpecBase with BeforeAndAfterEach {

  private val view    = mock[ConfirmPlasticPackagingTotalView]


  override def beforeEach() = {
    super.beforeEach()
    reset(view)
    when(view.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  lazy val confirmPlasticPackagingTotalRoute = controllers.returns.routes.ConfirmPlasticPackagingTotalController.onPageLoad.url

  "ConfirmPlasticPackagingTotal Controller" - {

    "when displaying the page" - {
      "must redirect if no data found" in {

        val application = buildApplication(emptyUserAnswers)

        running(application) {
          val request = FakeRequest(GET, confirmPlasticPackagingTotalRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.IndexController.onPageLoad.url)
        }
      }

      "must display all the answer with total" in {

        val application: Application = buildApplication(
          createUserAnswer(manufacturedPlastic = true -> 20000L, importedPlastic = true -> 25000L).success.value)

        running(application) {
          val request = FakeRequest(GET, confirmPlasticPackagingTotalRoute)

          val result = route(application, request).value

          status(result) mustEqual OK

          val captor: ArgumentCaptor[SummaryList] = ArgumentCaptor.forClass(classOf[SummaryList])
          verify(view).apply(captor.capture())(any(), any())
          assertResults(application, captor.getValue, ("site.yes", 20000), ("site.yes", 25000))
        }
      }

      "should display the No answer" in {
        val application: Application = buildApplication(createUserNoAnswer)

        running(application) {
          val request = FakeRequest(GET, confirmPlasticPackagingTotalRoute)

          val result = route(application, request).value

          status(result) mustEqual OK

          val captor: ArgumentCaptor[SummaryList] = ArgumentCaptor.forClass(classOf[SummaryList])
          verify(view).apply(captor.capture())(any(), any())
          assertResults(
            application,
            captor.getValue,
            expectedManufactured = "site.no" -> 0,
            expectedImported = "site.no" -> 0)
        }
      }

      "raise and error" - {
        "when not authorised" in {
          val application = applicationBuilderFailedAuth(userAnswers = None).build()

          running(application) {
            val request = FakeRequest(GET, confirmPlasticPackagingTotalRoute)

            val result = route(application, request).value

            intercept[InsufficientEnrolments](status(result))
          }
        }
      }
    }
  }

  private def buildApplication(userAnswer: UserAnswers) = {
    applicationBuilder(userAnswers = Some(userAnswer))
      .overrides(bind[ConfirmPlasticPackagingTotalView].toInstance(view))
      .build()
  }

  private def createUserAnswer
  (
    manufacturedPlastic: (Boolean, Long),
    importedPlastic: (Boolean, Long)
  ): Try[UserAnswers] = {
    UserAnswers("123").set(ObligationCacheable, taxReturnOb).get
      .set(ManufacturedPlasticPackagingPage, manufacturedPlastic._1).get
      .set(ManufacturedPlasticPackagingWeightPage, manufacturedPlastic._2).get
      .set(ImportedPlasticPackagingPage, importedPlastic._1).get
      .set(ImportedPlasticPackagingWeightPage, importedPlastic._2)
  }

  private def createUserNoAnswer: UserAnswers = {
    UserAnswers("123").set(ObligationCacheable, taxReturnOb).get
      .set(ManufacturedPlasticPackagingPage, false).get
      .set(ImportedPlasticPackagingPage, false).get
  }

  private def assertResults
  (
    application: Application,
    actual: SummaryList,
    expectedManufactured: (String, Long),
    expectedImported: (String, Long)
  ): Unit = {

    actual.rows.size mustBe 5

    assertRowResults(
      application,
      actual.rows(0),
      "confirmPlasticPackagingTotal.manufacturedPlasticPackaging.label",
      messages(application, expectedManufactured._1),
      Some(returnsRoutes.ManufacturedPlasticPackagingController.onPageLoad(CheckMode).url))

    assertRowResults(
      application,
      actual.rows(1),
      "confirmPlasticPackagingTotal.weightManufacturedPlasticPackaging.label",
      expectedManufactured._2.asKg,
      Some(returnsRoutes.ManufacturedPlasticPackagingWeightController.onPageLoad(CheckMode).url))

    assertRowResults(
      application,
      actual.rows(2),
      "confirmPlasticPackagingTotal.importedPlasticPackaging.label",
      messages(application, expectedImported._1),
      Some(returnsRoutes.ImportedPlasticPackagingController.onPageLoad(CheckMode).url))

    assertRowResults(
      application,
      actual.rows(3),
      "confirmPlasticPackagingTotal.weightImportedPlasticPackaging.label",
      expectedImported._2.asKg,
      Some(returnsRoutes.ImportedPlasticPackagingWeightController.onPageLoad(CheckMode).url))

    assertRowResults(
      application,
      actual.rows(4),
      "confirmPlasticPackagingTotal.total.label",
      (expectedManufactured._2 + expectedImported._2).asKg
    )
  }

  private def assertRowResults
  (
    app: Application,
    row: SummaryListRow,
    expectedKey: String,
    expectedAnswer: String,
    expectedUrl: Option[String] = None,
    change: Option[String] = None
  ): Unit = {
    row.key.content.asInstanceOf[Text].value mustBe messages(app, expectedKey)
    row.value.content.asInstanceOf[Text].value mustBe expectedAnswer
    change.map { value =>
      row.actions.get.items(0).content.asInstanceOf[Text].value mustBe messages(app, value)
    }
    expectedUrl.map { value => row.actions.get.items(0).href mustBe value }
  }
}
