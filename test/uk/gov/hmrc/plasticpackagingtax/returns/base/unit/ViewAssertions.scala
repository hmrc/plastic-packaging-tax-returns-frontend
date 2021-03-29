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

package uk.gov.hmrc.plasticpackagingtax.returns.base.unit

import org.jsoup.nodes.Element
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import uk.gov.hmrc.plasticpackagingtax.returns.spec.ViewMatchers
import uk.gov.hmrc.plasticpackagingtax.returns.views.model.SignOutReason
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.home.{routes => pptRoutes}

import scala.collection.convert.ImplicitConversions.`collection AsScalaIterable`

trait ViewAssertions extends ViewMatchers {

  def containTimeoutDialogFunction(view: Element) =
    view.getElementById("timeout-dialog") != null &&
      view.getElementsByTag("script")
        .map(
          s =>
            s.getElementsByAttributeValueContaining("src", "/assets/javascripts/timeoutDialog.js")
        )
        .nonEmpty

  def displaySignOutLink(view: Element) = {
    view.getElementsByClass("hmrc-sign-out-nav__link").first().text() must include("Sign out")
    view.getElementsByClass("hmrc-sign-out-nav__link").first() must haveHref(
      pptRoutes.SignOutController.signOut(SignOutReason.UserAction)
    )
  }

}
