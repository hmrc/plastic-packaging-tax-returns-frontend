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
import forms.returns.NonExportedHumanMedicinesPlasticPackagingFormProvider
import models.Mode
import navigation.Navigator
import pages.returns._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.returns.NonExportedHumanMedicinesPlasticPackagingView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class NonExportedHumanMedicinesPlasticPackagingController @Inject() (
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  form: NonExportedHumanMedicinesPlasticPackagingFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: NonExportedHumanMedicinesPlasticPackagingView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData) {
      implicit request =>

        val preparedForm = request.userAnswers.get(NonExportedHumanMedicinesPlasticPackagingPage) match {
          case None        => form()
          case Some(value) => form().fill(value)
        }

        NonExportedAmountHelper.getAmountAndDirectlyExportedAnswer(request.userAnswers)
          .fold(Redirect(controllers.routes.IndexController.onPageLoad))(
            o => Ok(view(o._1, preparedForm, mode, o._2, o._3))
        )
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async {
      implicit request =>
        form().bindFromRequest().fold(
          formWithErrors =>
            Future.successful(
              NonExportedAmountHelper.getAmountAndDirectlyExportedAnswer(request.userAnswers)
                .fold(Redirect(controllers.routes.IndexController.onPageLoad))(
                  o => BadRequest(view(o._1, formWithErrors, mode, o._2, o._3))
                )
            ),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(NonExportedHumanMedicinesPlasticPackagingPage, value))
              _              <- cacheConnector.set(request.pptReference, updatedAnswers)
            } yield Redirect(navigator.nextPage(NonExportedHumanMedicinesPlasticPackagingPage, mode, updatedAnswers))
        )
    }
}
