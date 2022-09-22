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
import forms.returns._
import models.Mode.NormalMode
import models.UserAnswers
import models.returns.TaxReturnObligation
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import pages.returns.ManufacturedPlasticPackagingPage
import play.api.data.Form
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.scalatestaccessibilitylinter.AccessibilityMatchers
import uk.gov.hmrc.scalatestaccessibilitylinter.domain.OutputFormat
import viewmodels.checkAnswers.returns.ManufacturedPlasticPackagingSummary.ConfirmManufacturedPlasticPackaging
import viewmodels.govuk.summarylist._
import views.html.returns._

import java.time.LocalDate

class CreateReturnA11ySpec
  extends ViewSpecBase
    with AccessibilityMatchers {

  val aTaxObligation: TaxReturnObligation = TaxReturnObligation(LocalDate.now(), LocalDate.now().plusWeeks(12), LocalDate.now().plusWeeks(16), "PK1")
  val amount = 321L

  "ExportedPlasticPackagingWeightView" should {
    val form = new ExportedPlasticPackagingWeightFormProvider()()
    val page = inject[ExportedPlasticPackagingWeightView]

    def render(form: Form[Long]): String =
      page(form, NormalMode, amount)(request, messages).toString()

    "pass accessibility checks without error" in {
      render(form) must passAccessibilityChecks
    }

    "pass accessibility checks with error" in {
      render(form.withError("test", "message")) must passAccessibilityChecks
    }

  }

  "StartYourReturnView" should {
    val form = new StartYourReturnFormProvider()()
    val page = inject[StartYourReturnView]

    def render(form: Form[Boolean]): String =
      page(form, NormalMode, aTaxObligation, true)(request, messages).toString()

    "pass accessibility checks without error" in {
      render(form) must passAccessibilityChecks
    }

    "pass accessibility checks with error" in {
      render(form.withError("test", "message")) must passAccessibilityChecks
    }
  }

  "ReturnConfirmationView" should {
    "pass accessibility checks" in {
      val page: ReturnConfirmationView = inject[ReturnConfirmationView]

      def render(chargeRef: Option[String]): Html =
        page(chargeRef, false)(request, messages)

      render(None).toString() must passAccessibilityChecks
    }
  }

  "NotStartOtherReturnsView" should {
    "pass accessibility checks" in {
      val page: NotStartOtherReturnsView = inject[NotStartOtherReturnsView]

      def render: Html =
        page()(request, messages)

      render.toString() must passAccessibilityChecks
    }
  }

  "NonExportedRecycledPlasticPackagingWeightView" should {
    val form: Form[Long] = new NonExportedRecycledPlasticPackagingWeightFormProvider()()
    val page: NonExportedRecycledPlasticPackagingWeightView = inject[NonExportedRecycledPlasticPackagingWeightView]

    def render(form: Form[Long] = form): String =
      page(form, NormalMode, amount)(request, messages).toString()
        
    "pass accessibility checks without error" in {
      render(form) must passAccessibilityChecks
    }
    
    "pass accessibility checks with error" in {
      render(form.withError("test", "message")) must passAccessibilityChecks
    }
  }

  "NonExportedRecycledPlasticPackagingView" should {
    "pass accessibility checks" in {
      val form = new NonExportedRecycledPlasticPackagingFormProvider()()
      val page = inject[NonExportedRecycledPlasticPackagingView]

      def render: Html =
        page(form, NormalMode, amount)(request, messages)

      render.toString() must passAccessibilityChecks
    }
  }

  "NonExportedHumanMedicinesPlasticPackagingWeightView" should {
    val form = new NonExportedHumanMedicinesPlasticPackagingWeightFormProvider()()
    val page = inject[NonExportedHumanMedicinesPlasticPackagingWeightView]

    def render(form: Form[Long]): String =
      page(amount, form, NormalMode)(request, messages).toString()

    "pass accessibility checks without error" in {
      render(form) must passAccessibilityChecks
    }

    "pass accessibility checks with error" in {
      render(form.withError("test", "message")) must passAccessibilityChecks
    }
  }

  "NonExportedHumanMedicinesPlasticPackagingView" should {
    val form = new NonExportedHumanMedicinesPlasticPackagingFormProvider()()
    val page = inject[NonExportedHumanMedicinesPlasticPackagingView]

    def render(form: Form[Boolean]): String =
      page(amount, form, NormalMode)(request, messages).toString()

    "pass accessibility checks without error" in {
      render(form) must passAccessibilityChecks
    }

    "pass accessibility checks with error" in {
      render(form.withError("test", "message")) must passAccessibilityChecks
    }
  }

  "ManufacturedPlasticPackagingWeightView" should {
    val form: Form[Long]                             = new ManufacturedPlasticPackagingWeightFormProvider()()
    val page: ManufacturedPlasticPackagingWeightView = inject[ManufacturedPlasticPackagingWeightView]

    def render(form: Form[Long] = form): String =
      page(form, NormalMode, aTaxObligation)(request, messages).toString()

    "pass accessibility checks without error" in {
      render(form) must passAccessibilityChecks
    }

    "pass accessibility checks with error" in {
      render(form.withError("test", "message")) must passAccessibilityChecks(OutputFormat.Verbose)
    }
  }

  "ManufacturedPlasticPackagingView" should {
    val form = new ManufacturedPlasticPackagingFormProvider()()
    val page = inject[ManufacturedPlasticPackagingView]

    def render(form: Form[Boolean]): String =
      page(form, NormalMode, aTaxObligation)(request, messages).toString()

    "pass accessibility checks without error" in {
      render(form) must passAccessibilityChecks
    }

    "pass accessibility checks with error" in {
      render(form.withError("test", "message")) must passAccessibilityChecks
    }
  }

  "ImportedPlasticPackagingWeightView" should {
    val form: Form[Long]                         = new ManufacturedPlasticPackagingWeightFormProvider()()
    val page: ImportedPlasticPackagingWeightView = inject[ImportedPlasticPackagingWeightView]

    def render(form: Form[Long] = form): String =
      page(form, NormalMode, aTaxObligation)(request, messages).toString()

    "pass accessibility checks without error" in {
      render(form) must passAccessibilityChecks
    }

    "pass accessibility checks with error" in {
      render(form.withError("test", "message")) must passAccessibilityChecks
    }
  }

  "ImportedPlasticPackagingView" should {
    val form = new ImportedPlasticPackagingFormProvider()()
    val page = inject[ImportedPlasticPackagingView]

    def render(form: Form[Boolean]): String =
      page(form, NormalMode, aTaxObligation)(request, messages).toString()

    "pass accessibility checks without error" in {
      render(form) must passAccessibilityChecks
    }

    "pass accessibility checks with error" in {
      render(form.withError("test", "message")) must passAccessibilityChecks
    }
  }

  "DirectlyExportedComponentsView" should {
    val form = new DirectlyExportedComponentsFormProvider()()
    val page = inject[DirectlyExportedComponentsView]

    def render(form: Form[Boolean]): String =
      page(form, NormalMode, amount)(request, messages).toString()

    "pass accessibility checks without error" in {
      render(form) must passAccessibilityChecks
    }

    "pass accessibility checks with error" in {
      render(form.withError("test", "message")) must passAccessibilityChecks
    }
  }

  "ConfirmPlasticPackagingTotalView" should {
    "pass accessibility checks" in {
      val page: ConfirmPlasticPackagingTotalView = inject[ConfirmPlasticPackagingTotalView]

      def render(list: SummaryList): Html =
        page(list)(request, messages)

      render(createSummaryList).toString() must passAccessibilityChecks
    }
  }

  def createSummaryList: SummaryList = {
    val answer = UserAnswers("123").set(ManufacturedPlasticPackagingPage, true).success.value
    SummaryListViewModel(
      Seq(ConfirmManufacturedPlasticPackaging).flatMap(_.row(answer))
    )
  }

  //TODO: Add spec for ReturnsCheckYourAnswersView once refactored

}
