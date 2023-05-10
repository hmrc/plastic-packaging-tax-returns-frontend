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

import connectors.CacheConnector
import controllers.actions._
import forms.returns.credits.CreditsClaimedListFormProvider
import models.Mode
import navigation.Navigator
import pages.returns.credits.CreditsClaimedListPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.returns.credits.CreditsClaimedListView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CreditsClaimedListController @Inject()(
                                              override val messagesApi: MessagesApi,
                                              cacheConnector: CacheConnector,
                                              navigator: Navigator,
                                              journeyAction: JourneyAction,
                                              formProvider: CreditsClaimedListFormProvider,
                                              val controllerComponents: MessagesControllerComponents,
                                              view: CreditsClaimedListView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = journeyAction {
    implicit request =>

      val preparedForm = request.userAnswers.fill(CreditsClaimedListPage, formProvider())

      Ok(view(preparedForm, Seq.empty, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = journeyAction.async {
    implicit request =>

      formProvider().bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, Seq.empty, mode))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(CreditsClaimedListPage, value))
            _              <- cacheConnector.set(request.pptReference, updatedAnswers)
          } yield Redirect(navigator.nextPage(CreditsClaimedListPage, mode, updatedAnswers))
      )
  }
}
