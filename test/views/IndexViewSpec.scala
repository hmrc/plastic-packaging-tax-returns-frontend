/*
 * Copyright 2023 HM Revenue & Customs
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
import config.{Features, FrontendAppConfig}
import controllers.payments.{routes => paymentRoute}
import controllers.returns.routes
import models.obligations.PPTObligations
import models.subscription.LegalEntityDetails
import org.jsoup.nodes.Element
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.mock
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.twirl.api.Html
import repositories.SessionRepository
import support.ObligationsTestData.{noneDueUpToDate, oneDueOneOverdue, oneDueTwoOverdue, oneDueUpToDate}
import support.{ViewAssertions, ViewMatchers}
import views.ObligationScenarioTypes.{NoneDueUpToDate, OneDueOneOverdue, OneDueTwoOverdue, OneDueUpToDate}
import views.SubscriptionTypes.{Group, Partnership, SingleEntity}
import views.html.IndexView

object ObligationScenarioTypes extends Enumeration {
  type ObligationScenarioTyp = Value

  val NoneDueUpToDate, OneDueUpToDate, OneDueOneOverdue, OneDueTwoOverdue = Value
}

object SubscriptionTypes extends Enumeration {
  type SubscriptionType = Value

  val SingleEntity, Group, Partnership = Value
}

class IndexPageViewSpec
    extends ViewSpecBase with ViewAssertions with ViewMatchers {


  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .overrides(
      bind[FrontendAppConfig].toInstance(appConfig),
      bind[SessionRepository].toInstance(mock[SessionRepository])
   ).build()

  lazy val homePage = inject[IndexView]
  private val appConfig = mock[FrontendAppConfig]
  private val singleEntitySubscription = mock[LegalEntityDetails]
  private val groupSubscription = mock[LegalEntityDetails]
  private val partnershipSubscription = mock[LegalEntityDetails]
  val completeReturnUrl = "/complete-return-url"
  val pptFinancials = Some("You owe £100")

  when(singleEntitySubscription.entityName).thenReturn("Single entity subscription")
  when(groupSubscription.entityName).thenReturn("Group subscription")
  when(groupSubscription.isGroup).thenReturn(true)
  when(partnershipSubscription.entityName).thenReturn("Partnership subscription")
  when(partnershipSubscription.isPartnership).thenReturn(true)
  when(appConfig.pptCompleteReturnGuidanceUrl).thenReturn(completeReturnUrl)
  when(appConfig.pptRegistrationManagePartnersUrl).thenReturn("pptRegistrationManagePartnersUrl")
  when(appConfig.pptRegistrationDeregisterUrl).thenReturn("pptRegistrationDeregisterUrl")
  when(appConfig.pptRegistrationAmendUrl).thenReturn("pptRegistrationAmendUrl")
  when(appConfig.pptRegistrationManageGroupUrl).thenReturn("pptRegistrationManageGroupUrl")
  when(appConfig.userResearchUrl).thenReturn("ur-url")
  when(appConfig.isDeRegistrationFeatureEnabled).thenReturn(true)
  when(appConfig.isFeatureEnabled(Features.paymentsEnabled)).thenReturn(true)


  private def createView(
    subscription: LegalEntityDetails,
    obligations: Option[PPTObligations]
  ): Html =
    homePage(
      subscription,
      obligations,
      true,
      pptFinancials,
      "XMPPT0000000001"
    )(request, messages)

  "Home Page view" should {

    val view: Html = createView(singleEntitySubscription, Some(noneDueUpToDate))

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
      view.getElementsByClass("govuk-heading-l").text() mustBe messages(
        "account.homePage.title"
      )
    }

    "contain ppt Reference number" in {
      val pptReference = view.getElementById("id-ppt-reference")

      pptReference.text() must include(messages("account.homePage.registrationNumber", "XMPPT0000000001"))
      pptReference.text() must include(singleEntitySubscription.entityName)
    }

    "display 'Set up or manage a Direct Debit link'" in {

      view.getElementById("direct-debit-link").text() mustBe "Set up or manage a Direct Debit"
      view.getElementById("direct-debit-link") must haveHref(
        paymentRoute.DirectDebitController.redirectLink
      )
    }


    Seq(
      (NoneDueUpToDate, noneDueUpToDate),
      (OneDueUpToDate, oneDueUpToDate),
      (OneDueOneOverdue, oneDueOneOverdue),
      (OneDueTwoOverdue, oneDueTwoOverdue)
    ).foreach {

      case (obligationType, obligations) =>
        Seq(
          (SingleEntity, singleEntitySubscription),
          (Group, groupSubscription),
          (Partnership, partnershipSubscription)
        ).foreach {

          case (subscriptionType, subscription) =>
            val view: Html = createView(subscription, Some(obligations))

            s"displaying $subscriptionType subscription" when {

              s"$obligationType obligations" should {


                "display 'returns' card" in {

                  val card = view.select(".card .card-body").get(0)

                  card.select(".govuk-heading-m").first() must containMessage(
                    "account.homePage.card.makeReturn.header"
                  )

                  val createLink = (
                    messages("account.homePage.card.makeReturn.line3.createLink"),
                    routes.StartYourReturnController.onPageLoad().url
                  )

                  val returnsCreationGuidanceLink =
                    (messages("account.homePage.card.makeReturn.guidance.link"), completeReturnUrl)

                  obligationType match {
                    case NoneDueUpToDate =>
                      val returnDetails =
                        Seq(messages("account.homePage.card.makeReturn.line1.none"))

                      val returnLinks = Seq(returnsCreationGuidanceLink)
                      assertReturnsCardDetail(card, returnDetails, returnLinks)

                    case OneDueUpToDate =>
                      val returnDetails =
                        Seq(
                          messages(
                            "account.homePage.card.makeReturn.line2.due",
                            ViewUtils.displayReturnQuarter(obligations.nextObligation.get),
                            ViewUtils.displayLocalDate(
                              obligations.nextObligation.get.dueDate.minusDays(
                                obligations.nextObligation.get.dueDate.getDayOfMonth - 1
                              )
                            ),
                            ViewUtils.displayLocalDate(obligations.nextObligation.get.dueDate)
                          )
                        )

                      val returnLinks = Seq(createLink, returnsCreationGuidanceLink)
                      assertReturnsCardDetail(card, returnDetails, returnLinks)

                    case OneDueOneOverdue =>
                      val returnDetails =
                        Seq(
                          messages(
                            "account.homePage.card.makeReturn.line1.singleOverdue",
                            ViewUtils.displayReturnQuarter(obligations.oldestOverdueObligation.get)
                          ),
                          messages(
                            "account.homePage.card.makeReturn.line2.due",
                            ViewUtils.displayReturnQuarter(obligations.nextObligation.get),
                            ViewUtils.displayLocalDate(
                              obligations.nextObligation.get.dueDate.minusDays(
                                obligations.nextObligation.get.dueDate.getDayOfMonth - 1
                              )
                            ),
                            ViewUtils.displayLocalDate(obligations.nextObligation.get.dueDate)
                          )
                        )

                      val returnLinks = Seq(createLink, returnsCreationGuidanceLink)
                      assertReturnsCardDetail(card, returnDetails, returnLinks)

                    case OneDueTwoOverdue =>
                      val returnDetails =
                        Seq(
                          messages(
                            "account.homePage.card.makeReturn.line1.multipleOverdue",
                            obligations.overdueObligationCount
                          ),
                          messages(
                            "account.homePage.card.makeReturn.line2.due",
                            ViewUtils.displayReturnQuarter(obligations.nextObligation.get),
                            ViewUtils.displayLocalDate(
                              obligations.nextObligation.get.dueDate.minusDays(
                                obligations.nextObligation.get.dueDate.getDayOfMonth - 1
                              )
                            ),
                            ViewUtils.displayLocalDate(obligations.nextObligation.get.dueDate)
                          )
                        )

                      val returnLinks = Seq(createLink, returnsCreationGuidanceLink)
                      assertReturnsCardDetail(card, returnDetails, returnLinks)

                  }
                }

                "display 'balance' card" in {

                  val card = view.select(".card .card-body").get(1)

                  card.select(".govuk-heading-m").first() must containMessage(
                    "account.homePage.card.payments.header"
                  )
                  card.select(".govuk-body").first().text() mustBe "You owe £100"
                }

                "display account management heading" in {
                  view.select("h2").text() must include(
                    messages("account.homePage.manage.ppt.account.header")
                  )

                }

                "display account management sections" in {

                  val coreManagement       = view.getElementById("core-management")
                  val additionalManagement = view.getElementById("additional-management")
                  val deregister           = view.getElementById("deregister")

                  subscriptionType match {
                    case SingleEntity =>
                      coreManagement.select("h3").text() must include(
                        messages("account.homePage.card.registration.details.1.link.single")
                      )

                      coreManagement.select("p").text() mustBe messages(
                        "account.homePage.card.registration.details.1.body"
                      )

                      coreManagement.select("a").first() must haveHref(
                        "pptRegistrationAmendUrl"
                      )

                      checkDeregisterCard(deregister)

                    case Group =>
                      coreManagement.select("h3").text() must include(
                        messages("account.homePage.card.registration.details.1.link.group")
                      )

                      coreManagement.select("p").text() mustBe messages(
                        "account.homePage.card.registration.details.1.body"
                      )

                      coreManagement.select("a").first() must haveHref(
                        "pptRegistrationAmendUrl"
                      )

                      additionalManagement.select("h3").text() must include(
                        messages("account.homePage.card.registration.details.2.link.group")
                      )

                      additionalManagement.select("p").text() mustBe messages(
                        "account.homePage.card.registration.details.2.body.group"
                      )

                      additionalManagement.select("a").first() must haveHref(
                        "pptRegistrationManageGroupUrl"
                      )

                      checkDeregisterCard(deregister)

                    case Partnership =>
                      coreManagement.select("h3").text() must include(
                        messages("account.homePage.card.registration.details.1.link.partnership")
                      )

                      coreManagement.select("p").text() mustBe messages(
                        "account.homePage.card.registration.details.1.body"
                      )

                      coreManagement.select("a").first() must haveHref(
                        "pptRegistrationAmendUrl"
                      )

                      additionalManagement.select("h3").text() must include(
                        messages("account.homePage.card.registration.details.2.link.partnership")
                      )

                      additionalManagement.select("p").text() mustBe messages(
                        "account.homePage.card.registration.details.2.body.partnership"
                      )

                      additionalManagement.select("a").first() must haveHref(
                        "pptRegistrationManagePartnersUrl"
                      )

                      checkDeregisterCard(deregister)

                  }
                }
              }
            }
        }
    }

    "not render the de-registration link" when {
      "deregistration enabled feature flag is false" in {

        when(appConfig.isDeRegistrationFeatureEnabled).thenReturn(false)

        val view: Html = createView(groupSubscription, Some(oneDueOneOverdue))

        view.getElementById("deregister") mustBe null

      }
    }

    "render the de-registration link" when {
      "deregistration enabled feature flag is true" in {

        when(appConfig.isDeRegistrationFeatureEnabled).thenReturn(true)

        val view: Html = createView(groupSubscription, Some(oneDueOneOverdue))

        view.getElementById("deregister") must not be null

      }
    }
  }

  private def checkDeregisterCard(deregister: Element) = {

    deregister.select("h3").text() must include(messages("account.homePage.card.deregister.link"))
    deregister.select("p").text() mustBe messages("account.homePage.card.deregister.body")
    deregister.select("a").first() must haveHref("pptRegistrationDeregisterUrl")

  }

  "get obligations fails" should {
    "inform the user" in {

      val viewWithoutObligations: Html = createView(singleEntitySubscription, None)
      val card                         = viewWithoutObligations.select(".card .card-body").get(0)

      card.select(".govuk-body").first() must containMessage(
        "account.homePage.card.makeReturn.failure"
      )

    }
  }

  private def assertReturnsCardDetail(
    card: Element,
    returnDetails: Seq[String],
    returnLinks: Seq[(String, String)]
  ): Unit = {

    val returnCardDetails = card.select(".govuk-body")

    returnDetails.zipWithIndex.foreach {
      case (returnDetail, idx) => returnCardDetails.get(idx).text() must include(returnDetail)
    }

    val returnCardLinks = card.select(".govuk-link")

    returnLinks.zipWithIndex.foreach {
      case (linkDetail, idx) =>
        returnCardLinks.get(idx).text() must include(linkDetail._1)
        returnCardLinks.get(idx) must haveHref(linkDetail._2)
    }

  }

}
