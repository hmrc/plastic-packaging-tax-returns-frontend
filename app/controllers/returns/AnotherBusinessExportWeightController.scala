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

import connectors.CacheConnector
import controllers.actions._
import controllers.helpers.NonExportedAmountHelper
import forms.AnotherBusinessExportWeightFormProvider
import models.Mode
import navigation.ReturnsJourneyNavigator
import pages.returns.AnotherBusinessExportWeightPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.returns.AnotherBusinessExportWeightView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AnotherBusinessExportWeightController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        cacheConnector: CacheConnector,
                                        returnsNavigator: ReturnsJourneyNavigator,
                                        journeyAction: JourneyAction,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: AnotherBusinessExportWeightFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: AnotherBusinessExportWeightView
                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(AnotherBusinessExportWeightPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      NonExportedAmountHelper.getAmountAndDirectlyExportedAnswer(request.userAnswers)
        .fold(Redirect(controllers.routes.IndexController.onPageLoad))(
          o => Ok(view(o._1, preparedForm, mode))
        )
  }

  def onSubmit(mode: Mode): Action[AnyContent] = journeyAction.async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(
            NonExportedAmountHelper.getAmountAndDirectlyExportedAnswer(request.userAnswers)
              .fold(Redirect(controllers.routes.IndexController.onPageLoad))(
                o => BadRequest(view(o._1, formWithErrors, mode))
              )
          ),

        value =>
          request.userAnswers
            .setOrFail(AnotherBusinessExportWeightPage, value)
            .save(cacheConnector.saveUserAnswerFunc(request.pptReference))
            .map(_ => Redirect(returnsNavigator.exportedByAnotherBusinessWeightRoute(request.userAnswers,mode)))
      )
  }
}
