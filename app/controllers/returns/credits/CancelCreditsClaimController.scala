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

package controllers.returns.credits

import controllers.actions._

import javax.inject.Inject
import models.Mode
import navigation.{Navigator, ReturnsJourneyNavigator}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import connectors.CacheConnector
import forms.returns.credits.CancelCreditsClaimFormProvider
import pages.returns.credits.CancelCreditsClaimPage
import uk.gov.hmrc.http.HttpVerbs.GET
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.returns.credits.CancelCreditsClaimView

import scala.concurrent.{ExecutionContext, Future}

class CancelCreditsClaimController @Inject()(
                                              override val messagesApi: MessagesApi,
                                              cacheConnector: CacheConnector,
                                              navigator: ReturnsJourneyNavigator,
                                              journeyAction: JourneyAction,
                                              form: CancelCreditsClaimFormProvider,
                                              val controllerComponents: MessagesControllerComponents,
                                              view: CancelCreditsClaimView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {


  def onPageLoad: Action[AnyContent] = journeyAction {
    implicit request =>

      val preparedForm = request.userAnswers.fill(CancelCreditsClaimPage, form())

      Ok(view(preparedForm))
  }

  def onSubmit: Action[AnyContent] = journeyAction.async {
    implicit request =>

      form().bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors))),

        value =>
          Future.successful(Redirect(Call(GET, "/foo")))
      )
  }
}
