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

package views

import base.ViewSpecBase
import forms.amends._
import models.returns.{IdDetails, ReturnDisplayApi, ReturnDisplayDetails, TaxReturnObligation}
import play.api.data.Form
import play.twirl.api.Html
import uk.gov.hmrc.scalatestaccessibilitylinter.AccessibilityMatchers
import uk.gov.hmrc.scalatestaccessibilitylinter.domain.OutputFormat
import viewmodels.checkAnswers.ViewReturnSummaryViewModel
import views.html.amends._

import java.time.LocalDate

class AmendReturnA11ySpec extends ViewSpecBase with AccessibilityMatchers {

  val aTaxObligation: TaxReturnObligation = TaxReturnObligation(LocalDate.now(), LocalDate.now().plusWeeks(12), LocalDate.now().plusWeeks(16), "PK1")
  val amount = 321L

  "AmendDirectExportPlasticPackagingView" should {
    val form: Form[Long] = new AmendDirectExportPlasticPackagingFormProvider()()
    val page: AmendDirectExportPlasticPackagingView = inject[AmendDirectExportPlasticPackagingView]

    def render(form: Form[Long]): String =
      page(form, aTaxObligation)(request, messages).toString()

    "pass accessibility checks without error" in {
      render(form) must passAccessibilityChecks
    }

    "pass accessibility checks with error" in {
      render(form.withError("test", "message")) must passAccessibilityChecks
    }
  }

  "AmendRecycledPlasticPackagingView" should {
    val form: Form[Long] = new AmendRecycledPlasticPackagingFormProvider()()
    val page: AmendRecycledPlasticPackagingView = inject[AmendRecycledPlasticPackagingView]

    def render(form: Form[Long] = form): String =
      page(form, aTaxObligation)(request, messages).toString()

    "pass accessibility checks without error" in {
      render(form) must passAccessibilityChecks
    }

    "pass accessibility checks with error" in {
      render(form.withError("test", "message")) must passAccessibilityChecks
    }
  }

  "AmendHumanMedicinePlasticPackagingView" should {
    val form: Form[Long] = new AmendHumanMedicinePlasticPackagingFormProvider()()
    val page: AmendHumanMedicinePlasticPackagingView = inject[AmendHumanMedicinePlasticPackagingView]

    def render(form: Form[Long]): String =
      page(form, aTaxObligation)(request, messages).toString()

    "pass accessibility checks without error" in {
      render(form) must passAccessibilityChecks
    }

    "pass accessibility checks with error" in {
      render(form.withError("test", "message")) must passAccessibilityChecks
    }
  }

  "AmendManufacturedPlasticPackagingView" should {
    val form: Form[Long] = new AmendManufacturedPlasticPackagingFormProvider()()
    val page: AmendManufacturedPlasticPackagingView = inject[AmendManufacturedPlasticPackagingView]

    def render(form: Form[Long] = form): String =
      page(form, aTaxObligation)(request, messages).toString()

    "pass accessibility checks without error" in {
      render(form) must passAccessibilityChecks
    }

    "pass accessibility checks with error" in {
      render(form.withError("test", "message")) must passAccessibilityChecks(OutputFormat.Verbose)
    }
  }

  "AmendImportedPlasticPackagingView" should {
    val form: Form[Long] = new AmendImportedPlasticPackagingFormProvider()()
    val page: AmendImportedPlasticPackagingView = inject[AmendImportedPlasticPackagingView]

    def render(form: Form[Long] = form): String =
      page(form, aTaxObligation)(request, messages).toString()

    "pass accessibility checks without error" in {
      render(form) must passAccessibilityChecks
    }

    "pass accessibility checks with error" in {
      render(form.withError("test", "message")) must passAccessibilityChecks
    }
  }

  "AmendConfirmationView" should {
    "pass accessibility checks" in {
      val page: AmendConfirmation = inject[AmendConfirmation]

      def render(chargeRef: Option[String]): Html =
        page(chargeRef)(request, messages)

      render(None).toString() must passAccessibilityChecks
    }
  }

  "ViewReturnSummaryView" should {
    "pass accessibility checks" in {
      val page: ViewReturnSummaryView = inject[ViewReturnSummaryView]
      val vm = ViewReturnSummaryViewModel(
        ReturnDisplayApi(
          "2019-08-28T09:30:47Z",
          IdDetails.apply("XMPPT0000000001", "00-11-submission-id"),
          None,
          ReturnDisplayDetails.apply(500, 400, 0, 0, 0, 0, 0, 0, 900, 300)
        )
      )

      def render(returnPeriod: String): Html =
        page(returnPeriod, vm)(request, messages)

      render("PK1").toString() must passAccessibilityChecks
    }
  }

  //TODO: Add spec for Heart Page (CheckYourAnswersView)

}
