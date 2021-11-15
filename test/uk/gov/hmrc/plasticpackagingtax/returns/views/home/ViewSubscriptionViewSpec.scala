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
import uk.gov.hmrc.plasticpackagingtax.returns.base.PptTestData.{
  createSubscriptionDisplayResponse,
  soleTraderSubscription,
  ukLimitedCompanySubscription
}
import uk.gov.hmrc.plasticpackagingtax.returns.base.unit.UnitViewSpec
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.home.{routes => homeRoutes}
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.subscriptions.view_subscription_page
import uk.gov.hmrc.plasticpackagingtax.returns.views.tags.ViewTest

@ViewTest
class ViewSubscriptionViewSpec extends UnitViewSpec with Matchers {

  private val page = instanceOf[view_subscription_page]

  private val ukCompanyView =
    page(createSubscriptionDisplayResponse(ukLimitedCompanySubscription))(request, messages)

  private val soleTraderView =
    page(createSubscriptionDisplayResponse(soleTraderSubscription))(request, messages)

  override def exerciseGeneratedRenderingMethods(): Unit = {
    page.f(createSubscriptionDisplayResponse(ukLimitedCompanySubscription))(request, messages)
    page.render(createSubscriptionDisplayResponse(ukLimitedCompanySubscription), request, messages)
  }

  "ViewSubscriptionView" should {

    def getKeyFor(section: Int, index: Int, view: Document) =
      view.getElementsByClass("govuk-summary-list").get(section).getElementsByClass(
        "govuk-summary-list__key"
      ).get(index)
    def getValueFor(section: Int, index: Int, view: Document) =
      view.getElementsByClass("govuk-summary-list").get(section).getElementsByClass(
        "govuk-summary-list__value"
      ).get(index).text()

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

      getValueFor(0, 1, ukCompanyView) mustBe "2-3 Scala Street Soho London W1T 2HN"
      getValueFor(0,
                  2,
                  ukCompanyView
      ) mustBe ukLimitedCompanySubscription.legalEntityDetails.customerDetails.organisationDetails.get.organisationType.get
      getValueFor(0,
                  0,
                  ukCompanyView
      ) mustBe ukLimitedCompanySubscription.legalEntityDetails.customerDetails.organisationDetails.get.organisationName.get
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
      ) mustBe ukLimitedCompanySubscription.primaryContactDetails.name
      getValueFor(1,
                  1,
                  ukCompanyView
      ) mustBe ukLimitedCompanySubscription.primaryContactDetails.positionInCompany
      getValueFor(1, 2, ukCompanyView) mustBe "addressLine1 line2 Town PostCode"
      getValueFor(1,
                  3,
                  ukCompanyView
      ) mustBe ukLimitedCompanySubscription.primaryContactDetails.contactDetails.telephone
      getValueFor(1,
                  4,
                  ukCompanyView
      ) mustBe ukLimitedCompanySubscription.primaryContactDetails.contactDetails.email

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
      ) mustBe soleTraderSubscription.legalEntityDetails.customerDetails.individualDetails.get.firstName
      getValueFor(0,
                  1,
                  soleTraderView
      ) mustBe soleTraderSubscription.legalEntityDetails.customerDetails.individualDetails.get.lastName
      getValueFor(0, 2, soleTraderView) mustBe "2-3 Scala Street Soho London W1T 2HN"
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

      getValueFor(1, 0, soleTraderView) mustBe soleTraderSubscription.primaryContactDetails.name
      getValueFor(1,
                  1,
                  soleTraderView
      ) mustBe soleTraderSubscription.primaryContactDetails.positionInCompany
      getValueFor(1, 2, soleTraderView) mustBe "addressLine1 line2 Town PostCode"

      getValueFor(1,
                  3,
                  soleTraderView
      ) mustBe soleTraderSubscription.primaryContactDetails.contactDetails.telephone
      getValueFor(1,
                  4,
                  soleTraderView
      ) mustBe soleTraderSubscription.primaryContactDetails.contactDetails.email

    }

  }

}
