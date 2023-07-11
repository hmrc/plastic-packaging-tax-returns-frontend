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

package controllers.returns

import connectors.CacheConnector
import controllers.actions._
import controllers.helpers.NonExportedAmountHelper
import forms.returns.PlasticExportedByAnotherBusinessFormProvider
import models.Mode
import models.requests.DataRequest._
import navigation.ReturnsJourneyNavigator
import pages.returns.AnotherBusinessExportedPage
import play.api.data.FormBinding.Implicits.formBinding
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Results.{BadRequest, Ok, Redirect}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import views.html.returns.PlasticExportedByAnotherBusinessView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PlasticExportedByAnotherBusinessController @Inject()(
                                                            override val messagesApi: MessagesApi,
                                                            cacheConnector: CacheConnector,
                                                            journeyAction: JourneyAction,
                                                            formProvider: PlasticExportedByAnotherBusinessFormProvider,
                                                            returnsNavigator: ReturnsJourneyNavigator,
                                                            nonExportedAmountHelper: NonExportedAmountHelper,
                                                            val controllerComponents: MessagesControllerComponents,
                                                            view: PlasticExportedByAnotherBusinessView
                                 )(implicit ec: ExecutionContext) extends I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = journeyAction {
    implicit request =>

      val preparedForm = request.userAnswers.fill(AnotherBusinessExportedPage, formProvider())

      nonExportedAmountHelper.totalPlasticAdditions(request.userAnswers).fold(
        Redirect(controllers.routes.IndexController.onPageLoad))(
        totalPlastic => Ok(view(preparedForm, mode, totalPlastic))
      )
  }

  def onSubmit(mode: Mode): Action[AnyContent] = journeyAction.async {
    implicit request =>
      val pptId: String = request.pptReference

      formProvider().bindFromRequest().fold(
        formWithErrors =>{
          nonExportedAmountHelper.totalPlasticAdditions(request.userAnswers).fold(
            Future.successful(Redirect(controllers.routes.IndexController.onPageLoad)))(
            totalPlastic => Future.successful(BadRequest(view(formWithErrors, mode, totalPlastic))))
        },
        value =>
            request.userAnswers
              .setOrFail(AnotherBusinessExportedPage, value)
              .save(cacheConnector.saveUserAnswerFunc(pptId))
              .map(_ => Redirect(returnsNavigator.exportedByAnotherBusinessRoute(value, mode)))
      )
  }
}
