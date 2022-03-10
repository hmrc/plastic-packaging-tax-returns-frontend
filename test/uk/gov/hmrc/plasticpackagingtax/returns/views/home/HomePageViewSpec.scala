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
import uk.gov.hmrc.plasticpackagingtax.returns.views.home.SubscriptionTypes.{
  Group,
  Partnership,
  SingleEntity
}
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.home.home_page
import uk.gov.hmrc.plasticpackagingtax.returns.views.tags.ViewTest

object SubscriptionTypes extends Enumeration {
  type SubscriptionType = Value

  val SingleEntity, Group, Partnership = Value
}

@ViewTest
class HomePageViewSpec extends UnitViewSpec with Matchers {

  private val homePage  = instanceOf[home_page]
  private val appConfig = instanceOf[AppConfig]

  private val singleEntitySubscription = mock[SubscriptionDisplayResponse]
  when(singleEntitySubscription.entityName).thenReturn("Single entity subscription")

  private val groupSubscription = mock[SubscriptionDisplayResponse]
  when(groupSubscription.entityName).thenReturn("Group subscription")
  when(groupSubscription.isGroup).thenReturn(true)

  private val partnershipSubscription = mock[SubscriptionDisplayResponse]
  when(partnershipSubscription.entityName).thenReturn("Partnership subscription")
  when(partnershipSubscription.isPartnership).thenReturn(true)

  val completeReturnUrl = "/complete-return-url"

  private def createView(subscription: SubscriptionDisplayResponse): Html =
    homePage(subscription, completeReturnUrl)(journeyRequest, messages)

  override def exerciseGeneratedRenderingMethods(): Unit = {
    homePage.f(singleEntitySubscription, "url")(journeyRequest, messages)
    homePage.render(singleEntitySubscription, "url", journeyRequest, messages)
  }

  "Home Page view" when {

    Seq((SingleEntity, singleEntitySubscription),
        (Group, groupSubscription),
        (Partnership, partnershipSubscription)
    ).foreach {
      case (subscriptionType, subscription) =>
        val view: Html = createView(subscription)

        s"displaying $subscriptionType subscription" should {

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
            view.getElementById("title").text() mustBe messages("account.homePage.title")
          }

          "display PPT reference number" in {
            val mainText = view.select("main").text()

            mainText must include(
              messages("account.homePage.registrationNumber", journeyRequest.pptReference)
            )
            subscriptionType match {
              case SingleEntity =>
                mainText must include(subscription.entityName)
              case Group =>
                mainText must include(
                  messages("account.homePage.organisation.group", subscription.entityName)
                )
              case Partnership =>
                mainText must include(subscription.entityName)
            }
          }

          "display 'returns' card" in {
            val card = view.select(".card .card-body").get(0)

            card.select(".govuk-heading-m").first() must containMessage(
              "account.homePage.card.makeReturn.header"
            )
            card.select(".govuk-body").first() must containMessage(
              "account.homePage.card.makeReturn.body",
              "April",
              "June",
              "2022",
              "29 July 2022"
            )
            card.select(".govuk-link").first() must containMessage(
              "account.homePage.card.makeReturn.link"
            )
            card.select(".govuk-link").first() must haveHref(completeReturnUrl)

            card.select(".govuk-link").get(1) must containMessage(
              "account.homePage.card.makeReturn.link.2"
            )
            card.select(".govuk-link").get(1) must haveHref("")
          }

          "display 'balance' card" in {
            val card = view.select(".card .card-body").get(1)

            card.select(".govuk-heading-m").first() must containMessage(
              "account.homePage.card.balance.header"
            )
            card.select(".govuk-body").first() must containMessage(
              "account.homePage.card.balance.body"
            )
          }

          "display account management heading" in {
            view.select("h2").text() must include(
              messages("account.homePage.manage.ppt.account.header")
            )
          }

          "display account management sections" in {
            val coreManagement       = view.getElementById("core-management")
            val additionalManagement = view.getElementById("additional-management")

            subscriptionType match {
              case SingleEntity =>
                coreManagement.select("h3").text() must include(
                  messages("account.homePage.card.registration.details.1.link.single")
                )
                coreManagement.select("p").text() mustBe messages(
                  "account.homePage.card.registration.details.1.body"
                )
                coreManagement.select("a").first() must haveHref(appConfig.pptRegistrationAmendUrl)
              case Group =>
                coreManagement.select("h3").text() must include(
                  messages("account.homePage.card.registration.details.1.link.group")
                )
                coreManagement.select("p").text() mustBe messages(
                  "account.homePage.card.registration.details.1.body"
                )
                coreManagement.select("a").first() must haveHref(appConfig.pptRegistrationAmendUrl)
                additionalManagement.select("h3").text() must include(
                  messages("account.homePage.card.registration.details.2.link.group")
                )
                additionalManagement.select("p").text() mustBe messages(
                  "account.homePage.card.registration.details.2.body.group"
                )
                additionalManagement.select("a").first() must haveHref(
                  appConfig.pptRegistrationManageGroupUrl
                )
              case Partnership =>
                coreManagement.select("h3").text() must include(
                  messages("account.homePage.card.registration.details.1.link.partnership")
                )
                coreManagement.select("p").text() mustBe messages(
                  "account.homePage.card.registration.details.1.body"
                )
                coreManagement.select("a").first() must haveHref(appConfig.pptRegistrationAmendUrl)
                additionalManagement.select("h3").text() must include(
                  messages("account.homePage.card.registration.details.2.link.partnership")
                )
                additionalManagement.select("p").text() mustBe messages(
                  "account.homePage.card.registration.details.2.body.partnership"
                )
                additionalManagement.select("a").first() must haveHref(
                  appConfig.pptRegistrationManagePartnersUrl
                )
            }

          }
        }
    }
  }
}
