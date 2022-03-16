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

import org.jsoup.nodes.Element
import org.mockito.Mockito.when
import org.scalatest.matchers.must.Matchers
import play.api.test.FakeRequest
import play.twirl.api.Html
import uk.gov.hmrc.plasticpackagingtax.returns.base.ObligationsTestData.{
  noneDueUpToDate,
  oneDueOneOverdue,
  oneDueTwoOverdue,
  oneDueUpToDate
}
import uk.gov.hmrc.plasticpackagingtax.returns.base.PptTestData
import uk.gov.hmrc.plasticpackagingtax.returns.base.unit.UnitViewSpec
import uk.gov.hmrc.plasticpackagingtax.returns.config.AppConfig
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.returns.routes
import uk.gov.hmrc.plasticpackagingtax.returns.models.obligations.PPTObligations
import uk.gov.hmrc.plasticpackagingtax.returns.models.request.AuthenticatedRequest
import uk.gov.hmrc.plasticpackagingtax.returns.models.subscription.subscriptionDisplay.SubscriptionDisplayResponse
import uk.gov.hmrc.plasticpackagingtax.returns.views.home.ObligationScenarioTypes.{
  NoneDueUpToDate,
  OneDueOneOverdue,
  OneDueTwoOverdue,
  OneDueUpToDate
}
import uk.gov.hmrc.plasticpackagingtax.returns.views.home.SubscriptionTypes.{
  Group,
  Partnership,
  SingleEntity
}
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.home.home_page
import uk.gov.hmrc.plasticpackagingtax.returns.views.tags.ViewTest
import uk.gov.hmrc.plasticpackagingtax.returns.views.utils.ViewUtils
import utils.FakeRequestCSRFSupport.CSRFFakeRequest

object ObligationScenarioTypes extends Enumeration {
  type ObligationScenarioTyp = Value

  val NoneDueUpToDate, OneDueUpToDate, OneDueOneOverdue, OneDueTwoOverdue = Value
}

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

  val authenticatedRequest = new AuthenticatedRequest(FakeRequest().withCSRFToken,
                                                      PptTestData.newUser(),
                                                      Some("XMPPT0000000001")
  )

  private def createView(
    subscription: SubscriptionDisplayResponse,
    obligations: Option[PPTObligations]
  ): Html =
    homePage(subscription, obligations, completeReturnUrl, "XMPPT0000000001")(authenticatedRequest,
                                                                              messages
    )

  override def exerciseGeneratedRenderingMethods(): Unit = {
    homePage.f(singleEntitySubscription, Some(noneDueUpToDate), "url", "XMPPT0000000001")(
      authenticatedRequest,
      messages
    )
    homePage.render(singleEntitySubscription,
                    Some(noneDueUpToDate),
                    "url",
                    "XMPPT0000000001",
                    authenticatedRequest,
                    messages
    )
  }

  "Home Page view" when {

    Seq((NoneDueUpToDate, noneDueUpToDate),
        (OneDueUpToDate, oneDueUpToDate),
        (OneDueOneOverdue, oneDueOneOverdue),
        (OneDueTwoOverdue, oneDueTwoOverdue)
    ).foreach {
      case (obligationType, obligations) =>
        Seq((SingleEntity, singleEntitySubscription),
            (Group, groupSubscription),
            (Partnership, partnershipSubscription)
        ).foreach {
          case (subscriptionType, subscription) =>
            val view: Html = createView(subscription, Some(obligations))

            s"displaying $subscriptionType subscription" when {
              s"$obligationType obligations" should {

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

                  val createLink = (messages("account.homePage.card.makeReturn.line3.createLink"),
                                    routes.StartDateReturnController.displayPage().url
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
                          messages("account.homePage.card.makeReturn.line2.due",
                                   ViewUtils.displayReturnQuarter(obligations.nextObligation.get),
                                   ViewUtils.displayLocalDate(
                                     obligations.nextObligation.get.dueDate
                                   )
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
                          messages("account.homePage.card.makeReturn.line2.due",
                                   ViewUtils.displayReturnQuarter(obligations.nextObligation.get),
                                   ViewUtils.displayLocalDate(
                                     obligations.nextObligation.get.dueDate
                                   )
                          )
                        )
                      val returnLinks = Seq(createLink, returnsCreationGuidanceLink)
                      assertReturnsCardDetail(card, returnDetails, returnLinks)
                    case OneDueTwoOverdue =>
                      val returnDetails =
                        Seq(messages("account.homePage.card.makeReturn.line1.multipleOverdue",
                                     obligations.overdueObligationCount
                            ),
                            messages("account.homePage.card.makeReturn.line2.due",
                                     ViewUtils.displayReturnQuarter(obligations.nextObligation.get),
                                     ViewUtils.displayLocalDate(
                                       obligations.nextObligation.get.dueDate
                                     )
                            )
                        )
                      val returnLinks = Seq(createLink, returnsCreationGuidanceLink)
                      assertReturnsCardDetail(card, returnDetails, returnLinks)
                  }
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
                      coreManagement.select("a").first() must haveHref(
                        appConfig.pptRegistrationAmendUrl
                      )
                    case Group =>
                      coreManagement.select("h3").text() must include(
                        messages("account.homePage.card.registration.details.1.link.group")
                      )
                      coreManagement.select("p").text() mustBe messages(
                        "account.homePage.card.registration.details.1.body"
                      )
                      coreManagement.select("a").first() must haveHref(
                        appConfig.pptRegistrationAmendUrl
                      )
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
                      coreManagement.select("a").first() must haveHref(
                        appConfig.pptRegistrationAmendUrl
                      )
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
  ) = {
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
