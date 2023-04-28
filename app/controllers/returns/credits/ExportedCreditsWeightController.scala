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
import controllers.actions.JourneyAction
import forms.returns.credits.ExportedCreditsWeightFormProvider
import models.Mode
import models.requests.DataRequest.headerCarrier
import models.returns.CreditsAnswer
import navigation.ReturnsJourneyNavigator
import pages.returns.credits.ExportedCreditsPage
import play.api.data.FormBinding.Implicits.formBinding
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Results.{BadRequest, Ok}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Results}
import views.html.returns.credits.ExportedCreditsWeightView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ExportedCreditsWeightController @Inject()(
  override val messagesApi: MessagesApi,
  journeyAction: JourneyAction,
  cacheConnector: CacheConnector,
  val controllerComponents: MessagesControllerComponents,
  view: ExportedCreditsWeightView,
  formProvider: ExportedCreditsWeightFormProvider,
  navigator: ReturnsJourneyNavigator
)
  (implicit ec: ExecutionContext) extends I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] =
    journeyAction {
      implicit request =>
        val form = request.userAnswers.genericFill(ExportedCreditsPage, formProvider(), CreditsAnswer.fillFormWeight)
        Ok(view(form, mode))
    }


  def onSubmit(mode: Mode): Action[AnyContent] = journeyAction.async {
      implicit request =>
        formProvider().bindFromRequest().fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
          formValue => {
            request.userAnswers
              .setOrFail(ExportedCreditsPage, CreditsAnswer.answerWeightWith(formValue))
              .save(cacheConnector.saveUserAnswerFunc(request.pptReference))
              .map(_ => Results.Redirect(navigator.exportedCreditsWeight(mode)))
          }
      )
    }


}
