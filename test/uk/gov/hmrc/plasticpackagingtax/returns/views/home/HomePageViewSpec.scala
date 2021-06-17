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

package uk.gov.hmrc.plasticpackagingtax.returns.views.home

import org.scalatest.matchers.must.Matchers
import play.twirl.api.Html
import uk.gov.hmrc.plasticpackagingtax.returns.base.unit.UnitViewSpec
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.home.{routes => homeRoutes}
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.returns.{routes => returnsRoutes}
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.home.home_page
import uk.gov.hmrc.plasticpackagingtax.returns.views.tags.ViewTest

@ViewTest
class HomePageViewSpec extends UnitViewSpec with Matchers {

  private val homePage = instanceOf[home_page]

  private def createView(): Html = homePage()

  "Home Page view" should {

    "have proper messages for labels" in {

      messages must haveTranslationFor("account.homePage.title")
      messages must haveTranslationFor("account.homePage.currentBalance")
      messages must haveTranslationFor("account.homePage.currentBalance.onDate")
      messages must haveTranslationFor("account.homePage.card.transactions.header")
      messages must haveTranslationFor("account.homePage.card.transactions.body")
      messages must haveTranslationFor("account.homePage.card.transactions.link.1")
      messages must haveTranslationFor("account.homePage.card.registration.header")
      messages must haveTranslationFor("account.homePage.card.registration.body")
      messages must haveTranslationFor("account.homePage.card.registration.link.1")
      messages must haveTranslationFor("account.homePage.card.registration.link.2")
    }

    val view: Html = createView()

    "validate other rendering  methods" in {
      homePage.f()(request, messages).select("title").text() must include(
        messages("account.homePage.title")
      )
      homePage.render(request, messages).select("title").text() must include(
        messages("account.homePage.title")
      )
    }

    "contain timeout dialog function" in {

      containTimeoutDialogFunction(view) mustBe true
    }

    "display sign out link" in {

      displaySignOutLink(view)
    }

    "display title" in {

      view.select("title").text() must include(messages("account.homePage.title"))
    }

    "display header" in {

      view.getElementById("title") must containMessage("account.homePage.title")
      view.getElementById("entityName").text() mustBe "Plastic Packaging Ltd."
    }

    "display 'balance' section" in {

      view.getElementById("currentBalance") must containMessage("account.homePage.currentBalance")
      view.getElementById("accountBalance").text() mustBe "£2720.00"
      view.getElementById("accountBalanceOnDate").text() must include("on ")
    }

    "display 'Returns and transactions' card" in {

      view.select(".card .card-body .govuk-heading-s").first() must containMessage(
        "account.homePage.card.transactions.header"
      )
      view.select(".card .card-body .govuk-body").first() must containMessage(
        "account.homePage.card.transactions.body"
      )
      view.select(".card .govuk-link").first() must containMessage(
        "account.homePage.card.transactions.link.1"
      )
      view.select(".card .govuk-link").first() must haveHref(
        returnsRoutes.ManufacturedPlasticWeightController.displayPage().url
      )
    }

    "display 'Registration and primary contacts' card" in {

      view.select(".card .card-body .govuk-heading-s").get(1) must containMessage(
        "account.homePage.card.registration.header"
      )
      view.select(".card .card-body .govuk-body").get(1) must containMessage(
        "account.homePage.card.registration.body"
      )

      view.select(".card .govuk-link").get(1) must containMessage(
        "account.homePage.card.registration.link.1"
      )
      view.select(".card .govuk-link").get(1) must haveHref(
        homeRoutes.ViewSubscriptionController.displayPage().url
      )

      view.select(".card .govuk-link").get(2) must containMessage(
        "account.homePage.card.registration.link.2"
      )
      view.select(".card .govuk-link").get(2) must haveHref(
        homeRoutes.ViewSubscriptionController.displayPage().url
      )

    }
  }
}
