/*
 * Copyright 2026 HM Revenue & Customs
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
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.scalatest.matchers.must.Matchers
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import config.FrontendAppConfig
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.twirl.api.Html
import support.ViewMatchers
import views.html.returns.ReturnConfirmationView
import views.html.amends.AmendConfirmation
import views.html.changeGroupLead.NewGroupLeadConfirmationView

class UserResearchBannerSpec extends ViewSpecBase with ViewMatchers with Matchers {

  private def appWith(bannerEnabled: Boolean): Application =
    new GuiceApplicationBuilder()
      .configure(
        "features.user-research-banner" -> bannerEnabled,
        "metrics.jvm"                   -> false,
        "metrics.enabled"               -> false
      )
      .build()

  val appConfig: FrontendAppConfig = inject[FrontendAppConfig]
  val messagesApi: MessagesApi     = inject[MessagesApi]

  private val returnConfirmationPage       = inject[ReturnConfirmationView]
  private val amendConfirmationPage        = inject[AmendConfirmation]
  private val newGroupLeadConfirmationPage = inject[NewGroupLeadConfirmationView]

  private lazy val bannerOn  = appWith(bannerEnabled = true)
  private lazy val bannerOff = appWith(bannerEnabled = false)

  private def welshMessages: Messages = MessagesImpl(Lang("cy"), messagesApi)

  private def optedInPages(app: Application, msgs: Messages): Seq[(String, Html)] = {
    val injector = app.injector
    Seq(
      "return_confirmation" ->
        injector.instanceOf[ReturnConfirmationView].apply(None, false)(request, msgs),
      "amend_confirmation" ->
        injector.instanceOf[AmendConfirmation].apply(Some("1234"))(request, msgs),
      "new_group_lead_confirmation" ->
        injector.instanceOf[NewGroupLeadConfirmationView].apply()(request, msgs)
    )
  }

  private def asElement(html: Html): Element = Jsoup.parse(html.toString()).body()

  private def containUserResearchBannerEnglish(el: Element): Unit = {
    el.select(".hmrc-user-research-banner").size() mustBe 1
    el.select(".hmrc-user-research-banner__link").attr("href") must include("https://banner-en")
  }

  private def containUserResearchBannerWelsh(el: Element): Unit = {
    el.select(".hmrc-user-research-banner").size() mustBe 1
    val link = el.select(".hmrc-user-research-banner__link")
    link.attr("href") must include("https://banner-cy")
    link.text must include("Ymunwch â’n panel ymchwil (yn agor tab newydd)")
  }

  "The user research banner" should {

    "be displayed on every opted-in page in English when the feature switch is enabled" in {

      implicit val msgs: Messages = MessagesImpl(Lang("en"), messagesApi)

      optedInPages(bannerOn, msgs).foreach { case (name, html) =>
        withClue(s"$name: ")(containUserResearchBannerEnglish(asElement(html)))
      }
    }

    "be displayed on every opted-in page in Welsh when the feature switch is enabled" in {
      implicit val msgs: Messages = welshMessages
      optedInPages(bannerOn, msgs).foreach { case (name, html) =>
        withClue(s"$name: ")(containUserResearchBannerWelsh(asElement(html)))
      }
    }

    "not be displayed when the feature switch is disabled" in {
      val disabledMessages = MessagesImpl(Lang("en"), messagesApi)
      optedInPages(bannerOff, disabledMessages).foreach { case (name, html) =>
        withClue(s"$name: ")(asElement(html).select(".hmrc-user-research-banner").size() mustBe 0)
      }
    }
  }
}
