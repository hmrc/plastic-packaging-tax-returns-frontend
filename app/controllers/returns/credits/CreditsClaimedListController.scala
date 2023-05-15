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
import models.requests.DataRequest
import navigation.ReturnsJourneyNavigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.returns.credits.CreditsClaimedListSummary
import views.html.returns.credits.CreditsClaimedListView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class CreditsClaimedListController @Inject()(
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  navigator: ReturnsJourneyNavigator,
  journeyAction: JourneyAction,
  formProvider: CreditsClaimedListFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: CreditsClaimedListView
)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = journeyAction {
    implicit request =>
      Ok(createView(formProvider(), mode))
  }

  private def createView(form: Form[Boolean], mode: Mode) (implicit request: DataRequest[_]) = {
    view(form, CreditsClaimedListSummary.createRows(request.userAnswers, navigator), mode)
  }

  def onSubmit(mode: Mode): Action[AnyContent] = journeyAction {
    implicit request =>

      formProvider().bindFromRequest().fold(
        formWithErrors =>
          BadRequest(createView(formWithErrors, mode)),

        isAddingAnotherYear =>
          Redirect(navigator.creditClaimedList(mode, isAddingAnotherYear, request.userAnswers))
      )
  }
}
