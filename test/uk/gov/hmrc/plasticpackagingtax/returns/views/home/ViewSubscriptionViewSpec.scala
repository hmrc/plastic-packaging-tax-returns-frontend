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

import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers
import uk.gov.hmrc.plasticpackagingtax.returns.base.PptTestData
import uk.gov.hmrc.plasticpackagingtax.returns.base.unit.UnitViewSpec
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.home.{routes => homeRoutes}
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.home.view_subscription_page
import uk.gov.hmrc.plasticpackagingtax.returns.views.tags.ViewTest

@ViewTest
class ViewSubscriptionViewSpec extends UnitViewSpec with Matchers {

  private val page = instanceOf[view_subscription_page]

  private val subscriptionUkCompany  = PptTestData.ukLimitedCompanySubscription()
  private val subscriptionSoleTrader = PptTestData.soleTraderSubscription()

  private val ukCompanyView  = page(subscriptionUkCompany)(request, messages)
  private val soleTraderView = page(subscriptionSoleTrader)(request, messages)

  "ViewSubscriptionView" should {

    "have proper messages for labels" in {

      messages must haveTranslationFor("account.viewSubscription.organisationDetails.title")
      messages must haveTranslationFor("account.viewSubscription.organisationDetails.label")
      messages must haveTranslationFor(
        "account.viewSubscription.organisationDetails.registeredBusinessAddress"
      )
      messages must haveTranslationFor(
        "account.viewSubscription.organisationDetails.organisationType"
      )
      messages must haveTranslationFor("account.viewSubscription.organisationDetails.businessName")
      messages must haveTranslationFor(
        "account.viewSubscription.organisationDetails.soleTrader.firstName"
      )
      messages must haveTranslationFor(
        "account.viewSubscription.organisationDetails.soleTrader.lastName"
      )

      messages must haveTranslationFor("account.viewSubscription.primaryContactDetails.meta.title")
      messages must haveTranslationFor("account.viewSubscription.primaryContactDetails.label")
      messages must haveTranslationFor("account.viewSubscription.primaryContactDetails.fullName")
      messages must haveTranslationFor("account.viewSubscription.primaryContactDetails.jobTitle")
      messages must haveTranslationFor("account.viewSubscription.primaryContactDetails.address")
      messages must haveTranslationFor("account.viewSubscription.primaryContactDetails.phoneNumber")
      messages must haveTranslationFor("account.viewSubscription.primaryContactDetails.email")

      messages must haveTranslationFor("account.viewSubscription.businessDetails.title")
      messages must haveTranslationFor("account.viewSubscription.businessDetails.phoneNumber")
      messages must haveTranslationFor("account.viewSubscription.businessDetails.name")
      messages must haveTranslationFor("account.viewSubscription.businessDetails.email")

    }

    def getKeyFor(section: Int, index: Int, view: Document) =
      view.getElementsByClass("govuk-summary-list").get(section).getElementsByClass(
        "govuk-summary-list__key"
      ).get(index)
    def getValueFor(section: Int, index: Int, view: Document) =
      view.getElementsByClass("govuk-summary-list").get(section).getElementsByClass(
        "govuk-summary-list__value"
      ).get(index).text()
    def getChangeLinkFor(section: Int, index: Int, view: Document) =
      view.getElementsByClass("govuk-summary-list").get(section).getElementsByClass(
        "govuk-link"
      ).get(index)

    "display 'Back' button" in {

      ukCompanyView.getElementById("back-link") must haveHref(
        homeRoutes.HomeController.displayPage()
      )
    }

    "display meta title" in {

      ukCompanyView.select("title").text() must include(
        messages("account.viewSubscription.organisationDetails.title")
      )
    }

    "display title" in {

      ukCompanyView.getElementsByClass("govuk-label--l").first() must containMessage(
        "account.viewSubscription.organisationDetails.title"
      )
    }

    "displaying organisation details section for uk company" in {

      ukCompanyView.getElementsByClass("govuk-heading-m").first().text() must include(
        messages("account.viewSubscription.organisationDetails.label")
      )

      getKeyFor(0, 0, ukCompanyView) must containMessage(
        "account.viewSubscription.organisationDetails.businessName"
      )
      getKeyFor(0, 1, ukCompanyView) must containMessage(
        "account.viewSubscription.organisationDetails.registeredBusinessAddress"
      )
      getKeyFor(0, 2, ukCompanyView) must containMessage(
        "account.viewSubscription.organisationDetails.organisationType"
      )

      getValueFor(0, 1, ukCompanyView) mustBe "addressLine1 line2 Town PostCode"
      getValueFor(0,
                  2,
                  ukCompanyView
      ) mustBe subscriptionUkCompany.organisationDetails.organisationType.get
      getValueFor(0,
                  0,
                  ukCompanyView
      ) mustBe subscriptionUkCompany.organisationDetails.incorporationDetails.get.companyName.get
    }

    "displaying primary contact details section" in {

      ukCompanyView.getElementsByClass("govuk-heading-m").get(1).text() must include(
        messages("account.viewSubscription.primaryContactDetails.label")
      )

      getKeyFor(1, 0, ukCompanyView) must containMessage(
        "account.viewSubscription.primaryContactDetails.fullName"
      )
      getKeyFor(1, 1, ukCompanyView) must containMessage(
        "account.viewSubscription.primaryContactDetails.jobTitle"
      )
      getKeyFor(1, 2, ukCompanyView) must containMessage(
        "account.viewSubscription.primaryContactDetails.address"
      )
      getKeyFor(1, 3, ukCompanyView) must containMessage(
        "account.viewSubscription.primaryContactDetails.phoneNumber"
      )
      getKeyFor(1, 4, ukCompanyView) must containMessage(
        "account.viewSubscription.primaryContactDetails.email"
      )

      getValueFor(1,
                  0,
                  ukCompanyView
      ) mustBe subscriptionUkCompany.primaryContactDetails.fullName.get
      getValueFor(1,
                  1,
                  ukCompanyView
      ) mustBe subscriptionUkCompany.primaryContactDetails.jobTitle.get
      getValueFor(1, 2, ukCompanyView) mustBe "addressLine1 line2 Town PostCode"
      getValueFor(1,
                  3,
                  ukCompanyView
      ) mustBe subscriptionUkCompany.primaryContactDetails.phoneNumber.get
      getValueFor(1, 4, ukCompanyView) mustBe subscriptionUkCompany.primaryContactDetails.email.get

      getChangeLinkFor(1, 0, ukCompanyView) must haveHref(
        homeRoutes.ViewSubscriptionController.displayPage()
      )
    }

    "displaying business contact details section" in {

      ukCompanyView.getElementsByClass("govuk-heading-m").get(2).text() must include(
        messages("account.viewSubscription.businessDetails.title")
      )
      getKeyFor(2, 0, ukCompanyView) must containMessage(
        "account.viewSubscription.businessDetails.phoneNumber"
      )
      getKeyFor(2, 1, ukCompanyView) must containMessage(
        "account.viewSubscription.businessDetails.email"
      )
      getValueFor(2,
                  0,
                  ukCompanyView
      ) mustBe subscriptionUkCompany.organisationDetails.incorporationDetails.get.phoneNumber.get
      getValueFor(2,
                  1,
                  ukCompanyView
      ) mustBe subscriptionUkCompany.organisationDetails.incorporationDetails.get.email.get

    }

    "displaying organisation details section for sole trader" in {

      soleTraderView.getElementsByClass("govuk-heading-m").first().text() must include(
        messages("account.viewSubscription.organisationDetails.label")
      )
      getKeyFor(0, 0, soleTraderView) must containMessage(
        "account.viewSubscription.organisationDetails.soleTrader.firstName"
      )
      getKeyFor(0, 1, soleTraderView) must containMessage(
        "account.viewSubscription.organisationDetails.soleTrader.lastName"
      )
      getKeyFor(0, 2, soleTraderView) must containMessage(
        "account.viewSubscription.organisationDetails.registeredBusinessAddress"
      )
      getKeyFor(0, 3, soleTraderView) must containMessage(
        "account.viewSubscription.organisationDetails.organisationType"
      )

      getValueFor(0,
                  0,
                  soleTraderView
      ) mustBe subscriptionSoleTrader.organisationDetails.soleTraderDetails.get.firstName.get
      getValueFor(0,
                  1,
                  soleTraderView
      ) mustBe subscriptionSoleTrader.organisationDetails.soleTraderDetails.get.lastName.get
      getValueFor(0, 2, soleTraderView) mustBe "addressLine1 line2 Town PostCode"
      getValueFor(0, 3, soleTraderView) mustBe "Sole Trader"

    }

    "displaying primary contact details section for sole trader" in {

      soleTraderView.getElementsByClass("govuk-heading-m").get(1).text() must include(
        messages("account.viewSubscription.primaryContactDetails.label")
      )
      getKeyFor(1, 0, soleTraderView) must containMessage(
        "account.viewSubscription.primaryContactDetails.fullName"
      )
      getKeyFor(1, 1, soleTraderView) must containMessage(
        "account.viewSubscription.primaryContactDetails.jobTitle"
      )
      getKeyFor(1, 2, soleTraderView) must containMessage(
        "account.viewSubscription.primaryContactDetails.address"
      )
      getKeyFor(1, 3, soleTraderView) must containMessage(
        "account.viewSubscription.primaryContactDetails.phoneNumber"
      )
      getKeyFor(1, 4, soleTraderView) must containMessage(
        "account.viewSubscription.primaryContactDetails.email"
      )

      getValueFor(1,
                  0,
                  soleTraderView
      ) mustBe subscriptionSoleTrader.primaryContactDetails.fullName.get
      getValueFor(1,
                  1,
                  soleTraderView
      ) mustBe subscriptionSoleTrader.primaryContactDetails.jobTitle.get
      getValueFor(1, 2, soleTraderView) mustBe "addressLine1 line2 Town PostCode"

      getValueFor(1,
                  3,
                  soleTraderView
      ) mustBe subscriptionSoleTrader.primaryContactDetails.phoneNumber.get
      getValueFor(1,
                  4,
                  soleTraderView
      ) mustBe subscriptionSoleTrader.primaryContactDetails.email.get

      getChangeLinkFor(1, 0, soleTraderView) must haveHref(
        homeRoutes.ViewSubscriptionController.displayPage()
      )
    }

    "displaying business contact details section for sole trader" in {

      soleTraderView.getElementsByClass("govuk-heading-m").get(2).text() must include(
        messages("account.viewSubscription.businessDetails.title")
      )

      getKeyFor(2, 0, soleTraderView) must containMessage(
        "account.viewSubscription.businessDetails.phoneNumber"
      )
      getKeyFor(2, 1, soleTraderView) must containMessage(
        "account.viewSubscription.businessDetails.email"
      )
      getValueFor(2,
                  0,
                  soleTraderView
      ) mustBe subscriptionSoleTrader.primaryContactDetails.phoneNumber.get
      getValueFor(2,
                  1,
                  soleTraderView
      ) mustBe subscriptionSoleTrader.primaryContactDetails.email.get

    }

    "display 'Save and Continue' button" in {

      ukCompanyView.getElementById("submit").text() mustBe "Save and Continue"
    }
  }

}
