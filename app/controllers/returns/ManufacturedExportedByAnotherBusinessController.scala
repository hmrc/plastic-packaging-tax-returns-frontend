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

package controllers.returns

import controllers.actions._
import forms.returns.ManufacturedExportedByAnotherBusinessFormProvider

import javax.inject.Inject
import models.Mode
import navigation.Navigator
import pages.returns.ManufacturedExportedByAnotherBusinessPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import connectors.CacheConnector
import controllers.helpers.NonExportedAmountHelper
import play.api.mvc.Results.{BadRequest, Ok, Redirect}
import views.html.returns.ManufacturedExportedByAnotherBusinessView
import play.api.data.FormBinding.Implicits.formBinding
import models.requests.DataRequest._

import scala.concurrent.{ExecutionContext, Future}

class ManufacturedExportedByAnotherBusinessController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         cacheConnector: CacheConnector,
                                         navigator: Navigator,
                                         journeyAction: JourneyAction,
                                         formProvider: ManufacturedExportedByAnotherBusinessFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: ManufacturedExportedByAnotherBusinessView
                                 )(implicit ec: ExecutionContext) extends I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = journeyAction {
    implicit request =>

      val preparedForm = request.userAnswers.fill(ManufacturedExportedByAnotherBusinessPage, formProvider())

      NonExportedAmountHelper.totalPlastic(request.userAnswers).fold(
        Redirect(controllers.routes.IndexController.onPageLoad))(
        totalPlastic => Ok(view(preparedForm, mode, totalPlastic))
      )
  }

  def onSubmit(mode: Mode): Action[AnyContent] = journeyAction.async {
    implicit request =>

      formProvider().bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, 200L))),

        value =>
            request.userAnswers
              .setOrFail(ManufacturedExportedByAnotherBusinessPage, value)
              .save(cacheConnector.saveUserAnswerFunc(request.pptReference))
              .map(_ => Redirect(Call("GET", "/foo")))

      )
  }
}
