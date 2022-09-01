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

package controllers.returns.credits

import connectors.CacheConnector
import controllers.actions._
import forms.returns.credits.ConvertedCreditsFormProvider
import models.Mode
import navigation.ReturnsJourneyNavigator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.returns.credits.ConvertedCreditsView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ConvertedCreditsController @Inject()
(
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  navigator: ReturnsJourneyNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: ConvertedCreditsFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ConvertedCreditsView
)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      Ok(view(form, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      Redirect(navigator.ConvertedCreditsRoute(mode))

    //      form.bindFromRequest().fold(
    //        formWithErrors =>
    //          Future.successful(BadRequest(view(formWithErrors, mode))),
    //
    //        value =>
    //          for {
    //            updatedAnswers <- Future.fromTry(request.userAnswers.set(ConvertedCreditsPage, value))
    //            _ <- cacheConnector.set(request.pptReference, updatedAnswers)
    //          } yield Redirect(navigator.ConvertedCreditsRoute(mode))
    //      )
  }
}
