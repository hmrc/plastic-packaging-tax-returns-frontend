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

///*
// * Copyright 2022 HM Revenue & Customs
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package views
//
//import base.ViewSpecBase
//import forms.returns.ExportedPlasticPackagingWeightFormProvider
//import models.Mode.NormalMode
//import models.UserAnswers
//import org.scalatest.TryValues.convertTryToSuccessOrFailure
//import pages.returns.ManufacturedPlasticPackagingPage
//import play.twirl.api.Html
//import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
//import uk.gov.hmrc.scalatestaccessibilitylinter.AccessibilityMatchers
//import viewmodels.checkAnswers.returns.ManufacturedPlasticPackagingSummary.ConfirmManufacturedPlasticPackaging
//import views.html.returns.{ConfirmPlasticPackagingTotalView, ExportedPlasticPackagingWeightView}
//
//class ConfirmPlasticPackagingTotalAccSpec
//  extends ViewSpecBase
//    with AccessibilityMatchers {
//
//  val form = new ExportedPlasticPackagingWeightFormProvider()()
//
//  val page = inject[ConfirmPlasticPackagingTotalView]
//
//  private def createView(list: SummaryList): Html =
//    page(list)(request, messages)
//
//  def createSummaryList: SummaryList = {
//    val answer = UserAnswers("123").set(ManufacturedPlasticPackagingPage, true).success.value
//    SummaryListViewModel(
//      Seq(ConfirmManufacturedPlasticPackaging).flatMap(_.row(answer))
//    )
//  }
//
//  "ExportedPlasticPackagingWeightView" should {
//
//    val view = createView(createSummaryList)
//
//    "pass accessibility checks" in {
//      createView.toString() must passAccessibilityChecks
//    }
//
//  }
//
//}
