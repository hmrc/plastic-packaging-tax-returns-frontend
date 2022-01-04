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

package uk.gov.hmrc.plasticpackagingtax.returns.views.home

import org.mockito.Mockito.when
import org.scalatest.matchers.must.Matchers
import play.twirl.api.Html
import uk.gov.hmrc.plasticpackagingtax.returns.base.unit.UnitViewSpec
import uk.gov.hmrc.plasticpackagingtax.returns.config.AppConfig
import uk.gov.hmrc.plasticpackagingtax.returns.models.subscription.subscriptionDisplay.SubscriptionDisplayResponse
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.home.home_page
import uk.gov.hmrc.plasticpackagingtax.returns.views.tags.ViewTest

@ViewTest
class HomePageViewSpec extends UnitViewSpec with Matchers {

  private val homePage  = instanceOf[home_page]
  private val appConfig = instanceOf[AppConfig]

  private val subscription = mock[SubscriptionDisplayResponse]
  when(subscription.entityName).thenReturn(Some("Organisation Name"))

  val completeReturnUrl = "/complete-return-url"

  private def createView(): Html =
    homePage(subscription, completeReturnUrl)(journeyRequest, messages)

  override def exerciseGeneratedRenderingMethods(): Unit = {
    homePage.f(subscription, "url")(journeyRequest, messages)
    homePage.render(subscription, "url", journeyRequest, messages)
  }

  "Home Page view" should {

    val view: Html = createView()

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

      view.getElementById("title").text() mustBe "Organisation Name"
      view.getElementById("section-header") must containMessage("account.homePage.section")
    }

    "displayPPT reference number" in {

      view.select(".govuk-body .govuk-label--s").text() must include(journeyRequest.pptReference)
    }

    "display 'returns' card" in {

      val card = view.select(".card .card-body").get(0)

      card.select(".govuk-heading-m").first() must containMessage(
        "account.homePage.card.makeReturn.header"
      )
      card.select(".govuk-body").first() must containMessage(
        "account.homePage.card.makeReturn.body"
      )
      card.select(".govuk-link").first() must containMessage(
        "account.homePage.card.makeReturn.link"
      )
      card.select(".govuk-link").first() must haveHref(completeReturnUrl)
    }

    "display 'balance' card" in {

      val card = view.select(".card .card-body").get(1)

      card.select(".govuk-heading-m").first() must containMessage(
        "account.homePage.card.balance.header"
      )
      card.select(".govuk-body").first() must containMessage("account.homePage.card.balance.body")
    }

    "display 'business details' card for partnership" in {
      when(subscription.organisationType).thenReturn(Some("Partnership"))
      val view =
        homePage(subscription, completeReturnUrl)(journeyRequest, messages)
      val card = view.select(".govuk-grid-column-one-third .column-one-third").get(0)

      card.select(".govuk-body").get(0) must containMessage(
        "account.homePage.card.registration.details.body"
      )
      card.select(".govuk-link").get(0) must containMessage(
        "account.homePage.card.registration.details.link.partnership"
      )
      card.select(".govuk-link").get(0) must haveHref(appConfig.pptRegistrationAmendUrl)
    }

    "display 'business details' card for single" in {
      when(subscription.organisationType).thenReturn(Some("UK Company"))
      val view =
        homePage(subscription, completeReturnUrl)(journeyRequest, messages)
      val card = view.select(".govuk-grid-column-one-third .column-one-third").get(0)

      card.select(".govuk-body").get(0) must containMessage(
        "account.homePage.card.registration.details.body"
      )
      card.select(".govuk-link").get(0) must containMessage(
        "account.homePage.card.registration.details.link.single"
      )
      card.select(".govuk-link").get(0) must haveHref(appConfig.pptRegistrationAmendUrl)
    }

    "display 'business details' card for group" in {
      when(subscription.organisationType).thenReturn(Some("UK Company"))
      when(subscription.isGroup).thenReturn(true)
      val view =
        homePage(subscription, completeReturnUrl)(journeyRequest, messages)
      val card = view.select(".govuk-grid-column-one-third .column-one-third").get(0)

      card.select(".govuk-body").get(0) must containMessage(
        "account.homePage.card.registration.details.body"
      )
      card.select(".govuk-link").get(0) must containMessage(
        "account.homePage.card.registration.details.link.group"
      )
      card.select(".govuk-link").get(0) must haveHref(appConfig.pptRegistrationAmendUrl)
    }
  }
}
