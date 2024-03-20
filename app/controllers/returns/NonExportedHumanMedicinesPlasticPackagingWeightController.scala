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
import forms.returns.NonExportedHumanMedicinesPlasticPackagingWeightFormProvider
import models.Mode
import models.requests.DataRequest
import navigation.ReturnsJourneyNavigator
import pages.returns.NonExportedHumanMedicinesPlasticPackagingWeightPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.returns.NonExportedHumanMedicinesPlasticPackagingWeightView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class NonExportedHumanMedicinesPlasticPackagingWeightController @Inject() (
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  navigator: ReturnsJourneyNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: NonExportedHumanMedicinesPlasticPackagingWeightFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: NonExportedHumanMedicinesPlasticPackagingWeightView,
  nonExportedAmountHelper: NonExportedAmountHelper
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val preparedForm = request.userAnswers.fill(NonExportedHumanMedicinesPlasticPackagingWeightPage, formProvider())
    createResponse(Ok, mode, preparedForm)
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      formProvider()
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(createResponse(BadRequest, mode, formWithErrors)),
          value =>
            for {
              updatedAnswers <- Future.fromTry(
                request.userAnswers.set(NonExportedHumanMedicinesPlasticPackagingWeightPage, value)
              )
              _ <- cacheConnector.set(request.pptReference, updatedAnswers)
            } yield Redirect(navigator.nonExportedHumanMedicinesPlasticPackagingWeightPage(mode))
        )
  }

  private def createResponse(responseStatus: Status, mode: Mode, form: Form[Long])(implicit
    request: DataRequest[_]
  ): Result =
    nonExportedAmountHelper.getAmountAndDirectlyExportedAnswer(request.userAnswers) match {
      case Some((amount, directlyExported, exportedByThirdParty)) =>
        responseStatus(view(amount, form, mode, directlyExported, exportedByThirdParty))
      case None => Redirect(controllers.routes.IndexController.onPageLoad)
    }

}
