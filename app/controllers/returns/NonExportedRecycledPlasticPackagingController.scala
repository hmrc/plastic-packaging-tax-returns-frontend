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
import forms.returns.NonExportedRecycledPlasticPackagingFormProvider
import models.Mode
import navigation.Navigator
import pages.returns.NonExportedRecycledPlasticPackagingPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.returns.NonExportedRecycledPlasticPackagingView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class NonExportedRecycledPlasticPackagingController @Inject()(
                                                               override val messagesApi: MessagesApi,
                                                               cacheConnector: CacheConnector,
                                                               navigator: Navigator,
                                                               identify: IdentifierAction,
                                                               getData: DataRetrievalAction,
                                                               requireData: DataRequiredAction,
                                                               formProvider: NonExportedRecycledPlasticPackagingFormProvider,
                                                               val controllerComponents: MessagesControllerComponents,
                                                               view: NonExportedRecycledPlasticPackagingView
                                                           )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  // todo: content switch logic
  val noDirectExports: Boolean = true


  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(NonExportedRecycledPlasticPackagingPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      NonExportedAmountHelper.nonExportedAmount.fold(identity, nonExportedAmount => Ok(view(preparedForm, mode, nonExportedAmount, noDirectExports)))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(NonExportedAmountHelper.nonExportedAmount.fold(
            identity, exportedAmount => BadRequest(view(formWithErrors, mode, exportedAmount, noDirectExports)))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(NonExportedRecycledPlasticPackagingPage, value))
            _ <- cacheConnector.set(request.pptReference, updatedAnswers)
          } yield Redirect(navigator.nextPage(NonExportedRecycledPlasticPackagingPage, mode, updatedAnswers))
      )
  }
}
