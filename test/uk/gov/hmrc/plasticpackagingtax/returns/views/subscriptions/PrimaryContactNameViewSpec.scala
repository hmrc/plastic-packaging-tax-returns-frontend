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

package uk.gov.hmrc.plasticpackagingtax.returns.views.subscriptions

import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers
import play.api.data.Form
import uk.gov.hmrc.plasticpackagingtax.returns.base.unit.UnitViewSpec
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.subscriptions.{
  routes => subscriptionsRoutes
}
import uk.gov.hmrc.plasticpackagingtax.returns.forms.subscriptions.Name
import uk.gov.hmrc.plasticpackagingtax.returns.forms.subscriptions.Name.form
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.subscriptions.primary_contact_name_page
import uk.gov.hmrc.plasticpackagingtax.returns.views.tags.ViewTest

@ViewTest
class PrimaryContactNameViewSpec extends UnitViewSpec with Matchers {

  private val page = instanceOf[primary_contact_name_page]

  private def createView(form: Form[Name] = Name.form()): Document =
    page(form)(request, messages)

  "Primary Contact Name View" should {

    "have proper messages for labels" in {
      messages must haveTranslationFor("subscription.primaryContactDetails.title")
      messages must haveTranslationFor("subscription.primaryContactDetails.name.title")
      messages must haveTranslationFor("subscription.primaryContactDetails.name.hint")
      messages must haveTranslationFor("subscription.primaryContactDetails.name.label")
      messages must haveTranslationFor("subscription.primaryContactDetails.name.error.length")
      messages must haveTranslationFor("subscription.primaryContactDetails.name.error.empty")
      messages must haveTranslationFor("subscription.primaryContactDetails.name.error.format")
    }

    val view = createView()

    "validate other rendering  methods" in {
      page.f(form())(request, messages).select("title").text() must include(
        messages("subscription.primaryContactDetails.name.title")
      )
      page.render(form(), request, messages).select("title").text() must include(
        messages("subscription.primaryContactDetails.name.title")
      )
    }

    "contain timeout dialog function" in {

      containTimeoutDialogFunction(view) mustBe true
    }

    "display sign out link" in {

      displaySignOutLink(view)
    }

    "display 'Back' button" in {

      view.getElementById("back-link") must haveHref(
        subscriptionsRoutes.ViewSubscriptionController.displayPage()
      )
    }

    "display title" in {

      view.select("title").text() must include(
        messages("subscription.primaryContactDetails.name.title")
      )
    }

    "display header" in {

      view.getElementById("section-header").text() must include(
        messages("subscription.primaryContactDetails.title")
      )
    }

    "display total name hint" in {

      view.getElementById("value-hint").text() must include(
        messages("subscription.primaryContactDetails.name.hint")
      )
    }

    "display total name input box" in {

      view must containElementWithID("value")
    }

    "display 'Save' button" in {

      view.getElementById("submit").text() mustBe "Save"
    }
  }

  "Exported Plastic name View when filled" should {

    "display data in total name input" in {

      val form = Name
        .form()
        .fill(Name("Jack Gatsby"))
      val view = createView(form)

      view.getElementById("value").attr("value") mustBe "Jack Gatsby"
    }
  }

  "display error" when {

    "name is not entered" in {

      val form = Name
        .form()
        .fillAndValidate(Name(""))
      val view = createView(form)

      view must haveGovukGlobalErrorSummary

      view must haveGovukFieldError("value", "Name is required")
    }
  }
}
